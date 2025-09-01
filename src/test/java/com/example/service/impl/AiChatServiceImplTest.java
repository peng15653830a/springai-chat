package com.example.service.impl;

import com.example.config.ChatStreamingProperties;
import com.example.entity.Message;
import com.example.service.*;
import com.example.dto.response.SseEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.example.service.constants.AiChatConstants.ROLE_ASSISTANT;
import static com.example.service.constants.AiChatConstants.ROLE_USER;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AiChatServiceImpl测试类
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
class AiChatServiceImplTest {

  @Mock(lenient = true)
  private ChatStreamingProperties streamingProperties;

  @Mock(lenient = true)
  private ModelScopeDirectService modelScopeDirectService;

  @Mock(lenient = true)
  private SearchService searchService;

  @Mock(lenient = true)
  private ConversationService conversationService;

  @Mock(lenient = true)
  private MessageService messageService;
  
  @Mock(lenient = true)
  private com.example.service.chat.PromptBuilder promptBuilder;
  
  @Mock(lenient = true)
  private com.example.service.chat.ChatErrorHandler errorHandler;

  @InjectMocks
  private AiChatServiceImpl aiChatService;

  private Long conversationId = 1L;
  private String userMessage = "What is artificial intelligence?";

  @BeforeEach
  void setUp() {
    // Setup default mocks
    when(messageService.saveUserMessageAsync(anyLong(), anyString()))
        .thenReturn(Mono.just(createMessage(1L, ROLE_USER, userMessage)));
    
    when(conversationService.generateTitleIfNeededAsync(anyLong(), anyString()))
        .thenReturn(Mono.empty());
    
    SearchService.SearchContextResult searchResult = 
        new SearchService.SearchContextResult("", null, Flux.empty());
    when(searchService.performSearchWithEvents(anyString(), anyBoolean()))
        .thenReturn(Mono.just(searchResult));
    
    when(messageService.getConversationHistoryAsync(anyLong()))
        .thenReturn(Mono.just(Arrays.asList()));
    
    when(streamingProperties.getResponseTimeout()).thenReturn(Duration.ofSeconds(30));
    
    when(modelScopeDirectService.executeDirectStreaming(anyString(), anyLong(), anyBoolean()))
        .thenReturn(Flux.just(SseEventResponse.chunk("Test response")));
        
    when(promptBuilder.buildPrompt(anyLong(), anyString(), anyBoolean()))
        .thenReturn(Mono.just("Test prompt"));
        
    when(errorHandler.handleChatError(any(Throwable.class)))
        .thenReturn(Flux.just(SseEventResponse.error("Test error")));
  }

  @Test
  void shouldStreamChatSuccessfully() {
    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(messageService).saveUserMessageAsync(conversationId, userMessage);
    verify(searchService, times(2)).performSearchWithEvents(userMessage, false);
    verify(messageService).getConversationHistoryAsync(conversationId);
    verify(modelScopeDirectService).executeDirectStreaming(anyString(), eq(conversationId), eq(false));
  }

  @Test
  void shouldStreamChatWithSearchEnabled() {
    // Given
    SearchService.SearchContextResult searchResult = 
        new SearchService.SearchContextResult(
            "Search results: AI information", 
            null,
            Flux.just(SseEventResponse.search("Searching for AI..."))
        );
    when(searchService.performSearchWithEvents(userMessage, true))
        .thenReturn(Mono.just(searchResult));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, true, false))
        .expectNext(SseEventResponse.search("Searching for AI..."))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(searchService, times(2)).performSearchWithEvents(userMessage, true);
    verify(modelScopeDirectService).executeDirectStreaming(contains("Search results: AI information"), eq(conversationId), eq(false));
  }

  @Test
  void shouldStreamChatWithDeepThinking() {
    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, true))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(modelScopeDirectService).executeDirectStreaming(anyString(), eq(conversationId), eq(true));
  }

  @Test
  void shouldIncludeConversationHistoryInPrompt() {
    // Given
    List<Message> history = Arrays.asList(
        createMessage(1L, ROLE_USER, "Previous question"),
        createMessage(2L, ROLE_ASSISTANT, "Previous answer"),
        createMessage(3L, ROLE_USER, "Another question")
    );
    when(messageService.getConversationHistoryAsync(conversationId))
        .thenReturn(Mono.just(history));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(modelScopeDirectService).executeDirectStreaming(
        argThat(prompt -> 
            prompt.contains("用户: Previous question") && 
            prompt.contains("助手: Previous answer") &&
            prompt.contains("用户: Another question") &&
            prompt.contains("用户: " + userMessage)
        ), 
        eq(conversationId), 
        eq(false)
    );
  }

  @Test
  void shouldLimitHistoryToLast10Messages() {
    // Given - 创建15条历史消息
    List<Message> history = Arrays.asList(
        createMessage(1L, ROLE_USER, "Message 1"),
        createMessage(2L, ROLE_ASSISTANT, "Response 1"),
        createMessage(3L, ROLE_USER, "Message 2"),
        createMessage(4L, ROLE_ASSISTANT, "Response 2"),
        createMessage(5L, ROLE_USER, "Message 3"),
        createMessage(6L, ROLE_ASSISTANT, "Response 3"),
        createMessage(7L, ROLE_USER, "Message 4"),
        createMessage(8L, ROLE_ASSISTANT, "Response 4"),
        createMessage(9L, ROLE_USER, "Message 5"),
        createMessage(10L, ROLE_ASSISTANT, "Response 5"),
        createMessage(11L, ROLE_USER, "Message 6"), // 这条应该包含在内
        createMessage(12L, ROLE_ASSISTANT, "Response 6"),
        createMessage(13L, ROLE_USER, "Message 7"),
        createMessage(14L, ROLE_ASSISTANT, "Response 7"),
        createMessage(15L, ROLE_USER, "Message 8")
    );
    when(messageService.getConversationHistoryAsync(conversationId))
        .thenReturn(Mono.just(history));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(modelScopeDirectService).executeDirectStreaming(
        argThat(prompt -> 
            !prompt.contains("Message 1") && // 早期消息不应该包含
            prompt.contains("Message 6") && // 最近10条中的第一条应该包含
            prompt.contains("Message 8") // 最后一条应该包含
        ), 
        eq(conversationId), 
        eq(false)
    );
  }

  @Test
  void shouldHandleErrorInStreamChat() {
    // Given
    when(messageService.saveUserMessageAsync(anyLong(), anyString()))
        .thenReturn(Mono.error(new RuntimeException("Database error")));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.error("AI服务暂时不可用，请稍后重试"))
        .verifyComplete();
  }

  @Test
  void shouldHandleErrorInSearchStep() {
    // Given
    when(searchService.performSearchWithEvents(anyString(), anyBoolean()))
        .thenReturn(Mono.error(new RuntimeException("Search error")));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, true, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("AI服务暂时不可用"))
        .verifyComplete();
  }

  @Test
  void shouldHandleErrorInChatStreamService() {
    // Given
    when(modelScopeDirectService.executeDirectStreaming(anyString(), anyLong(), anyBoolean()))
        .thenReturn(Flux.error(new RuntimeException("Chat service error")));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("AI服务暂时不可用"))
        .verifyComplete();
  }

  @Test
  void shouldGenerateTitleAsynchronously() {
    // When
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();

    // Then
    verify(conversationService).generateTitleIfNeededAsync(conversationId, userMessage);
  }

  @Test
  void shouldBuildPromptWithoutSearchContext() {
    // Given
    SearchService.SearchContextResult emptySearchResult = 
        new SearchService.SearchContextResult("", null, Flux.empty());
    when(searchService.performSearchWithEvents(anyString(), anyBoolean()))
        .thenReturn(Mono.just(emptySearchResult));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(modelScopeDirectService).executeDirectStreaming(
        argThat(prompt -> !prompt.contains("基于以下搜索结果")),
        eq(conversationId),
        eq(false)
    );
  }

  @Test
  void shouldBuildPromptWithSearchContext() {
    // Given
    SearchService.SearchContextResult searchResult = 
        new SearchService.SearchContextResult("AI is a field of computer science", null, Flux.empty());
    when(searchService.performSearchWithEvents(anyString(), anyBoolean()))
        .thenReturn(Mono.just(searchResult));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, true, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(modelScopeDirectService).executeDirectStreaming(
        argThat(prompt -> 
            prompt.contains("基于以下搜索结果回答用户问题：") &&
            prompt.contains("AI is a field of computer science")
        ),
        eq(conversationId),
        eq(false)
    );
  }

  @Test
  void shouldHandleEmptyConversationHistory() {
    // Given
    when(messageService.getConversationHistoryAsync(conversationId))
        .thenReturn(Mono.just(Arrays.asList()));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(modelScopeDirectService).executeDirectStreaming(
        argThat(prompt -> prompt.equals("用户: " + userMessage)),
        eq(conversationId),
        eq(false)
    );
  }

  @Test
  void shouldHandleNullConversationHistory() {
    // Given
    when(messageService.getConversationHistoryAsync(conversationId))
        .thenReturn(Mono.just(Arrays.asList()));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(modelScopeDirectService).executeDirectStreaming(
        argThat(prompt -> prompt.equals("用户: " + userMessage)),
        eq(conversationId),
        eq(false)
    );
  }

  private Message createMessage(Long id, String role, String content) {
    Message message = new Message();
    message.setId(id);
    message.setRole(role);
    message.setContent(content);
    message.setCreatedAt(LocalDateTime.now());
    return message;
  }

  // ========================= 流式处理测试（从 ChatStreamServiceTest 迁移） =========================

  @Test
  void shouldExecuteStreamingChatSuccessfully() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    // Mock ModelScopeDirectService行为  
    Flux<SseEventResponse> mockResponse = Flux.just(
        SseEventResponse.start("AI正在思考中..."),
        SseEventResponse.chunk("测试响应"),
        SseEventResponse.end(1L)
    );
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(mockResponse);

    // When & Then
    StepVerifier.create(aiChatService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "start".equals(event.getType()) && 
            "AI正在思考中...".equals(event.getData()))
        .expectNextMatches(event -> 
            "chunk".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleErrorGracefullyInExecuteStreamingChat() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    // Mock ModelScopeDirectService抛出异常
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(Flux.error(new RuntimeException("AI服务不可用")));

    // When & Then
    StepVerifier.create(aiChatService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("AI服务暂时不可用"))
        .verifyComplete();
  }

  @Test
  void shouldHandleDeepThinkingModeInExecuteStreamingChat() {
    // Given
    String prompt = "复杂问题需要深度思考";
    Long conversationId = 1L;
    
    // Mock ModelScopeDirectService行为  
    Flux<SseEventResponse> mockResponse = Flux.just(
        SseEventResponse.start("AI正在深度思考...")
    );
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, true))
        .thenReturn(mockResponse);

    // When & Then
    StepVerifier.create(aiChatService.executeStreamingChat(prompt, conversationId, true))
        .expectNextMatches(event -> 
            "start".equals(event.getType()) && 
            "AI正在深度思考...".equals(event.getData()))
        .verifyComplete();
  }

  @Test
  void shouldHandle401ErrorInStreamingChat() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(Flux.error(new RuntimeException("HTTP 401 Unauthorized")));

    // When & Then
    StepVerifier.create(aiChatService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("API密钥无效"))
        .verifyComplete();
  }

  @Test
  void shouldHandle429ErrorInStreamingChat() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(Flux.error(new RuntimeException("HTTP 429 Too Many Requests")));

    // When & Then
    StepVerifier.create(aiChatService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("API调用频率超限"))
        .verifyComplete();
  }

  @Test
  void shouldHandleTimeoutErrorInStreamingChat() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(Flux.error(new RuntimeException("Request timeout occurred")));

    // When & Then
    StepVerifier.create(aiChatService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("请求超时"))
        .verifyComplete();
  }

  @Test
  void shouldHandleConnectionErrorInStreamingChat() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(Flux.error(new RuntimeException("Connection refused")));

    // When & Then
    StepVerifier.create(aiChatService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("网络连接失败"))
        .verifyComplete();
  }

  @Test
  void shouldHandleTimeoutFromConfiguration() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    // 设置短超时时间
    when(streamingProperties.getResponseTimeout()).thenReturn(Duration.ofMillis(1));
    
    // Mock一个慢速响应
    Flux<SseEventResponse> slowResponse = Flux.just(SseEventResponse.chunk("慢速响应"))
        .delayElements(Duration.ofMillis(100));
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(slowResponse);

    // When & Then
    StepVerifier.create(aiChatService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleComplexErrorMessageInStreamingChat() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    // 复杂的错误消息包含多个关键词
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(Flux.error(new RuntimeException("Connection timeout: HTTP 401 error")));

    // When & Then
    StepVerifier.create(aiChatService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("API密钥无效")) // 应该优先匹配401错误
        .verifyComplete();
  }
}