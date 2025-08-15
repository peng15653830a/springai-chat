package com.example.service.impl;

import static com.example.service.constants.AiChatConstants.*;

import com.example.config.AiConfig;
import com.example.entity.Conversation;
import com.example.entity.Message;
import com.example.service.AiChatService;
import com.example.service.AiResponse;
import com.example.service.ConversationService;
import com.example.service.MessageService;
import com.example.service.SearchService;
import com.example.service.SseEmitterManager;
import org.springframework.ai.chat.client.ChatClient;
import com.example.service.dto.AiChatRequest;
import com.example.service.dto.ChatMessage;
import com.example.service.dto.ChatResponse;
import com.example.service.dto.SearchResult;
import com.example.service.dto.SseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AI聊天服务实现类
 *
 * @author xupeng
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

  /** 用于存储当前线程处理的用户消息ID，便于错误回滚 */
  private static final ThreadLocal<Long> CURRENT_USER_MESSAGE_ID = new ThreadLocal<>();

  @Autowired private AiConfig aiConfig;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private MessageService messageService;
  
  @Autowired private ConversationService conversationService;

  @Autowired private SearchService searchService;

  @Autowired private SseEmitterManager sseEmitterManager;

  @Autowired private ChatClient chatClient;

  @Override
  public AiResponse chat(Long conversationId, String userMessage, String searchContext) {
    List<Message> history = getConversationHistory(conversationId);
    List<ChatMessage> messageHistory = convertMessagesToHistory(history);
    return chatWithAi(userMessage, messageHistory, searchContext);
  }

  @Override
  public AiResponse chatWithAi(String message, List<ChatMessage> history) {
    return chatWithAi(message, history, null);
  }

  @Override
  public AiResponse chatWithAi(String message, List<ChatMessage> history, String searchContext) {
    validateInput(message, history);

    try {
      List<ChatMessage> messages = buildMessageList(message, history, searchContext);

      log.debug("发送Spring AI聊天请求，消息数量: {}", messages.size());

      return sendChatRequest(messages, searchContext);

    } catch (Exception e) {
      log.error("AI聊天请求失败: {}", e.getMessage(), e);
      return createErrorResponse(e);
    }
  }

  @Override
  public List<Message> getConversationHistory(Long conversationId) {
    return messageService.getMessagesByConversationId(conversationId);
  }

  @Override
  public Message sendMessage(Long conversationId, String content, boolean searchEnabled) {
    validateConversationInput(conversationId, content);

    // 保存用户消息
    Message userMessage = messageService.saveMessage(conversationId, ROLE_USER, content);
    log.info("用户消息保存成功，消息ID: {}", userMessage.getId());

    // 立即生成对话标题（基于用户问题）
    generateConversationTitleIfNeeded(conversationId, content);

    // 异步处理AI回复，传递用户消息ID用于错误回滚
    processAiResponseAsync(conversationId, content, searchEnabled, userMessage.getId());

    return userMessage;
  }

  /** 默认流式响应分块大小 */
  private static final int DEFAULT_STREAMING_CHUNK_SIZE = 50;

  @Override
  public List<String> splitResponseForStreaming(String response) {
    if (!StringUtils.hasText(response)) {
      return new ArrayList<>();
    }

    List<String> chunks = new ArrayList<>();
    // 可配置的块大小
    int chunkSize = DEFAULT_STREAMING_CHUNK_SIZE;

    for (int i = 0; i < response.length(); i += chunkSize) {
      int end = Math.min(i + chunkSize, response.length());
      chunks.add(response.substring(i, end));
    }

    return chunks;
  }

  /** 验证输入参数 */
  private void validateInput(String message, List<ChatMessage> history) {
    if (!StringUtils.hasText(message)) {
      throw new IllegalArgumentException(ERROR_EMPTY_MESSAGE_CONTENT);
    }
  }

  /** 验证会话输入参数 */
  private void validateConversationInput(Long conversationId, String content) {
    if (conversationId == null || conversationId <= 0) {
      throw new IllegalArgumentException(ERROR_INVALID_CONVERSATION_ID);
    }
    if (!StringUtils.hasText(content)) {
      throw new IllegalArgumentException(ERROR_EMPTY_MESSAGE_CONTENT);
    }
  }

  /** 构建消息列表 */
  private List<ChatMessage> buildMessageList(
      String message, List<ChatMessage> history, String searchContext) {
    List<ChatMessage> messages = new ArrayList<>();

    // 添加系统提示
    if (StringUtils.hasText(searchContext)) {
      messages.add(ChatMessage.createSystemMessage("基于以下搜索结果回答用户问题：\n" + searchContext));
    }

    // 添加历史消息
    if (history != null && !history.isEmpty()) {
      messages.addAll(history);
    }

    // 添加当前用户消息
    messages.add(ChatMessage.createUserMessage(message));

    return messages;
  }


  /** 使用Spring AI发送同步聊天请求 */
  private AiResponse sendChatRequest(List<ChatMessage> messages, String searchContext) {
    try {
      String fullMessage = buildChatPrompt(messages, searchContext);
      
      log.debug("发送AI请求，消息长度: {}", fullMessage.length());
      
      String response = chatClient.prompt()
          .user(fullMessage)
          .call()
          .content();
      
      log.debug("AI响应成功，响应长度: {}", response != null ? response.length() : 0);
      return new AiResponse(response, null);
      
    } catch (Exception e) {
      log.error("Spring AI同步聊天请求失败: {}", e.getMessage(), e);
      return createErrorResponse(e);
    }
  }
  
  /** 构建聊天提示 */
  private String buildChatPrompt(List<ChatMessage> messages, String searchContext) {
    StringBuilder prompt = new StringBuilder();
    
    // 添加搜索上下文
    if (StringUtils.hasText(searchContext)) {
      prompt.append("基于以下搜索结果回答用户问题：\n").append(searchContext).append("\n\n");
    }
    
    // 添加消息历史
    for (ChatMessage msg : messages) {
      if ("user".equals(msg.getRole())) {
        prompt.append("用户: ").append(msg.getContent()).append("\n");
      } else if ("assistant".equals(msg.getRole())) {
        prompt.append("助手: ").append(msg.getContent()).append("\n");
      }
    }
    
    return prompt.toString();
  }

  /** 使用Spring AI进行流式聊天请求 */
  private void sendStreamingChatRequest(String userMessage, String searchContext, Long conversationId, List<SearchResult> searchResults) {
    try {
      List<Message> history = getConversationHistory(conversationId);
      String fullPrompt = buildFullPrompt(userMessage, searchContext, history);
      
      log.debug("发送流式AI请求，会话ID: {}, 提示长度: {}", conversationId, fullPrompt.length());
      
      // 使用Spring AI流式API获取回答内容，同时并行获取推理内容
      StringBuilder responseBuilder = new StringBuilder();
      StringBuilder reasoningBuilder = new StringBuilder();
      
      chatClient.prompt()
          .user(fullPrompt)
          .stream()
          .chatResponse()
          .doOnNext(chatResponse -> {
            try {
              log.debug("🔍 收到ChatResponse: {}", chatResponse);
              
              if (chatResponse != null && chatResponse.getResults() != null && !chatResponse.getResults().isEmpty()) {
                log.debug("📊 ChatResponse结果数量: {}", chatResponse.getResults().size());
                
                var generation = chatResponse.getResults().get(0);
                log.debug("🎯 Generation对象: {}", generation);
                log.debug("🎯 Generation类型: {}", generation.getClass().getName());
                
                var output = generation.getOutput();
                log.debug("📝 Output对象: {}", output);
                log.debug("📝 Output类型: {}", output.getClass().getName());
                
                // 处理内容部分
                if (output instanceof org.springframework.ai.chat.messages.AssistantMessage) {
                  org.springframework.ai.chat.messages.AssistantMessage assistantMsg = (org.springframework.ai.chat.messages.AssistantMessage) output;
                  String content = assistantMsg.getText();
                  log.debug("💬 消息内容: [{}]", content);
                  
                  if (content != null && !content.isEmpty()) {
                    sendSseEvent(conversationId, SseEvent.chunk(content));
                    responseBuilder.append(content);
                  }
                  
                  // 详细检查metadata
                  var metadata = assistantMsg.getMetadata();
                  log.debug("🔧 Metadata对象: {}", metadata);
                  if (metadata != null) {
                    log.debug("🔧 Metadata键值对:");
                    metadata.forEach((key, value) -> {
                      log.debug("   {} = {} (类型: {})", key, value, value != null ? value.getClass().getName() : "null");
                    });
                    
                    // 检查各种可能的推理内容字段
                    String reasoning = null;
                    if (metadata.containsKey("reasoning_content")) {
                      reasoning = (String) metadata.get("reasoning_content");
                      log.debug("✅ 找到reasoning_content: [{}]", reasoning);
                    } else if (metadata.containsKey("reasoningContent")) {
                      reasoning = (String) metadata.get("reasoningContent");
                      log.debug("✅ 找到reasoningContent: [{}]", reasoning);
                    } else if (metadata.containsKey("thinking")) {
                      reasoning = (String) metadata.get("thinking");
                      log.debug("✅ 找到thinking: [{}]", reasoning);
                    } else {
                      log.debug("❌ 未找到推理内容字段");
                    }
                    
                    if (reasoning != null && !reasoning.isEmpty()) {
                      reasoningBuilder.append(reasoning);
                      log.debug("📝 累积推理内容长度: {}", reasoningBuilder.length());
                    }
                  } else {
                    log.debug("❌ Metadata为null");
                  }
                } else {
                  log.debug("❌ Output不是AssistantMessage类型: {}", output.getClass().getName());
                }
              } else {
                log.debug("❌ ChatResponse为空或无结果");
              }
            } catch (Exception e) {
              log.error("处理流式ChatResponse失败: {}", e.getMessage(), e);
            }
          })
          .doOnComplete(() -> {
            try {
              // 并行获取推理内容
              CompletableFuture<String> reasoningTask = CompletableFuture.supplyAsync(() -> {
                try {
                  return extractReasoningContentFromModelScope(fullPrompt);
                } catch (Exception e) {
                  log.error("提取推理内容失败: {}", e.getMessage(), e);
                  return "";
                }
              });
              
              // 流式响应完成后保存完整消息
              String fullResponse = responseBuilder.toString();
              
              if (!fullResponse.isEmpty()) {
                // 等待推理内容提取完成（最多等待5秒）
                String reasoning = "";
                try {
                  reasoning = reasoningTask.get(5, java.util.concurrent.TimeUnit.SECONDS);
                } catch (Exception e) {
                  log.warn("获取推理内容超时或失败: {}", e.getMessage());
                }
                
                // 保存消息时包含推理内容
                String thinkingContent = reasoning.isEmpty() ? null : reasoning;
                Message aiMessage = messageService.saveMessage(conversationId, ROLE_ASSISTANT, fullResponse, thinkingContent, null);
                sendSseEvent(conversationId, SseEvent.end(aiMessage.getId()));
                log.debug("Spring AI流式响应完成，会话ID: {}, 响应长度: {}, 推理长度: {}", 
                    conversationId, fullResponse.length(), reasoning.length());
              } else {
                log.warn("AI响应为空，会话ID: {}", conversationId);
                sendSseEvent(conversationId, SseEvent.error("AI响应为空，请重试"));
              }
            } catch (Exception e) {
              log.error("保存AI消息失败: {}", e.getMessage(), e);
              handleAiResponseError(conversationId, e);
            }
          })
          .doOnError(error -> {
            log.error("Spring AI流式响应失败，会话ID: {}: {}", conversationId, error.getMessage(), error);
            handleAiResponseError(conversationId, error);
          })
          .subscribe(); // 启动流式处理
          
    } catch (Exception e) {
      log.error("Spring AI流式聊天请求失败，会话ID: {}: {}", conversationId, e.getMessage(), e);
      handleAiResponseError(conversationId, e);
    }
  }






  /** 创建错误响应 */
  private AiResponse createErrorResponse(Exception e) {
    if (e instanceof IOException) {
      return new AiResponse(ERROR_NETWORK_CONNECTION, null);
    }
    return new AiResponse(ERROR_AI_SERVICE_FAILURE + ": " + e.getMessage(), null);
  }

  /** 异步处理AI回复 */
  private void processAiResponseAsync(
      Long conversationId, String userMessage, boolean searchEnabled, Long userMessageId) {
    CompletableFuture.runAsync(
        () -> {
          try {
            // 设置当前线程的用户消息ID
            CURRENT_USER_MESSAGE_ID.set(userMessageId);
            
            log.info("开始处理AI回复，会话ID: {}, 搜索开启: {}", conversationId, searchEnabled);

            // 发送开始事件
            sendSseEvent(conversationId, SseEvent.start(DEFAULT_START_MESSAGE));

            List<SearchResult> searchResults = null;
            String searchContext = "";
            
            if (searchEnabled) {
              SearchContextResult result = processSearchIfEnabled(conversationId, userMessage, searchEnabled);
              searchResults = result.getSearchResults();
              searchContext = result.getSearchContext();
            }
            
            streamChatWithAi(conversationId, userMessage, searchContext, searchResults);

            // 流式处理中已经处理了消息保存和结束事件，这里不需要重复处理
            log.debug("AI回复流式发送完成，会话ID: {}", conversationId);

          } catch (Exception e) {
            handleAiResponseError(conversationId, e);
          } finally {
            // 清理ThreadLocal
            CURRENT_USER_MESSAGE_ID.remove();
          }
        });
  }

  /** 处理AI回复错误并回滚用户消息 */
  private void handleAiResponseError(Long conversationId, Throwable e) {
    Long userMessageId = CURRENT_USER_MESSAGE_ID.get();
    log.error("处理AI回复时发生异常，会话ID: {}, 需要回滚用户消息ID: {}", conversationId, userMessageId, e);
    
    // 分析错误类型，提供更具体的错误信息
    String errorMessage = getSpecificErrorMessage(e);
    
    if (userMessageId != null) {
      // 回滚用户消息
      try {
        messageService.deleteMessage(userMessageId);
        log.info("回滚用户消息成功，消息ID: {}", userMessageId);
        
        // 发送回滚通知
        sendSseEvent(conversationId, SseEvent.error("处理消息时发生错误: " + errorMessage));
      } catch (Exception rollbackException) {
        log.error("回滚用户消息失败，消息ID: {}", userMessageId, rollbackException);
        sendSseEvent(conversationId, SseEvent.error("系统错误: " + errorMessage));
      }
    } else {
      sendSseEvent(conversationId, SseEvent.error("处理消息时发生错误: " + errorMessage));
    }
  }
  
  /** 获取具体的错误信息 */
  private String getSpecificErrorMessage(Throwable e) {
    String message = e.getMessage();
    if (message == null) {
      message = e.getClass().getSimpleName();
    }
    
    // 检查是否是HTTP错误
    if (message.contains("400")) {
      return "400 Bad Request from POST " + aiConfig.getChatApiUrl();
    } else if (message.contains("401")) {
      return "API密钥无效，请检查配置";
    } else if (message.contains("403")) {
      return "API访问被拒绝，请检查权限";
    } else if (message.contains("429")) {
      return "API调用频率超限，请稍后重试";
    } else if (message.contains("500")) {
      return "AI服务内部错误，请稍后重试";
    } else if (message.contains("timeout") || message.contains("Timeout")) {
      return "请求超时，请检查网络连接";
    } else if (message.contains("Connection")) {
      return "网络连接失败，请检查网络";
    }
    
    return message;
  }

  /** 处理搜索（如果启用） */
  private SearchContextResult processSearchIfEnabled(
      Long conversationId, String userMessage, boolean searchEnabled) {
    if (!searchEnabled) {
      return new SearchContextResult("", null);
    }

    log.info("开始搜索相关信息，会话ID: {}", conversationId);
    sendSseEvent(conversationId, SseEvent.search(SEARCH_STATUS_START));

    List<SearchResult> searchResults = searchService.searchMetaso(userMessage);
    String searchContext = searchService.formatSearchResults(searchResults);

    // 发送搜索结果给前端
    if (searchResults != null && !searchResults.isEmpty()) {
      try {
        sendSseEvent(conversationId, SseEvent.searchResults(searchResults));
        log.debug("发送搜索结果，数量: {}, 会话ID: {}", searchResults.size(), conversationId);
      } catch (Exception e) {
        log.error("序列化搜索结果失败: {}", e.getMessage());
      }
    }

    sendSseEvent(conversationId, SseEvent.search(SEARCH_STATUS_COMPLETE));
    log.debug("搜索完成，上下文长度: {}, 会话ID: {}", searchContext.length(), conversationId);

    return new SearchContextResult(searchContext, searchResults);
  }

  /** 搜索上下文结果内部类 */
  private static class SearchContextResult {
    private final String searchContext;
    private final List<SearchResult> searchResults;

    public SearchContextResult(String searchContext, List<SearchResult> searchResults) {
      this.searchContext = searchContext;
      this.searchResults = searchResults;
    }

    public String getSearchContext() {
      return searchContext;
    }

    public List<SearchResult> getSearchResults() {
      return searchResults;
    }
  }

  /** 流式调用AI服务 */
  private String streamChatWithAi(Long conversationId, String userMessage, String searchContext, List<SearchResult> searchResults) {
    try {
      List<Message> history = getConversationHistory(conversationId);
      List<ChatMessage> messageHistory = convertMessagesToHistory(history);

      // 使用Spring AI简化的流式处理
      sendStreamingChatRequest(userMessage, searchContext, conversationId, searchResults);
      
      // 流式处理中内容已经通过SSE发送，这里返回空字符串
      return "";
      
    } catch (Exception e) {
      log.error("流式AI聊天请求失败: {}", e.getMessage(), e);
      handleAiResponseError(conversationId, e);
      return DEFAULT_SORRY_MESSAGE;
    }
  }

  /** 构建完整的提示文本，包含历史消息和搜索上下文 */
  private String buildFullPrompt(String userMessage, String searchContext, List<Message> history) {
    StringBuilder prompt = new StringBuilder();
    
    // 添加搜索上下文（如果有）
    if (StringUtils.hasText(searchContext)) {
      prompt.append("基于以下搜索结果回答用户问题：\n").append(searchContext).append("\n\n");
    }
    
    // 添加历史对话（最近10条，避免过长）
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

  /** 转换消息为历史记录格式 */
  private List<ChatMessage> convertMessagesToHistory(List<Message> messages) {
    return messages.stream().map(this::convertMessageToHistoryEntry).collect(Collectors.toList());
  }

  /** 转换单个消息为历史记录条目 */
  private ChatMessage convertMessageToHistoryEntry(Message message) {
    return new ChatMessage(message.getRole(), message.getContent());
  }

  /** 检查并生成对话标题（如果需要的话） */
  private void generateConversationTitleIfNeeded(Long conversationId, String userMessage) {
    try {
      // 获取对话信息
      Conversation conversation = conversationService.getConversationById(conversationId);
      
      // 如果已有非空标题且不是"新对话"，则无需生成
      if (conversation.getTitle() != null && 
          !conversation.getTitle().trim().isEmpty() && 
          !"新对话".equals(conversation.getTitle().trim())) {
        return;
      }
      
      // 直接使用传入的用户消息生成标题
      if (userMessage == null || userMessage.trim().isEmpty()) {
        return;
      }
      
      // 生成简洁的标题
      String newTitle = generateTitleFromMessage(userMessage.trim());
      
      // 更新对话标题
      conversationService.updateConversationTitle(conversationId, newTitle);
      log.debug("自动生成对话标题成功，会话ID: {}, 标题: {}", conversationId, newTitle);
      
    } catch (Exception e) {
      log.error("生成对话标题时发生异常，会话ID: {}", conversationId, e);
    }
  }
  
  /** 短消息标题最大长度 */
  private static final int SHORT_TITLE_MAX_LENGTH = 20;
  /** 第一句话标题最大长度 */
  private static final int FIRST_SENTENCE_MAX_LENGTH = 25;
  /** 长文本截取长度 */
  private static final int LONG_TEXT_TRUNCATE_LENGTH = 20;
  /** 长文本截取检查结束位置 */
  private static final int LONG_TEXT_TRUNCATE_END_POS = 18;
  /** 长文本截取检查开始位置 */
  private static final int LONG_TEXT_TRUNCATE_START_POS = 10;
  /** 长文本截取标点符号正则表达式 */
  private static final String LONG_TEXT_PUNCTUATION_REGEX = ".*[。！？，、；：]$";

  /** 从用户消息生成简洁标题 */
  private String generateTitleFromMessage(String message) {
    if (message == null || message.trim().isEmpty()) {
      return "新对话";
    }
    
    String cleanMessage = message.trim();
    
    // 如果消息很短（20字以内），直接使用
    if (cleanMessage.length() <= SHORT_TITLE_MAX_LENGTH) {
      return cleanMessage;
    }
    
    // 尝试找到第一句话（以句号、问号、感叹号、换行结尾）
    String[] sentences = cleanMessage.split("[。！？\n]");
    if (sentences.length > 0 && !sentences[0].trim().isEmpty()) {
      String firstSentence = sentences[0].trim();
      if (firstSentence.length() <= FIRST_SENTENCE_MAX_LENGTH) {
        return firstSentence;
      }
    }
    
    // 对于长文本，智能截取：
    // 1. 优先在标点符号处截断
    // 2. 避免截断单词（中文字符或英文单词边界）
    if (cleanMessage.length() > LONG_TEXT_TRUNCATE_LENGTH) {
      String truncated = cleanMessage.substring(0, Math.min(LONG_TEXT_TRUNCATE_LENGTH, cleanMessage.length()));
      // 如果截断位置不是标点，尝试找到合适的截断点
      if (cleanMessage.length() > LONG_TEXT_TRUNCATE_LENGTH && !truncated.matches(LONG_TEXT_PUNCTUATION_REGEX)) {
        // 尝试在18字符内找到标点符号
        for (int i = Math.min(LONG_TEXT_TRUNCATE_END_POS, truncated.length() - 1); i >= LONG_TEXT_TRUNCATE_START_POS; i--) {
          char c = truncated.charAt(i);
          if (c == '，' || c == '、' || c == '；' || c == '：') {
            return truncated.substring(0, i + 1);
          }
        }
      }
      return truncated + "...";
    }
    
    return cleanMessage;
  }

  /** 发送SSE事件 */
  private void sendSseEvent(Long conversationId, SseEvent sseEvent) {
    try {
      sseEmitterManager.sendEvent(conversationId, sseEvent);
    } catch (Exception e) {
      log.error("发送SSE事件失败，会话ID: {}, 事件类型: {}", conversationId, sseEvent.getType(), e);
    }
  }

  /** 发送SSE事件 (保持向后兼容) */
  @Deprecated
  private void sendSseEvent(Long conversationId, String eventType, Object data) {
    SseEvent event = new SseEvent(eventType, data);
    sendSseEvent(conversationId, event);
  }

  /** 从魔搭API提取推理内容 */
  private String extractReasoningContentFromModelScope(String prompt) throws Exception {
    log.debug("🔍 开始提取推理内容");
    
    // 构建请求体
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", aiConfig.getModel());
    requestBody.put("messages", List.of(
        Map.of("role", "system", "content", "你是一个有用的AI助手。"),
        Map.of("role", "user", "content", prompt)
    ));
    requestBody.put("max_tokens", aiConfig.getMaxTokens());
    requestBody.put("temperature", aiConfig.getTemperature());
    requestBody.put("stream", true);
    requestBody.put("reasoning_effort", "high");
    
    // 使用RestTemplate调用魔搭API
    org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
    
    // 设置请求头
    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
    headers.setBearerAuth(aiConfig.getApiKey());
    
    org.springframework.http.HttpEntity<Map<String, Object>> entity = 
        new org.springframework.http.HttpEntity<>(requestBody, headers);
    
    StringBuilder reasoningContent = new StringBuilder();
    
    try {
      // 发送请求并处理流式响应
      restTemplate.execute(
          aiConfig.getChatApiUrl(),
          org.springframework.http.HttpMethod.POST,
          request -> {
            request.getHeaders().putAll(headers);
            objectMapper.writeValue(request.getBody(), requestBody);
          },
          response -> {
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(response.getBody(), java.nio.charset.StandardCharsets.UTF_8))) {
              
              String line;
              while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ") && !line.contains("[DONE]")) {
                  try {
                    String jsonData = line.substring(6).trim();
                    com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(jsonData);
                    com.fasterxml.jackson.databind.JsonNode choices = root.get("choices");
                    
                    if (choices != null && choices.isArray() && choices.size() > 0) {
                      com.fasterxml.jackson.databind.JsonNode delta = choices.get(0).get("delta");
                      if (delta != null) {
                        com.fasterxml.jackson.databind.JsonNode reasoningNode = delta.get("reasoning_content");
                        if (reasoningNode != null && !reasoningNode.asText().isEmpty()) {
                          String reasoning = reasoningNode.asText();
                          reasoningContent.append(reasoning);
                          log.debug("📝 提取推理片段: [{}]", reasoning);
                        }
                      }
                    }
                  } catch (Exception e) {
                    log.debug("解析SSE数据失败: {}", e.getMessage());
                  }
                }
              }
            }
            return null;
          }
      );
    } catch (Exception e) {
      log.error("调用魔搭API失败: {}", e.getMessage(), e);
      throw e;
    }
    
    String result = reasoningContent.toString();
    log.debug("✅ 推理内容提取完成，长度: {}", result.length());
    return result;
  }
}
