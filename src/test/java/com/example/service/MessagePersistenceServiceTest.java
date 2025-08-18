package com.example.service;

import com.example.entity.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

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
}