package com.example.service;

import com.example.entity.Message;
import com.example.service.dto.SseEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MessagePersistenceService测试
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
class MessagePersistenceServiceTest {

  @Mock
  private MessageService messageService;

  private MessagePersistenceService messagePersistenceService;

  @BeforeEach
  void setUp() {
    messagePersistenceService = new MessagePersistenceService();
    ReflectionTestUtils.setField(messagePersistenceService, "messageService", messageService);
  }

  @Test
  void shouldSaveUserMessage() {
    // Given
    Long conversationId = 1L;
    String content = "Hello AI";
    
    Message expectedMessage = new Message();
    expectedMessage.setId(1L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("user");
    expectedMessage.setContent(content);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    when(messageService.saveMessage(conversationId, "user", content))
        .thenReturn(expectedMessage);

    // When & Then
    StepVerifier.create(messagePersistenceService.saveUserMessage(conversationId, content))
        .expectNextMatches(message -> 
            message.getId().equals(1L) &&
            message.getContent().equals(content) &&
            "user".equals(message.getRole()))
        .verifyComplete();
        
    verify(messageService).saveMessage(conversationId, "user", content);
  }

  @Test
  void shouldHandleSaveUserMessageError() {
    // Given
    Long conversationId = 1L;
    String content = "Hello AI";
    
    when(messageService.saveMessage(conversationId, "user", content))
        .thenThrow(new RuntimeException("数据库连接失败"));

    // When & Then
    StepVerifier.create(messagePersistenceService.saveUserMessage(conversationId, content))
        .expectErrorMatches(error -> 
            error instanceof RuntimeException &&
            error.getMessage().contains("保存用户消息失败"))
        .verify();
  }

  @Test
  void shouldSaveAiMessage() {
    // Given
    Long conversationId = 1L;
    String content = "Hello! How can I help you?";
    String thinking = "用户打招呼，我应该友好回应";
    
    Message expectedMessage = new Message();
    expectedMessage.setId(2L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setThinking(thinking);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    when(messageService.saveMessage(conversationId, "assistant", content, thinking, null))
        .thenReturn(expectedMessage);

    // When & Then
    StepVerifier.create(messagePersistenceService.saveAiMessage(conversationId, content, thinking))
        .expectNextMatches(event -> 
            "end".equals(event.getType()))
        .verifyComplete();
        
    verify(messageService).saveMessage(conversationId, "assistant", content, thinking, null);
  }

  @Test
  void shouldSaveAiMessageWithoutThinking() {
    // Given
    Long conversationId = 1L;
    String content = "Simple response";
    
    Message expectedMessage = new Message();
    expectedMessage.setId(3L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    when(messageService.saveMessage(conversationId, "assistant", content, null, null))
        .thenReturn(expectedMessage);

    // When & Then
    StepVerifier.create(messagePersistenceService.saveAiMessage(conversationId, content, null))
        .expectNextMatches(event -> 
            "end".equals(event.getType()))
        .verifyComplete();
        
    verify(messageService).saveMessage(conversationId, "assistant", content, null, null);
  }

  @Test
  void shouldHandleSaveAiMessageError() {
    // Given
    Long conversationId = 1L;
    String content = "Response content";
    
    when(messageService.saveMessage(conversationId, "assistant", content, null, null))
        .thenThrow(new RuntimeException("数据库写入失败"));

    // When & Then
    StepVerifier.create(messagePersistenceService.saveAiMessage(conversationId, content, null))
        .expectErrorMatches(error -> 
            error instanceof RuntimeException &&
            error.getMessage().contains("保存AI消息失败"))
        .verify();
  }

  @Test
  void shouldSaveAiMessageWithSearchResults() {
    // Given
    Long conversationId = 1L;
    String content = "Based on search results: ...";
    String thinking = "需要搜索相关信息";
    List<String> searchResults = Arrays.asList("result1", "result2", "result3");
    
    Message expectedMessage = new Message();
    expectedMessage.setId(4L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setThinking(thinking);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    when(messageService.saveMessage(eq(conversationId), eq("assistant"), eq(content), 
        eq(thinking), anyString()))
        .thenReturn(expectedMessage);

    // When & Then
    StepVerifier.create(messagePersistenceService.saveAiMessageWithSearch(
            conversationId, content, thinking, searchResults))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(4L);
        })
        .verifyComplete();
        
    verify(messageService).saveMessage(eq(conversationId), eq("assistant"), eq(content), 
        eq(thinking), anyString());
  }

  @Test
  void shouldSaveAiMessageWithEmptySearchResults() {
    // Given
    Long conversationId = 1L;
    String content = "No search results available";
    String thinking = "无需搜索";
    List<String> searchResults = Collections.emptyList();
    
    Message expectedMessage = new Message();
    expectedMessage.setId(5L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setThinking(thinking);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    when(messageService.saveMessage(conversationId, "assistant", content, thinking, null))
        .thenReturn(expectedMessage);

    // When & Then
    StepVerifier.create(messagePersistenceService.saveAiMessageWithSearch(
            conversationId, content, thinking, searchResults))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(5L);
        })
        .verifyComplete();
        
    verify(messageService).saveMessage(conversationId, "assistant", content, thinking, null);
  }

  @Test
  void shouldSaveAiMessageWithNullSearchResults() {
    // Given
    Long conversationId = 1L;
    String content = "Response without search";
    String thinking = null;
    List<String> searchResults = null;
    
    Message expectedMessage = new Message();
    expectedMessage.setId(6L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    when(messageService.saveMessage(conversationId, "assistant", content, null, null))
        .thenReturn(expectedMessage);

    // When & Then
    StepVerifier.create(messagePersistenceService.saveAiMessageWithSearch(
            conversationId, content, thinking, searchResults))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(6L);
        })
        .verifyComplete();
        
    verify(messageService).saveMessage(conversationId, "assistant", content, null, null);
  }

  @Test
  void shouldHandleSaveAiMessageWithSearchError() {
    // Given
    Long conversationId = 1L;
    String content = "Response content";
    String thinking = "思考过程";
    List<String> searchResults = Arrays.asList("result1");
    
    when(messageService.saveMessage(eq(conversationId), eq("assistant"), eq(content), 
        eq(thinking), anyString()))
        .thenThrow(new RuntimeException("存储失败"));

    // When & Then
    StepVerifier.create(messagePersistenceService.saveAiMessageWithSearch(
            conversationId, content, thinking, searchResults))
        .expectErrorMatches(error -> 
            error instanceof RuntimeException &&
            error.getMessage().contains("保存AI消息失败"))
        .verify();
  }

  @Test
  void shouldGetConversationHistory() {
    // Given
    Long conversationId = 1L;
    
    Message message1 = new Message();
    message1.setId(1L);
    message1.setConversationId(conversationId);
    message1.setRole("user");
    message1.setContent("Hello");
    message1.setCreatedAt(LocalDateTime.now().minusMinutes(5));
    
    Message message2 = new Message();
    message2.setId(2L);
    message2.setConversationId(conversationId);
    message2.setRole("assistant");
    message2.setContent("Hi there!");
    message2.setCreatedAt(LocalDateTime.now());
    
    List<Message> expectedMessages = Arrays.asList(message1, message2);
    
    when(messageService.getMessagesByConversationId(conversationId))
        .thenReturn(expectedMessages);

    // When & Then
    StepVerifier.create(messagePersistenceService.getConversationHistory(conversationId))
        .expectNextMatches(messages -> 
            messages.size() == 2 &&
            messages.get(0).getId().equals(1L) &&
            messages.get(1).getId().equals(2L))
        .verifyComplete();
        
    verify(messageService).getMessagesByConversationId(conversationId);
  }

  @Test
  void shouldGetEmptyConversationHistory() {
    // Given
    Long conversationId = 1L;
    List<Message> emptyMessages = Collections.emptyList();
    
    when(messageService.getMessagesByConversationId(conversationId))
        .thenReturn(emptyMessages);

    // When & Then
    StepVerifier.create(messagePersistenceService.getConversationHistory(conversationId))
        .expectNextMatches(messages -> messages.isEmpty())
        .verifyComplete();
        
    verify(messageService).getMessagesByConversationId(conversationId);
  }

  @Test
  void shouldHandleGetConversationHistoryError() {
    // Given
    Long conversationId = 1L;
    
    when(messageService.getMessagesByConversationId(conversationId))
        .thenThrow(new RuntimeException("数据库查询失败"));

    // When & Then
    StepVerifier.create(messagePersistenceService.getConversationHistory(conversationId))
        .expectError(RuntimeException.class)
        .verify();
        
    verify(messageService).getMessagesByConversationId(conversationId);
  }

  @Test
  void shouldSaveAiMessageWithThinkingContent() {
    // Given
    Long conversationId = 1L;
    String content = "Detailed response";
    String thinking = "用户询问复杂问题，需要仔细思考后回答";
    
    Message expectedMessage = new Message();
    expectedMessage.setId(7L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setThinking(thinking);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    when(messageService.saveMessage(conversationId, "assistant", content, thinking, null))
        .thenReturn(expectedMessage);

    // When & Then
    StepVerifier.create(messagePersistenceService.saveAiMessage(conversationId, content, thinking))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(7L);
        })
        .verifyComplete();
        
    verify(messageService).saveMessage(conversationId, "assistant", content, thinking, null);
  }

  @Test
  void shouldSaveAiMessageWithComplexSearchResults() {
    // Given
    Long conversationId = 1L;
    String content = "Complex search-based response";
    String thinking = "基于多个搜索结果进行分析";
    
    // 创建复杂的搜索结果对象
    List<Object> complexSearchResults = Arrays.asList(
        new TestSearchResult("title1", "content1", "url1"),
        new TestSearchResult("title2", "content2", "url2")
    );
    
    Message expectedMessage = new Message();
    expectedMessage.setId(8L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setThinking(thinking);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    when(messageService.saveMessage(eq(conversationId), eq("assistant"), eq(content), 
        eq(thinking), anyString()))
        .thenReturn(expectedMessage);

    // When & Then
    StepVerifier.create(messagePersistenceService.saveAiMessageWithSearch(
            conversationId, content, thinking, complexSearchResults))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(8L);
        })
        .verifyComplete();
        
    verify(messageService).saveMessage(eq(conversationId), eq("assistant"), eq(content), 
        eq(thinking), anyString());
  }

  // 测试用的简单搜索结果类
  private static class TestSearchResult {
    private String title;
    private String content;
    private String url;
    
    public TestSearchResult(String title, String content, String url) {
      this.title = title;
      this.content = content;
      this.url = url;
    }
    
    // 需要getter方法供Jackson序列化使用
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getUrl() { return url; }
  }

  @Test
  void shouldHandleJsonSerializationSuccess() {
    // Given
    Long conversationId = 1L;
    String content = "Response with serializable search results";
    String thinking = "处理可序列化的搜索结果";
    
    // 创建一个正常的可序列化对象
    List<Object> serializableResults = Arrays.asList(new Object() {
      @SuppressWarnings("unused")
      public String getName() {
        return "test-result";
      }
      
      @SuppressWarnings("unused")
      public int getValue() {
        return 42;
      }
    });
    
    Message expectedMessage = new Message();
    expectedMessage.setId(9L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setThinking(thinking);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    // 序列化成功时，应该保存消息（searchResultsJson不为null）
    when(messageService.saveMessage(eq(conversationId), eq("assistant"), eq(content), 
        eq(thinking), anyString()))
        .thenReturn(expectedMessage);

    // When & Then
    StepVerifier.create(messagePersistenceService.saveAiMessageWithSearch(
            conversationId, content, thinking, serializableResults))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(9L);
        })
        .verifyComplete();
        
    verify(messageService).saveMessage(eq(conversationId), eq("assistant"), eq(content), 
        eq(thinking), anyString());
  }
}