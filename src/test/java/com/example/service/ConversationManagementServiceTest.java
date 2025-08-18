package com.example.service;

import com.example.entity.Conversation;
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
 * ConversationManagementService测试
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
class ConversationManagementServiceTest {

  @Mock
  private ConversationService conversationService;

  private ConversationManagementService conversationManagementService;

  @BeforeEach
  void setUp() {
    conversationManagementService = new ConversationManagementService();
    ReflectionTestUtils.setField(conversationManagementService, "conversationService", conversationService);
  }

  @Test
  void shouldGenerateTitleForNewConversation() {
    // Given
    Long conversationId = 1L;
    String userMessage = "你好，请介绍一下量子计算";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("新对话");
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
  }

  @Test
  void shouldSkipTitleGenerationForExistingTitle() {
    // Given
    Long conversationId = 1L;
    String userMessage = "继续上面的话题";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("量子计算讨论");
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    // 验证只调用了查询方法
    verify(conversationService, only()).getConversationById(conversationId);
  }

  @Test
  void shouldGenerateTitleForEmptyTitle() {
    // Given
    Long conversationId = 1L;
    String userMessage = "什么是机器学习";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("");  // 空标题
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
  }

  @Test
  void shouldGenerateTitleForNullTitle() {
    // Given
    Long conversationId = 1L;
    String userMessage = "解释深度学习原理";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);  // null标题
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
  }

  @Test
  void shouldHandleConversationNotFound() {
    // Given
    Long conversationId = 999L;
    String userMessage = "test message";
    
    when(conversationService.getConversationById(conversationId))
        .thenThrow(new RuntimeException("对话不存在"));

    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();  // 应该优雅处理错误
        
    verify(conversationService).getConversationById(conversationId);
  }

  @Test
  void shouldHandleGenerationError() {
    // Given
    Long conversationId = 1L;
    String userMessage = "test";
    
    when(conversationService.getConversationById(conversationId))
        .thenThrow(new RuntimeException("数据库连接失败"));

    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();  // 错误应该被捕获并返回空Mono
        
    verify(conversationService).getConversationById(conversationId);
  }
}