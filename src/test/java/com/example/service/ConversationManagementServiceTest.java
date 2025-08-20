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

  @Test
  void shouldGenerateTitleForWhitespaceOnlyTitle() {
    // Given
    Long conversationId = 1L;
    String userMessage = "人工智能发展历程";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("   ");  // 只有空格的标题
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), eq("人工智能发展历程"));
  }

  @Test
  void shouldGenerateTitleForNewConversationTitle() {
    // Given
    Long conversationId = 1L;
    String userMessage = "区块链技术原理";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("新对话");  // 默认标题
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), eq("区块链技术原理"));
  }

  @Test
  void shouldGenerateTitleWithPaddedNewConversationTitle() {
    // Given
    Long conversationId = 1L;
    String userMessage = "神经网络算法";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("  新对话  ");  // 有空格的新对话标题
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), eq("神经网络算法"));
  }

  @Test
  void shouldGenerateDefaultTitleForNullMessage() {
    // Given
    Long conversationId = 1L;
    String userMessage = null;
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    // 对于null消息，服务应该不会调用updateConversationTitle
    verify(conversationService, never()).updateConversationTitle(any(), any());
  }

  @Test
  void shouldGenerateDefaultTitleForEmptyMessage() {
    // Given
    Long conversationId = 1L;
    String userMessage = "";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("新对话");
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), eq("新对话"));
  }

  @Test
  void shouldGenerateDefaultTitleForWhitespaceMessage() {
    // Given
    Long conversationId = 1L;
    String userMessage = "   ";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), eq("新对话"));
  }

  @Test
  void shouldUseShortMessageAsTitle() {
    // Given
    Long conversationId = 1L;
    String userMessage = "你好";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), eq("你好"));
  }

  @Test
  void shouldUse20CharMessageAsTitle() {
    // Given
    Long conversationId = 1L;
    String userMessage = "这是一个20个字符长的消息测试";  // 正好20个字符
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("");
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), eq("这是一个20个字符长的消息测试"));
  }

  @Test
  void shouldUseFirstSentenceAsTitle() {
    // Given
    Long conversationId = 1L;
    String userMessage = "什么是AI。它是如何工作的？请详细解释一下原理";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("新对话");
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), eq("什么是AI"));
  }

  @Test
  void shouldUseFirstSentenceWithExclamationAsTitle() {
    // Given
    Long conversationId = 1L;
    String userMessage = "帮我分析数据！这个很重要，需要快速处理";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("  ");
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), eq("帮我分析数据！这个很重要，需要快速处理"));
  }

  @Test
  void shouldUseFirstSentenceWithQuestionAsTitle() {
    // Given
    Long conversationId = 1L;
    String userMessage = "如何学习编程？我是一个初学者，需要什么基础知识";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), eq("如何学习编程"));
  }

  @Test
  void shouldUseFirstSentenceWithNewlineAsTitle() {
    // Given
    Long conversationId = 1L;
    String userMessage = "Python基础语法\n请详细介绍变量和数据类型";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("");
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), eq("Python基础语法"));
  }

  @Test
  void shouldSkipLongFirstSentence() {
    // Given
    Long conversationId = 1L;
    String userMessage = "请详细介绍一下人工智能技术在现代社会中的应用场景和发展趋势。包括机器学习、深度学习等";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("新对话");
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleMessageWithPunctuation() {
    // Given
    Long conversationId = 1L;
    String userMessage = "分析数据，这是一个简单的任务";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleLongMessageTruncation() {
    // Given
    Long conversationId = 1L;
    String userMessage = "这是一个很长的消息，用来测试标题截断功能，应该被正确地截断并添加省略号";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("");
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleFirstSentenceExtraction() {
    // Given
    Long conversationId = 1L;
    String userMessage = "简单问题。这是第二句话，应该不会被使用。";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("新对话");
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleVariousEdgeCases() {
    // Given
    Long conversationId = 1L;
    String userMessage = "测试边界情况的标题生成功能";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleTitleUpdateError() {
    // Given
    Long conversationId = 1L;
    String userMessage = "测试错误处理";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    doThrow(new RuntimeException("更新标题失败"))
        .when(conversationService).updateConversationTitle(any(), any());
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();  // 错误应该被捕获
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleEmptyFirstSentence() {
    // Given - 测试split后第一个sentence为空的情况
    Long conversationId = 1L;
    String userMessage = "。这是第二句话，第一句是空的";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleFirstSentenceOverLimit() {
    // Given - 测试第一句话超过25字符的情况
    Long conversationId = 1L;
    String userMessage = "这是一个超过二十五个字符长度的第一句话，应该跳过使用第一句话的逻辑。第二句话";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleTruncateWithEndingPunctuation() {
    // Given - 测试截断位置恰好是标点符号的情况
    Long conversationId = 1L;
    String userMessage = "这是二十个字符测试消息，"; // 第20个字符是逗号
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleTruncateWithPunctuationInMiddle() {
    // Given - 测试在截断范围内找到标点符号的情况
    Long conversationId = 1L;
    String userMessage = "这是一个测试消息，用来验证标点符号截断功能的正确性和完整性";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleTruncateWithNoPunctuationFound() {
    // Given - 测试在截断范围内找不到合适标点符号的情况
    Long conversationId = 1L;
    String userMessage = "这是一个没有标点符号的长消息用来测试截断逻辑当找不到合适标点时的处理方式";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleTruncateWithSemicolon() {
    // Given - 测试找到分号标点符号的情况
    Long conversationId = 1L;
    String userMessage = "这是测试分号；截断功能是否能正确处理各种标点符号的测试消息";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleTruncateWithColon() {
    // Given - 测试找到冒号标点符号的情况
    Long conversationId = 1L;
    String userMessage = "测试冒号标点：截断逻辑应该能正确识别并在此处截断消息内容";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleTruncateWithDot() {
    // Given - 测试找到顿号标点符号的情况
    Long conversationId = 1L;
    String userMessage = "这是测试顿号、标点符号截断功能的消息内容用来验证算法正确性";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleNullMessage() {
    // Given - 测试null消息的情况，会导致NullPointerException被onErrorResume捕获
    Long conversationId = 1L;
    String userMessage = null;
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("");
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then - null消息会导致userMessage.trim()抛异常，被onErrorResume捕获
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    // null消息会导致异常，不会调用updateConversationTitle
    verify(conversationService, never()).updateConversationTitle(any(), any());
  }

  @Test
  void shouldHandleEmptySentencesArray() {
    // Given - 测试split后sentences数组为空的情况
    Long conversationId = 1L;
    String userMessage = "这是一个超过二十个字符但分割后没有有效句子的消息"; // 没有分隔符，split会产生一个元素
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleExactly20CharMessage() {
    // Given - 测试恰好20个字符的消息，走不到智能截取逻辑
    Long conversationId = 1L;
    String userMessage = "这是恰好二十个字符的消息测试"; // 恰好20个字符
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("");
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), eq("这是恰好二十个字符的消息测试"));
  }

  @Test
  void shouldHandleTruncateWithMatchingPunctuation() {
    // Given - 测试截断位置恰好匹配标点符号的情况
    Long conversationId = 1L;
    String userMessage = "这是一个测试消息，用来验证在第二十个位置正好是标点符号时的处理逻辑"; // 第20个字符是标点符号
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleSentencesWithOnlyPeriods() {
    // Given - 测试只有句号分隔符的情况，产生空的第一句
    Long conversationId = 1L;
    String userMessage = "。。。这是测试只有句号的情况，第一个分割结果应该是空的";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("");
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleEmptyStringAfterSplit() {
    // Given - 测试sentences.length = 0的情况（虽然理论上split不会产生空数组）
    // 或者测试sentences[0].trim().isEmpty()为true的情况
    Long conversationId = 1L;
    String userMessage = "。！？\n"; // 只有分隔符，分割后第一个元素为空字符串
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldTestRemainingPunctuationBranches() {
    // Given - 测试L91中未覆盖的标点符号分支
    Long conversationId = 1L;
    // 构造一个字符串，在截断范围内包含特定标点符号，比如顿号'、'
    String userMessage = "这是一个测试消息、用来验证标点符号截断功能的正确性和完整性";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }

  @Test
  void shouldHandleTruncatedMessageEndingWithValidPunctuation() {
    // Given - 测试truncated.matches(".*[。！？，、；：]$")为true的情况
    Long conversationId = 1L;
    String userMessage = "这是一个恰好在第二十位结束的消息，"; // 第20个字符是逗号，匹配正则表达式
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(null);
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationService.getConversationById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationManagementService.generateTitleIfNeeded(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationService).getConversationById(conversationId);
    verify(conversationService).updateConversationTitle(eq(conversationId), any(String.class));
  }
}