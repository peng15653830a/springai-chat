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
import com.example.service.StreamingResponseHandler;
import com.example.service.dto.AiChatRequest;
import com.example.service.dto.ChatMessage;
import com.example.service.dto.ChatResponse;
import com.example.service.dto.SearchResult;
import com.example.service.dto.SseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import javax.net.ssl.SSLContext;
import org.apache.http.util.EntityUtils;
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

  @Autowired private AiConfig aiConfig;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private MessageService messageService;
  
  @Autowired private ConversationService conversationService;

  @Autowired private SearchService searchService;

  @Autowired private SseEmitterManager sseEmitterManager;

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
      AiChatRequest request = createChatRequest(messages);

      log.debug("发送AI聊天请求: {}", objectMapper.writeValueAsString(request));

      return sendChatRequest(request);

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

    // 异步处理AI回复
    processAiResponseAsync(conversationId, content, searchEnabled);

    return userMessage;
  }

  @Override
  public List<String> splitResponseForStreaming(String response) {
    if (!StringUtils.hasText(response)) {
      return new ArrayList<>();
    }

    List<String> chunks = new ArrayList<>();
    // 可配置的块大小
    int chunkSize = 50;

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

  /** 创建聊天请求 */
  private AiChatRequest createChatRequest(List<ChatMessage> messages) {
    return AiChatRequest.create(
        aiConfig.getModel(), messages, aiConfig.getTemperature(), aiConfig.getMaxTokens(), true);
  }

  /** 发送聊天请求 */
  private AiResponse sendChatRequest(AiChatRequest request) throws IOException {
    try (CloseableHttpClient httpClient = createSslFriendlyHttpClient()) {
      HttpPost post = createHttpPost(request);

      try (CloseableHttpResponse response = httpClient.execute(post)) {
        return processHttpResponse(response);
      }
    }
  }

  /** 发送流式聊天请求 */
  private void sendStreamingChatRequest(AiChatRequest request, Long conversationId, List<SearchResult> searchResults) throws IOException {
    try (CloseableHttpClient httpClient = createSslFriendlyHttpClient()) {
      HttpPost post = createHttpPost(request);

      try (CloseableHttpResponse response = httpClient.execute(post)) {
        processStreamingResponse(response, conversationId, searchResults);
      }
    }
  }

  /** 创建SSL友好的HttpClient */
  private CloseableHttpClient createSslFriendlyHttpClient() {
    try {
      SSLContext sslContext = SSLContextBuilder.create()
          .loadTrustMaterial(new TrustSelfSignedStrategy())
          .build();
      
      SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
          sslContext, 
          SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
      );
      
      return HttpClients.custom()
          .setSSLSocketFactory(socketFactory)
          .build();
    } catch (Exception e) {
      log.warn("创建SSL友好的HttpClient失败，使用默认配置: {}", e.getMessage());
      return HttpClients.createDefault();
    }
  }

  /** 创建HTTP请求 */
  private HttpPost createHttpPost(AiChatRequest request) throws IOException {
    HttpPost post = new HttpPost(aiConfig.getChatApiUrl());

    // 设置请求头
    post.setHeader(HTTP_HEADER_CONTENT_TYPE, HTTP_CONTENT_TYPE_JSON);
    post.setHeader(HTTP_HEADER_AUTHORIZATION, HTTP_AUTH_BEARER_PREFIX + aiConfig.getApiKey());

    // 设置请求体
    String jsonRequest = objectMapper.writeValueAsString(request);
    post.setEntity(new StringEntity(jsonRequest, StandardCharsets.UTF_8));

    return post;
  }

  /** 处理HTTP响应 */
  private AiResponse processHttpResponse(CloseableHttpResponse response) throws IOException {
    int statusCode = response.getStatusLine().getStatusCode();

    if (statusCode != HTTP_STATUS_OK) {
      log.warn("AI服务返回非200状态码: {}", statusCode);
      return new AiResponse(DEFAULT_SORRY_MESSAGE, null);
    }

    HttpEntity entity = response.getEntity();
    if (entity == null) {
      log.warn("AI服务响应为空");
      return new AiResponse(DEFAULT_SORRY_MESSAGE, null);
    }

    String responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
    return parseAiResponse(responseString);
  }

  /** 解析AI响应 */
  private AiResponse parseAiResponse(String responseString) {
    try {
      ChatResponse chatResponse = objectMapper.readValue(responseString, ChatResponse.class);

      if (chatResponse.getChoices() == null || chatResponse.getChoices().isEmpty()) {
        log.warn("AI响应中choices为空");
        return new AiResponse(DEFAULT_SORRY_MESSAGE, null);
      }

      ChatResponse.Choice firstChoice = chatResponse.getChoices().get(0);
      ChatResponse.ResponseMessage message = firstChoice.getMessage();

      if (message == null) {
        log.warn("AI响应中message为空");
        return new AiResponse(DEFAULT_SORRY_MESSAGE, null);
      }

      String content = message.getContent();
      String thinking = message.getThinking();

      return new AiResponse(content != null ? content : DEFAULT_SORRY_MESSAGE, thinking);

    } catch (Exception e) {
      log.error("解析AI响应失败: {}", e.getMessage(), e);
      return new AiResponse(DEFAULT_SORRY_MESSAGE, null);
    }
  }

  /** 处理流式响应 */
  private void processStreamingResponse(CloseableHttpResponse response, Long conversationId, List<SearchResult> searchResults) {
    StringBuilder fullResponse = new StringBuilder();
    
    StreamingResponseHandler handler = new StreamingResponseHandler(
        objectMapper,
        // onChunk: 收到内容块时的处理
        chunk -> {
          fullResponse.append(chunk);
          sendSseEvent(conversationId, SSE_EVENT_CHUNK, chunk);
        },
        // onComplete: 流式响应完成时的处理
        () -> {
          try {
            // 保存完整的AI回复，包括搜索结果
            Message aiMessage;
            if (searchResults != null && !searchResults.isEmpty()) {
              String searchResultsJson = objectMapper.writeValueAsString(searchResults);
              aiMessage = messageService.saveMessage(conversationId, ROLE_ASSISTANT, fullResponse.toString(), searchResultsJson);
              log.debug("流式AI回复保存成功（含搜索结果），消息ID: {}, 搜索结果数量: {}", aiMessage.getId(), searchResults.size());
            } else {
              aiMessage = messageService.saveMessage(conversationId, ROLE_ASSISTANT, fullResponse.toString());
              log.debug("流式AI回复保存成功，消息ID: {}", aiMessage.getId());
            }
            
            // 标题已在用户发送消息时生成，无需重复处理

            // 发送结束事件
            SseEvent.EndEventData endData = new SseEvent.EndEventData(aiMessage.getId());
            sendSseEvent(conversationId, SSE_EVENT_END, endData);
            log.debug("流式AI回复发送完成，会话ID: {}", conversationId);
          } catch (Exception e) {
            log.error("保存流式AI回复时发生异常，会话ID: {}", conversationId, e);
            sendSseEvent(conversationId, SSE_EVENT_ERROR, ERROR_AI_SERVICE_EXCEPTION + e.getMessage());
          }
        },
        // onError: 流式响应出错时的处理
        error -> {
          log.error("处理流式响应时发生异常，会话ID: {}", conversationId, error);
          sendSseEvent(conversationId, SSE_EVENT_ERROR, ERROR_AI_SERVICE_EXCEPTION + error.getMessage());
        }
    );
    
    handler.handleResponse(response);
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
      Long conversationId, String userMessage, boolean searchEnabled) {
    CompletableFuture.runAsync(
        () -> {
          try {
            log.info("开始处理AI回复，会话ID: {}, 搜索开启: {}", conversationId, searchEnabled);

            // 发送开始事件
            sendSseEvent(conversationId, SSE_EVENT_START, DEFAULT_START_MESSAGE);

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
            log.error("处理AI回复时发生异常，会话ID: {}", conversationId, e);
            sendSseEvent(
                conversationId, SSE_EVENT_ERROR, ERROR_AI_SERVICE_EXCEPTION + e.getMessage());
          }
        });
  }

  /** 处理搜索（如果启用） */
  private SearchContextResult processSearchIfEnabled(
      Long conversationId, String userMessage, boolean searchEnabled) {
    if (!searchEnabled) {
      return new SearchContextResult("", null);
    }

    log.info("开始搜索相关信息，会话ID: {}", conversationId);
    SseEvent.SearchEventData startData = new SseEvent.SearchEventData(SEARCH_STATUS_START);
    sendSseEvent(conversationId, SSE_EVENT_SEARCH, startData);

    List<SearchResult> searchResults = searchService.searchMetaso(userMessage);
    String searchContext = searchService.formatSearchResults(searchResults);

    // 发送搜索结果给前端
    if (searchResults != null && !searchResults.isEmpty()) {
      try {
        String searchResultsJson = objectMapper.writeValueAsString(searchResults);
        SseEvent.SearchResultsEventData searchResultsData = new SseEvent.SearchResultsEventData(searchResults);
        sendSseEvent(conversationId, "search_results", searchResultsData);
        log.debug("发送搜索结果，数量: {}, 会话ID: {}", searchResults.size(), conversationId);
      } catch (Exception e) {
        log.error("序列化搜索结果失败: {}", e.getMessage());
      }
    }

    SseEvent.SearchEventData completeData = new SseEvent.SearchEventData(SEARCH_STATUS_COMPLETE);
    sendSseEvent(conversationId, SSE_EVENT_SEARCH, completeData);
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

      List<ChatMessage> messages = buildMessageList(userMessage, messageHistory, searchContext);
      AiChatRequest request = createChatRequest(messages);

      log.debug("发送流式AI聊天请求: {}", objectMapper.writeValueAsString(request));

      // 使用真正的流式处理
      sendStreamingChatRequest(request, conversationId, searchResults);
      
      return ""; // 流式处理中内容已经通过SSE发送，这里返回空字符串
      
    } catch (Exception e) {
      log.error("流式AI聊天请求失败: {}", e.getMessage(), e);
      sendSseEvent(conversationId, SSE_EVENT_ERROR, ERROR_AI_SERVICE_EXCEPTION + e.getMessage());
      return DEFAULT_SORRY_MESSAGE;
    }
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
  
  /** 从用户消息生成简洁标题 */
  private String generateTitleFromMessage(String message) {
    if (message == null || message.trim().isEmpty()) {
      return "新对话";
    }
    
    String cleanMessage = message.trim();
    
    // 如果消息很短（20字以内），直接使用
    if (cleanMessage.length() <= 20) {
      return cleanMessage;
    }
    
    // 尝试找到第一句话（以句号、问号、感叹号、换行结尾）
    String[] sentences = cleanMessage.split("[。！？\\n]");
    if (sentences.length > 0 && !sentences[0].trim().isEmpty()) {
      String firstSentence = sentences[0].trim();
      if (firstSentence.length() <= 25) {
        return firstSentence;
      }
    }
    
    // 对于长文本，智能截取：
    // 1. 优先在标点符号处截断
    // 2. 避免截断单词（中文字符或英文单词边界）
    if (cleanMessage.length() > 20) {
      String truncated = cleanMessage.substring(0, Math.min(20, cleanMessage.length()));
      // 如果截断位置不是标点，尝试找到合适的截断点
      if (cleanMessage.length() > 20 && !truncated.matches(".*[。！？，、；：]$")) {
        // 尝试在18字符内找到标点符号
        for (int i = Math.min(18, truncated.length() - 1); i >= 10; i--) {
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
  private void sendSseEvent(Long conversationId, String eventType, Object data) {
    try {
      sseEmitterManager.sendMessage(conversationId, eventType, data);
    } catch (Exception e) {
      log.error("发送SSE事件失败，会话ID: {}, 事件类型: {}", conversationId, eventType, e);
    }
  }
}
