package com.example.service.impl;

import com.example.entity.Message;
import com.example.service.*;
import com.example.service.dto.SseEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

  @Mock
  private ChatStreamService chatStreamService;

  @Mock
  private SearchIntegrationService searchIntegrationService;

  @Mock
  private ConversationManagementService conversationManagementService;

  @Mock
  private MessagePersistenceService messagePersistenceService;

  @InjectMocks
  private AiChatServiceImpl aiChatService;

  private Long conversationId = 1L;
  private String userMessage = "What is artificial intelligence?";

  @BeforeEach
  void setUp() {
    // Setup default mocks
    when(messagePersistenceService.saveUserMessage(anyLong(), anyString()))
        .thenReturn(Mono.just(createMessage(1L, ROLE_USER, userMessage)));
    
    when(conversationManagementService.generateTitleIfNeeded(anyLong(), anyString()))
        .thenReturn(Mono.empty());
    
    SearchIntegrationService.SearchContextResult searchResult = 
        new SearchIntegrationService.SearchContextResult("", null, Flux.empty());
    when(searchIntegrationService.performSearchIfEnabled(anyString(), anyBoolean()))
        .thenReturn(Mono.just(searchResult));
    
    when(messagePersistenceService.getConversationHistory(anyLong()))
        .thenReturn(Mono.just(Arrays.asList()));
    
    when(chatStreamService.executeStreamingChat(anyString(), anyLong(), anyBoolean()))
        .thenReturn(Flux.just(SseEventResponse.chunk("Test response")));
  }

  @Test
  void shouldStreamChatSuccessfully() {
    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(messagePersistenceService).saveUserMessage(conversationId, userMessage);
    verify(searchIntegrationService).performSearchIfEnabled(userMessage, false);
    verify(messagePersistenceService).getConversationHistory(conversationId);
    verify(chatStreamService).executeStreamingChat(anyString(), eq(conversationId), eq(false));
  }

  @Test
  void shouldStreamChatWithSearchEnabled() {
    // Given
    SearchIntegrationService.SearchContextResult searchResult = 
        new SearchIntegrationService.SearchContextResult(
            "Search results: AI information", 
            null,
            Flux.just(SseEventResponse.search("Searching for AI..."))
        );
    when(searchIntegrationService.performSearchIfEnabled(userMessage, true))
        .thenReturn(Mono.just(searchResult));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, true, false))
        .expectNext(SseEventResponse.search("Searching for AI..."))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(searchIntegrationService, times(2)).performSearchIfEnabled(userMessage, true);
    verify(chatStreamService).executeStreamingChat(contains("Search results: AI information"), eq(conversationId), eq(false));
  }

  @Test
  void shouldStreamChatWithDeepThinking() {
    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, true))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(chatStreamService).executeStreamingChat(anyString(), eq(conversationId), eq(true));
  }

  @Test
  void shouldIncludeConversationHistoryInPrompt() {
    // Given
    List<Message> history = Arrays.asList(
        createMessage(1L, ROLE_USER, "Previous question"),
        createMessage(2L, ROLE_ASSISTANT, "Previous answer"),
        createMessage(3L, ROLE_USER, "Another question")
    );
    when(messagePersistenceService.getConversationHistory(conversationId))
        .thenReturn(Mono.just(history));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(chatStreamService).executeStreamingChat(
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
    when(messagePersistenceService.getConversationHistory(conversationId))
        .thenReturn(Mono.just(history));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(chatStreamService).executeStreamingChat(
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
    when(messagePersistenceService.saveUserMessage(anyLong(), anyString()))
        .thenReturn(Mono.error(new RuntimeException("Database error")));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.error("聊天服务暂时不可用，请稍后重试"))
        .verifyComplete();
  }

  @Test
  void shouldHandleErrorInSearchStep() {
    // Given
    when(searchIntegrationService.performSearchIfEnabled(anyString(), anyBoolean()))
        .thenReturn(Mono.error(new RuntimeException("Search error")));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, true, false))
        .expectNext(SseEventResponse.error("聊天服务暂时不可用，请稍后重试"))
        .verifyComplete();
  }

  @Test
  void shouldHandleErrorInChatStreamService() {
    // Given
    when(chatStreamService.executeStreamingChat(anyString(), anyLong(), anyBoolean()))
        .thenReturn(Flux.error(new RuntimeException("Chat service error")));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.error("聊天服务暂时不可用，请稍后重试"))
        .verifyComplete();
  }

  @Test
  void shouldGenerateTitleAsynchronously() {
    // When
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();

    // Then
    verify(conversationManagementService).generateTitleIfNeeded(conversationId, userMessage);
  }

  @Test
  void shouldBuildPromptWithoutSearchContext() {
    // Given
    SearchIntegrationService.SearchContextResult emptySearchResult = 
        new SearchIntegrationService.SearchContextResult("", null, Flux.empty());
    when(searchIntegrationService.performSearchIfEnabled(anyString(), anyBoolean()))
        .thenReturn(Mono.just(emptySearchResult));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(chatStreamService).executeStreamingChat(
        argThat(prompt -> !prompt.contains("基于以下搜索结果")),
        eq(conversationId),
        eq(false)
    );
  }

  @Test
  void shouldBuildPromptWithSearchContext() {
    // Given
    SearchIntegrationService.SearchContextResult searchResult = 
        new SearchIntegrationService.SearchContextResult("AI is a field of computer science", null, Flux.empty());
    when(searchIntegrationService.performSearchIfEnabled(anyString(), anyBoolean()))
        .thenReturn(Mono.just(searchResult));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, true, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(chatStreamService).executeStreamingChat(
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
    when(messagePersistenceService.getConversationHistory(conversationId))
        .thenReturn(Mono.just(Arrays.asList()));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(chatStreamService).executeStreamingChat(
        argThat(prompt -> prompt.equals("用户: " + userMessage)),
        eq(conversationId),
        eq(false)
    );
  }

  @Test
  void shouldHandleNullConversationHistory() {
    // Given
    when(messagePersistenceService.getConversationHistory(conversationId))
        .thenReturn(Mono.just(null));

    // When & Then
    StepVerifier.create(aiChatService.streamChat(conversationId, userMessage, false, false))
        .expectNext(SseEventResponse.chunk("Test response"))
        .verifyComplete();
    
    verify(chatStreamService).executeStreamingChat(
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
}