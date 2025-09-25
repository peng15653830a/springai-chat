package com.example.service.impl;

import com.example.config.ChatStreamingProperties;
import com.example.config.MultiModelProperties;
import com.example.dto.request.StreamChatRequest;
import com.example.dto.stream.ChatEvent;
import com.example.handler.ChatErrorHandler;
import com.example.integration.ai.greatwall.GreatWallChatOptions;
import com.example.manager.ChatClientManager;
import com.example.service.*;
import com.example.strategy.model.ModelSelector;
import com.example.strategy.prompt.PromptBuilder;
import com.example.tool.WebSearchTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * AI 聊天主服务（精简版）
 * 目标：主线清晰、日志简洁、便于维护。
 * 流程：准备 → 执行 → 完成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

  private static final String PROVIDER_GREATWALL = "greatwall";

  private final ChatStreamingProperties streamingProperties;
  private final ConversationService conversationService;
  private final MessageService messageService;
  private final ChatClientManager chatClientManager;
  private final ModelSelector modelSelector;
  private final PromptBuilder promptBuilder;
  private final ChatErrorHandler errorHandler;
  private final SseEventPublisher sseEventPublisher;
  private final MessageToolResultService messageToolResultService;
  private final MultiModelProperties multiModelProperties;
  private final WebSearchTool webSearchTool;

  /**
   * 每次对话执行所需的上下文。
   */
  private record InteractionContext(
      Long conversationId,
      String conversationIdStr,
      ModelSelector.ModelSelection model,
      Long assistantMessageId) {}

  @Override
  public Flux<ChatEvent> streamChat(StreamChatRequest request) {
    log.info(
        "chat start cid={}, len={}, search={}, think={}, user={}, model={}->{}",
        request.getConversationId(),
        request.getMessage() != null ? request.getMessage().length() : 0,
        request.isSearchEnabled(),
        request.isDeepThinking(),
        request.getUserId(),
        request.getProvider(),
        request.getModel());

    // 获取SseEventPublisher的事件流，用于合并搜索事件
    var searchEventFlux = sseEventPublisher.registerConversationFlux(request.getConversationId());

    // 合并搜索事件流和主聊天流
    return Flux.merge(
            // 搜索相关的SSE事件流
            searchEventFlux,
            Flux.concat(
                // 准备阶段：处理输入和上下文
                prepareContext(request),
                // 执行阶段：与AI模型交互（Spring AI自动处理Tool Calling）
                processChat(request),
                // 完成阶段：保存结果
                finishChat(request)))
        .doFinally(
            signalType -> sseEventPublisher.removeConversation(request.getConversationId()))
        .onErrorResume(errorHandler::handleChatError);
  }

  // ========================= 第一层：主流程控制 =========================

  /** 准备阶段：处理输入和上下文 */
  private Flux<ChatEvent> prepareContext(StreamChatRequest request) {
    // 异步生成标题，不阻塞主流程
    conversationService
        .generateTitleIfNeededAsync(request.getConversationId(), request.getMessage())
        .subscribe();
    return Flux.empty();
  }

  /** 执行阶段：与AI模型交互 */
  private Flux<ChatEvent> processChat(StreamChatRequest request) {
    String userMessage = request.getMessage();
    return Flux.defer(
        () -> {
          ModelSelector.ModelSelection selected = selectModel(request);
          return messageService
              .saveUserMessageAsync(request.getConversationId(), userMessage)
              .flatMapMany(saved -> streamFromAi(selected, request, saved.getId()));
        });
  }

  /** 完成阶段：保存结果 */
  private Flux<ChatEvent> finishChat(StreamChatRequest request) { return Flux.empty(); }

  // ========================= 第二层：各阶段具体实现 =========================

  /** 构建提示词 */
  private Mono<String> buildPrompt(StreamChatRequest request) {
    return promptBuilder.buildPrompt(
        request.getConversationId(), request.getMessage(), request.isSearchEnabled());
  }

  /** 选择模型 */
  private ModelSelector.ModelSelection selectModel(StreamChatRequest request) {
    if (request.getUserId() != null) {
      // 使用用户偏好选择模型
      return modelSelector.selectModelForUser(
          request.getUserId(), request.getProvider(), request.getModel());
    } else {
      // 直接使用指定模型或默认模型
      String actualProviderName = modelSelector.getActualProviderName(request.getProvider());
      String actualModelName =
          modelSelector.getActualModelName(actualProviderName, request.getModel());
      return new ModelSelector.ModelSelection(actualProviderName, actualModelName);
    }
  }

  /** 从AI模型流式获取响应 - 使用Spring AI 1.0标准ToolContext传递消息ID */
  private Flux<ChatEvent> streamFromAi(
      ModelSelector.ModelSelection selection, StreamChatRequest request, Long userMessageId) {
    Long cid = request.getConversationId();
    String cidStr = String.valueOf(cid);

    return Mono.fromCallable(() -> createAssistantDraft(cid))
        .flatMapMany(
            assistantId -> {
              var ctx = new InteractionContext(cid, cidStr, selection, assistantId);
              var updated = new java.util.concurrent.atomic.AtomicBoolean(false);

              var streamPublisher =
                  Flux.defer(
                      () ->
                          buildPrompt(request)
                              .flatMapMany(prompt -> buildAndStream(ctx, prompt, request, updated)));

              return Flux.concat(Mono.just(ChatEvent.start("processing")), streamPublisher)
                  .timeout(streamingProperties.getResponseTimeout())
                  .onErrorResume(ex -> handleStreamError(cid, assistantId, updated, ex));
            })
        .onErrorResume(errorHandler::handleChatError);
  }

  private Long createAssistantDraft(Long conversationId) {
    com.example.entity.Message draft =
        messageService.saveMessage(
            com.example.dto.request.MessageSaveRequest.builder()
                .conversationId(conversationId)
                .role(com.example.constant.AiChatConstants.ROLE_ASSISTANT)
                .content("[draft]")
                .build());
    return draft != null ? draft.getId() : -1L;
  }

  private Flux<ChatEvent> buildAndStream(
      InteractionContext ctx, String prompt, StreamChatRequest request, java.util.concurrent.atomic.AtomicBoolean updated) {
    var promptSpec =
        getChatClientForModel(ctx.model())
            .prompt()
            .user(prompt)
            .options(buildChatOptions(ctx.model(), request))
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, ctx.conversationIdStr()))
            .toolContext(
                java.util.Map.of(
                    "conversationId", ctx.conversationId(),
                    "messageId", ctx.assistantMessageId(),
                    "searchEnabled", request.isSearchEnabled()));

    if (request.isSearchEnabled()) {
      log.info("tool injected provider={}, model={}", ctx.model().providerName(), ctx.model().modelName());
      promptSpec = promptSpec.tools(webSearchTool);
    }

    var source =
        promptSpec.stream()
            .chatResponse()
            .mapNotNull(resp -> resp.getResult() != null ? resp.getResult().getOutput() : null)
            .mapNotNull(out -> out.getText())
            .filter(s -> s != null && !s.trim().isEmpty());

    var hot = source.replay().autoConnect(2);

    return Flux.merge(
        hot.map(text -> ChatEvent.chunk(ctx.assistantMessageId(), text)),
        hot.scanWith(StringBuilder::new, (sb, c) -> sb.append(c))
            .takeLast(1)
            .flatMap(sb -> finalizeMessage(ctx, sb.toString(), updated)));
  }

  private Mono<ChatEvent> finalizeMessage(
      InteractionContext ctx, String finalText, java.util.concurrent.atomic.AtomicBoolean updated) {
    // Normalize first, then extract thinking/content once for both DB and client replacement
    String normalized = com.example.util.MarkdownNormalizer.normalize(finalText);
    var parts = extractThinkingParts(normalized);

    try {
      if (ctx.assistantMessageId() != null && ctx.assistantMessageId() > 0) {
        messageService.updateMessageContent(ctx.assistantMessageId(), parts.content(), parts.thinking());
        updated.set(true);
        if (parts.thinking() != null && !parts.thinking().isBlank()) {
          sseEventPublisher.publishThinking(
              ctx.conversationId(), ctx.assistantMessageId(), parts.thinking());
        }
      }
    } catch (Exception e) {
      log.warn("update message failed, id={}, err={}", ctx.assistantMessageId(), e.getMessage());
      throw e;
    }
    Long endId = ctx.assistantMessageId() != null && ctx.assistantMessageId() > 0 ? ctx.assistantMessageId() : null;
    return Mono.just(ChatEvent.end(endId, parts.content()));
  }

  /**
   * 提取 <think>...</think> 或 <thinking>...</thinking> 片段，返回分离后的正文与thinking。
   * 简化实现：在最终聚合内容时一次性提取，避免流式解析带来的复杂度。
   */
  private static ThinkingParts extractThinkingParts(String content) {
    if (content == null || content.isBlank()) {
      return new ThinkingParts(null, content);
    }
    String regex = "(?is)<think(?:ing)?>[\\s\\S]*?</think(?:ing)?>";
    java.util.regex.Pattern p = java.util.regex.Pattern.compile(regex);
    java.util.regex.Matcher m = p.matcher(content);
    StringBuilder thinking = new StringBuilder();
    int lastEnd = 0;
    StringBuilder cleaned = new StringBuilder();
    while (m.find()) {
      // 追加前一段正文
      cleaned.append(content, lastEnd, m.start());
      // 提取thinking内容（去掉标签）
      String raw = m.group();
      String inner = raw.replaceAll("(?is)</?think(?:ing)?>", "").trim();
      if (!inner.isBlank()) {
        if (!thinking.isEmpty()) thinking.append("\n\n");
        thinking.append(inner);
      }
      lastEnd = m.end();
    }
    // 追加最后的正文
    cleaned.append(content.substring(lastEnd));
    String thinkingStr = thinking.isEmpty() ? null : thinking.toString();
    String cleanedStr = cleaned.toString().trim();
    return new ThinkingParts(thinkingStr, cleanedStr);
  }

  private record ThinkingParts(String thinking, String content) {}

  private Flux<ChatEvent> handleStreamError(
      Long conversationId,
      Long assistantMessageId,
      java.util.concurrent.atomic.AtomicBoolean updated,
      Throwable ex) {
    if (!updated.get()) {
      try {
        if (assistantMessageId != null && assistantMessageId > 0) {
          try {
            messageToolResultService.deleteMessageToolResults(assistantMessageId);
          } catch (Exception ignore) {
            // ignore
          }
          messageService.deleteMessage(assistantMessageId);
        }
        log.info("🧹 已清理失败对话产生的占位消息及其相关工具记录，messageId: {}", assistantMessageId);
      } catch (Exception cleanEx) {
        log.warn("清理占位消息失败，messageId: {}，错误: {}", assistantMessageId, cleanEx.getMessage());
      }
    }
    return errorHandler.handleChatError(ex);
  }

  /** 获取指定模型的ChatClient ChatClient已配置MessageHistoryAdvisor，工具按需在调用时注入 */
  private ChatClient getChatClientForModel(ModelSelector.ModelSelection modelSelection) {
    // 使用ChatClientManager，每个ChatClient都已配置MessageHistoryAdvisor
    return chatClientManager.getChatClient(modelSelection.providerName());
  }

  /** 基于 provider/model 及请求参数构建本次调用的 ChatOptions。 */
  private org.springframework.ai.chat.prompt.ChatOptions buildChatOptions(
      ModelSelector.ModelSelection modelSelection, StreamChatRequest request) {
    String provider = modelSelection.providerName();
    String model = modelSelection.modelName();

    MultiModelProperties.ProviderConfig p = multiModelProperties.getProviders().get(provider);
    MultiModelProperties.ModelConfig m = null;
    if (p != null && p.getModels() != null) {
      m = p.getModels().stream().filter(x -> model.equals(x.getName())).findFirst().orElse(null);
    }

    double temperature =
        m != null && m.getTemperature() != null
            ? m.getTemperature().doubleValue()
            : multiModelProperties.getDefaults().getTemperature().doubleValue();
    Integer maxTokens =
        m != null && m.getMaxTokens() != null
            ? m.getMaxTokens()
            : multiModelProperties.getDefaults().getMaxTokens();

    if (PROVIDER_GREATWALL.equalsIgnoreCase(provider)) {
      GreatWallChatOptions opts = GreatWallChatOptions.create();
      opts.setModel(model);
      opts.setTemperature(temperature);
      opts.setMaxTokens(maxTokens);
      // 仅在模型支持且请求开启时启用 thinking
      boolean enableThinking =
          request.isDeepThinking() && chatClientManager.supportsThinking(provider, model);
      opts.setEnableThinking(enableThinking);
      return opts;
    }

    // 其他 OpenAI 兼容模型（如 deepseek 等）
    var builder = OpenAiChatOptions.builder()
        .model(model)
        .temperature(temperature)
        .maxTokens(maxTokens);

    // 当模型声明 supports-tools 且开启联网搜索时，启用工具调用（auto 模式避免死循环）
    boolean modelSupportsTools = m != null && m.isSupportsTools();
    if (modelSupportsTools && request.isSearchEnabled()) {
      try {
        // 兼容不同版本Spring AI的写法：优先尝试 toolChoice(String) 方法
        java.lang.reflect.Method toolChoiceMethod = null;
        for (var method : builder.getClass().getMethods()) {
          if (method.getName().equals("toolChoice") && method.getParameterCount() == 1) {
            toolChoiceMethod = method; break;
          }
        }
        if (toolChoiceMethod != null) {
          String paramType = toolChoiceMethod.getParameterTypes()[0].getName();
          if (paramType.equals("java.lang.String")) {
            toolChoiceMethod.invoke(builder, "auto");
            log.info("✅ 已开启 tool_choice=auto（模型支持工具调用）");
          } else {
            // 对于枚举或对象类型，尝试传入字符串，若失败则忽略（不同版本签名差异）
            try {
              toolChoiceMethod.invoke(builder, "auto");
              log.info("✅ 已开启 tool_choice=auto（兼容形式）");
            } catch (Exception ignore) {
              log.warn("⚠️ 未能以反射方式设置 tool_choice，当前Spring AI版本签名不匹配");
            }
          }
        } else {
          log.warn("⚠️ 当前 OpenAiChatOptions.builder 未暴露 toolChoice 方法，跳过强制设置");
        }
      } catch (Exception e) {
        log.warn("⚠️ 设置 tool_choice=required 失败: {}", e.getMessage());
      }
    }

    return builder.build();
  }
}
