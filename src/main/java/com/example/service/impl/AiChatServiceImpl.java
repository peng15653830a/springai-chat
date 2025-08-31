package com.example.service.impl;

import com.example.config.ChatStreamingProperties;
import com.example.config.MultiModelProperties;
import com.example.dto.ModelInfo;
import com.example.dto.UserModelPreferenceDto;
import com.example.entity.Message;
import com.example.service.*;
import com.example.service.dto.ChatRequest;
import com.example.service.dto.SseEventResponse;
import com.example.service.factory.ModelProviderFactory;
import com.example.service.provider.ModelProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.example.service.constants.AiChatConstants.ROLE_ASSISTANT;
import static com.example.service.constants.AiChatConstants.ROLE_USER;

/**
 * AI聊天服务实现类（纯响应式架构，整合了ChatStreamService的流式处理功能）
 *
 * @author xupeng
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

  @Autowired private ChatStreamingProperties streamingProperties;
  @Autowired private SearchService searchService;
  @Autowired private ConversationService conversationService;
  @Autowired private MessageService messageService;
  @Autowired private ModelProviderFactory modelProviderFactory;
  @Autowired private ModelManagementService modelManagementService;
  @Autowired private MultiModelProperties multiModelProperties;

  @Override
  public Flux<SseEventResponse> streamChat(Long conversationId, String userMessage, boolean searchEnabled, boolean deepThinking) {
    // 使用默认模型进行聊天
    return streamChatWithModel(conversationId, userMessage, searchEnabled, deepThinking, null, null, null);
  }

  @Override
  public Flux<SseEventResponse> streamChatWithModel(Long conversationId, String userMessage, 
                                                   boolean searchEnabled, boolean deepThinking,
                                                   Long userId, String providerName, String modelName) {
    log.info("开始响应式流式聊天，会话ID: {}, 消息长度: {}, 搜索开启: {}, 深度思考: {}, 用户ID: {}, 指定模型: {}-{}", 
        conversationId, userMessage.length(), searchEnabled, deepThinking, userId, providerName, modelName);

    return Flux.concat(
        // 1. 保存用户消息并生成标题
        saveUserMessageAndGenerateTitle(conversationId, userMessage),
        
        // 2. 执行搜索（如果启用）
        performSearchStep(userMessage, searchEnabled),
        
        // 3. 构建提示并执行流式聊天
        buildPromptAndStreamChatWithModel(conversationId, userMessage, searchEnabled, deepThinking, 
                                        userId, providerName, modelName)
    )
    .onErrorResume(error -> {
      log.error("流式聊天过程中发生错误，会话ID: {}", conversationId, error);
      return handleChatError(error);
    });
  }

  // ========================= 内部流式处理方法实现 =========================
  
  @Override
  public Flux<SseEventResponse> executeStreamingChat(String prompt, Long conversationId, boolean deepThinking) {
    // 使用默认模型执行聊天
    return executeStreamingChatWithModel(prompt, conversationId, deepThinking, null, null);
  }

  /**
   * 使用指定模型执行流式聊天
   */
  public Flux<SseEventResponse> executeStreamingChatWithModel(String prompt, Long conversationId, 
                                                             boolean deepThinking, 
                                                             String providerName, String modelName) {
    log.debug("开始执行流式AI聊天，提示长度: {}, 会话ID: {}, 深度思考: {}, 模型: {}-{}", 
             prompt.length(), conversationId, deepThinking, providerName, modelName);

    try {
      // 获取模型提供者
      ModelProvider provider = getModelProvider(providerName);
      String actualModelName = getActualModelName(provider, modelName);
      
      // 构建聊天请求
      ChatRequest request = ChatRequest.builder()
          .conversationId(conversationId)
          .modelName(actualModelName)
          .fullPrompt(prompt)
          .deepThinking(deepThinking)
          .build();

      log.info("🚀 使用{}提供者，模型: {}, 深度思考: {}", provider.getDisplayName(), actualModelName, deepThinking);
      
      return provider.streamChat(request)
          .timeout(streamingProperties.getResponseTimeout())
          .onErrorResume(this::handleChatError);
          
    } catch (Exception e) {
      log.error("获取模型提供者失败", e);
      return handleChatError(e);
    }
  }

  @Override
  public Flux<SseEventResponse> handleChatError(Throwable error) {
    log.error("流式聊天发生错误", error);
    
    String errorMessage = getErrorMessage(error);
    return Flux.just(SseEventResponse.error(errorMessage));
  }

  /**
   * 保存用户消息并生成标题
   */
  private Flux<SseEventResponse> saveUserMessageAndGenerateTitle(Long conversationId, String userMessage) {
    return messageService.saveUserMessageAsync(conversationId, userMessage)
        .doOnNext(message -> {
          // 异步生成标题，不阻塞主流程
          conversationService.generateTitleIfNeededAsync(conversationId, userMessage)
              .subscribe();
        })
        .then(Mono.<SseEventResponse>empty())
        .flux();
  }

  /**
   * 执行搜索步骤
   */
  private Flux<SseEventResponse> performSearchStep(String userMessage, boolean searchEnabled) {
    return searchService.performSearchWithEvents(userMessage, searchEnabled)
        .flatMapMany(SearchService.SearchContextResult::getSearchEvents);
  }

  /**
   * 构建提示并执行流式聊天
   */
  private Flux<SseEventResponse> buildPromptAndStreamChat(Long conversationId, String userMessage, 
                                                        boolean searchEnabled, boolean deepThinking) {
    return buildPromptAndStreamChatWithModel(conversationId, userMessage, searchEnabled, deepThinking, null, null, null);
  }

  /**
   * 构建提示并执行流式聊天（支持模型选择）
   */
  private Flux<SseEventResponse> buildPromptAndStreamChatWithModel(Long conversationId, String userMessage, 
                                                                 boolean searchEnabled, boolean deepThinking,
                                                                 Long userId, String providerName, String modelName) {
    return Mono.zip(
        messageService.getConversationHistoryAsync(conversationId),
        searchService.performSearchWithEvents(userMessage, searchEnabled)
    )
    .flatMapMany(tuple -> {
      List<Message> history = tuple.getT1();
      String searchContext = tuple.getT2().getSearchContext();
      
      String fullPrompt = buildFullPrompt(userMessage, searchContext, history);
      
      // 解析用户模型选择
      String[] resolvedModel = resolveUserModel(userId, providerName, modelName);
      String finalProviderName = resolvedModel[0];
      String finalModelName = resolvedModel[1];
      
      return executeStreamingChatWithModel(fullPrompt, conversationId, deepThinking, finalProviderName, finalModelName);
    });
  }

  /**
   * 构建完整的提示文本
   */
  private String buildFullPrompt(String userMessage, String searchContext, List<Message> history) {
    StringBuilder prompt = new StringBuilder();
    
    // 添加搜索上下文（如果有）
    if (searchContext != null && !searchContext.trim().isEmpty()) {
      prompt.append("基于以下搜索结果回答用户问题：\n").append(searchContext).append("\n\n");
    }
    
    // 添加历史对话（最近10条）
    if (history != null && !history.isEmpty()) {
      int startIndex = Math.max(0, history.size() - 10);
      for (int i = startIndex; i < history.size(); i++) {
        Message msg = history.get(i);
        if (ROLE_USER.equals(msg.getRole())) {
          prompt.append("用户: ").append(msg.getContent()).append("\n");
        } else if (ROLE_ASSISTANT.equals(msg.getRole())) {
          prompt.append("助手: ").append(msg.getContent()).append("\n");
        }
      }
      prompt.append("\n");
    }
    
    // 添加当前用户消息
    prompt.append("用户: ").append(userMessage);
    
    return prompt.toString();
  }

  /**
   * 获取用户友好的错误信息
   */
  private String getErrorMessage(Throwable error) {
    String message = error.getMessage();
    if (message == null) {
      message = error.getClass().getSimpleName();
    }
    
    if (message.contains("401")) {
      return "API密钥无效，请检查配置";
    } else if (message.contains("429")) {
      return "API调用频率超限，请稍后重试";
    } else if (message.contains("timeout")) {
      return "请求超时，请检查网络连接";
    } else if (message.contains("Connection")) {
      return "网络连接失败，请检查网络";
    }
    
    return "AI服务暂时不可用，请稍后重试";
  }

  /**
   * 解析用户模型选择
   * 优先级：指定的模型 > 用户偏好 > 系统默认
   * 
   * @param userId 用户ID
   * @param providerName 指定的提供者名称
   * @param modelName 指定的模型名称
   * @return [提供者名称, 模型名称]
   */
  private String[] resolveUserModel(Long userId, String providerName, String modelName) {
    // 如果指定了完整的模型信息，直接使用
    if (providerName != null && modelName != null) {
      log.debug("使用指定模型: {}-{}", providerName, modelName);
      return new String[]{providerName, modelName};
    }
    
    // 尝试获取用户默认模型偏好
    if (userId != null) {
      try {
        UserModelPreferenceDto userPreference = modelManagementService.getUserDefaultModel(userId);
        if (userPreference != null) {
          log.debug("使用用户默认模型: {}-{}", userPreference.getProviderName(), userPreference.getModelName());
          return new String[]{userPreference.getProviderName(), userPreference.getModelName()};
        }
      } catch (Exception e) {
        log.warn("获取用户模型偏好失败，使用系统默认: {}", e.getMessage());
      }
    }
    
    // 使用系统默认模型
    String defaultProvider = multiModelProperties.getDefaultProvider();
    String defaultModel = multiModelProperties.getDefaultModel();
    log.debug("使用系统默认模型: {}-{}", defaultProvider, defaultModel);
    return new String[]{defaultProvider, defaultModel};
  }

  /**
   * 获取模型提供者
   * 
   * @param providerName 提供者名称，如果为null则使用默认提供者
   * @return 模型提供者实例
   */
  private ModelProvider getModelProvider(String providerName) {
    if (providerName == null) {
      return modelProviderFactory.getDefaultProvider();
    }
    return modelProviderFactory.getProvider(providerName);
  }

  /**
   * 获取实际的模型名称
   * 
   * @param provider 模型提供者
   * @param modelName 指定的模型名称，如果为null则使用该提供者的第一个可用模型
   * @return 实际的模型名称
   */
  private String getActualModelName(ModelProvider provider, String modelName) {
    if (modelName != null) {
      return modelName;
    }
    
    // 获取该提供者的第一个可用模型
    List<ModelInfo> availableModels = provider.getAvailableModels();
    if (availableModels.isEmpty()) {
      throw new IllegalStateException("提供者 " + provider.getProviderName() + " 没有可用的模型");
    }
    
    return availableModels.get(0).getName();
  }
}