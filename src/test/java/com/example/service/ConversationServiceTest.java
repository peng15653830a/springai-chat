package com.example.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.entity.Conversation;
import com.example.entity.Message;
import com.example.mapper.ConversationMapper;
import com.example.mapper.MessageMapper;
import com.example.service.impl.ConversationServiceImpl;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

  @Mock private ConversationMapper conversationMapper;

  @Mock private MessageMapper messageMapper;

  @InjectMocks private ConversationServiceImpl conversationService;

  private Conversation testConversation;
  private Message testMessage;

  @BeforeEach
  void setUp() {
    testConversation = new Conversation();
    testConversation.setId(1L);
    testConversation.setUserId(1L);
    testConversation.setTitle("测试对话");
    testConversation.setCreatedAt(LocalDateTime.now());
    testConversation.setUpdatedAt(LocalDateTime.now());

    testMessage = new Message();
    testMessage.setId(1L);
    testMessage.setConversationId(1L);
    testMessage.setContent("测试消息");
    testMessage.setRole("user");
    testMessage.setCreatedAt(LocalDateTime.now());
  }

  // ========== createConversation 测试 ==========

  @Test
  void testCreateConversation_Success() {
    // Given
    Long userId = 1L;
    String title = "新对话";

    doAnswer(
            invocation -> {
              Conversation conversation = invocation.getArgument(0);
              conversation.setId(1L);
              return null;
            })
        .when(conversationMapper)
        .insert(any(Conversation.class));

    // When
    Conversation result = conversationService.createConversation(userId, title);

    // Then
    assertNotNull(result);
    assertEquals(userId, result.getUserId());
    assertEquals(title, result.getTitle());
    assertEquals(1L, result.getId());
    verify(conversationMapper).insert(any(Conversation.class));
  }

  @Test
  void testCreateConversation_InvalidUserId() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.createConversation(null, "标题"));
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.createConversation(0L, "标题"));
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.createConversation(-1L, "标题"));

    verify(conversationMapper, never()).insert(any());
  }

  @Test
  void testCreateConversation_InvalidTitle() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.createConversation(null, "标题"));
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.createConversation(0L, "标题"));
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.createConversation(-1L, "标题"));

    verify(conversationMapper, never()).insert(any());
  }

  // ========== getConversationById 测试 ==========

  @Test
  void testGetConversationById_Success() {
    // Given
    Long conversationId = 1L;
    when(conversationMapper.selectById(conversationId)).thenReturn(testConversation);

    // When
    Conversation result = conversationService.getConversationById(conversationId);

    // Then
    assertNotNull(result);
    assertEquals(testConversation.getId(), result.getId());
    assertEquals(testConversation.getTitle(), result.getTitle());
    verify(conversationMapper).selectById(conversationId);
  }

  @Test
  void testGetConversationById_InvalidId() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.getConversationById(null));
    assertThrows(IllegalArgumentException.class, () -> conversationService.getConversationById(0L));
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.getConversationById(-1L));

    verify(conversationMapper, never()).selectById(any());
  }

  @Test
  void testGetConversationById_NotFound() {
    // Given
    Long conversationId = 999L;
    when(conversationMapper.selectById(conversationId)).thenReturn(null);

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> conversationService.getConversationById(conversationId));

    verify(conversationMapper).selectById(conversationId);
  }

  // ========== getUserConversations 测试 ==========

  @Test
  void testGetUserConversations_Success() {
    // Given
    Long userId = 1L;
    List<Conversation> conversations = Arrays.asList(testConversation);
    when(conversationMapper.selectByUserId(userId)).thenReturn(conversations);

    // When
    List<Conversation> result = conversationService.getUserConversations(userId);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testConversation.getId(), result.get(0).getId());
    verify(conversationMapper).selectByUserId(userId);
  }

  @Test
  void testGetUserConversations_InvalidUserId() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.getUserConversations(null));
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.getUserConversations(0L));
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.getUserConversations(-1L));

    verify(conversationMapper, never()).selectByUserId(any());
  }

  // ========== getRecentConversations 测试 ==========

  @Test
  void testGetRecentConversations_Success() {
    // Given
    Long userId = 1L;
    int limit = 10;
    List<Conversation> conversations = Arrays.asList(testConversation);
    when(conversationMapper.selectRecentByUserId(userId, limit)).thenReturn(conversations);

    // When
    List<Conversation> result = conversationService.getRecentConversations(userId, limit);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(conversationMapper).selectRecentByUserId(userId, limit);
  }

  // ========== updateConversationTitle 测试 ==========

  @Test
  void testUpdateConversationTitle_Success() {
    // Given
    Long conversationId = 1L;
    String newTitle = "新标题";

    // When
    conversationService.updateConversationTitle(conversationId, newTitle);

    // Then
    verify(conversationMapper)
        .updateById(
            argThat(
                conversation ->
                    conversation.getId().equals(conversationId)
                        && conversation.getTitle().equals(newTitle)));
  }

  // ========== deleteConversation 测试 ==========

  @Test
  void testDeleteConversation_Success() {
    // Given
    Long conversationId = 1L;

    // When
    conversationService.deleteConversation(conversationId);

    // Then
    verify(messageMapper).deleteByConversationId(conversationId);
    verify(conversationMapper).deleteById(conversationId);
  }

  @Test
  void testDeleteConversation_InvalidId() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.deleteConversation(null));
    assertThrows(IllegalArgumentException.class, () -> conversationService.deleteConversation(0L));
    assertThrows(IllegalArgumentException.class, () -> conversationService.deleteConversation(-1L));

    verify(messageMapper, never()).deleteByConversationId(any());
    verify(conversationMapper, never()).deleteById(any());
  }

  // ========== getConversationMessages 测试 ==========

  @Test
  void testGetConversationMessages_Success() {
    // Given
    Long conversationId = 1L;
    List<Message> messages = Arrays.asList(testMessage);
    when(messageMapper.selectByConversationId(conversationId)).thenReturn(messages);

    // When
    List<Message> result = conversationService.getConversationMessages(conversationId);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testMessage.getId(), result.get(0).getId());
    verify(messageMapper).selectByConversationId(conversationId);
  }

  @Test
  void testGetConversationMessages_InvalidId() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.getConversationMessages(null));
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.getConversationMessages(0L));
    assertThrows(
        IllegalArgumentException.class, () -> conversationService.getConversationMessages(-1L));

    verify(messageMapper, never()).selectByConversationId(any());
  }

  // ========== getRecentMessages 测试 ==========

  @Test
  void testGetRecentMessages_Success() {
    // Given
    Long conversationId = 1L;
    int limit = 10;
    List<Message> messages = Arrays.asList(testMessage);
    when(messageMapper.selectRecentMessages(conversationId, limit)).thenReturn(messages);

    // When
    List<Message> result = conversationService.getRecentMessages(conversationId, limit);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(messageMapper).selectRecentMessages(conversationId, limit);
  }

  // ========== 额外的分支测试 ==========

  @Test
  void testGetUserConversations_EmptyResult() {
    // Given
    Long userId = 1L;
    when(conversationMapper.selectByUserId(userId)).thenReturn(Arrays.asList());

    // When
    List<Conversation> result = conversationService.getUserConversations(userId);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(conversationMapper).selectByUserId(userId);
  }

  @Test
  void testGetRecentConversations_EmptyResult() {
    // Given
    Long userId = 1L;
    int limit = 5;
    when(conversationMapper.selectRecentByUserId(userId, limit)).thenReturn(Arrays.asList());

    // When
    List<Conversation> result = conversationService.getRecentConversations(userId, limit);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(conversationMapper).selectRecentByUserId(userId, limit);
  }

  @Test
  void testGetConversationMessages_EmptyResult() {
    // Given
    Long conversationId = 1L;
    when(messageMapper.selectByConversationId(conversationId)).thenReturn(Arrays.asList());

    // When
    List<Message> result = conversationService.getConversationMessages(conversationId);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(messageMapper).selectByConversationId(conversationId);
  }

  @Test
  void testGetRecentMessages_EmptyResult() {
    // Given
    Long conversationId = 1L;
    int limit = 10;
    when(messageMapper.selectRecentMessages(conversationId, limit)).thenReturn(Arrays.asList());

    // When
    List<Message> result = conversationService.getRecentMessages(conversationId, limit);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(messageMapper).selectRecentMessages(conversationId, limit);
  }

  @Test
  void testUpdateConversationTitle_WithNullTitle() {
    // Given
    Long conversationId = 1L;
    String newTitle = null;

    // When
    conversationService.updateConversationTitle(conversationId, newTitle);

    // Then
    verify(conversationMapper)
        .updateById(
            argThat(
                conversation ->
                    conversation.getId().equals(conversationId)
                        && conversation.getTitle() == null));
  }

  @Test
  void testUpdateConversationTitle_WithEmptyTitle() {
    // Given
    Long conversationId = 1L;
    String newTitle = "";

    // When
    conversationService.updateConversationTitle(conversationId, newTitle);

    // Then
    verify(conversationMapper)
        .updateById(
            argThat(
                conversation ->
                    conversation.getId().equals(conversationId)
                        && conversation.getTitle().equals("")));
  }

  // ========================= 标题管理方法测试（从 ConversationManagementServiceTest 迁移） =========================

  @Test
  void shouldGenerateTitleIfNeededAsyncForNewConversation() {
    // Given
    Long conversationId = 1L;
    String userMessage = "你好，请介绍一下量子计算";
    
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle("新对话");
    conversation.setCreatedAt(LocalDateTime.now());
    
    when(conversationMapper.selectById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationService.generateTitleIfNeededAsync(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationMapper).selectById(conversationId);
    verify(conversationMapper).updateById(any(Conversation.class));
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
    
    when(conversationMapper.selectById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationService.generateTitleIfNeededAsync(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationMapper).selectById(conversationId);
    // 验证只调用了查询方法，没有更新
    verify(conversationMapper, never()).updateById(any(Conversation.class));
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
    
    when(conversationMapper.selectById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationService.generateTitleIfNeededAsync(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationMapper).selectById(conversationId);
    verify(conversationMapper).updateById(any(Conversation.class));
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
    
    when(conversationMapper.selectById(conversationId)).thenReturn(conversation);
    
    // When & Then
    StepVerifier.create(conversationService.generateTitleIfNeededAsync(conversationId, userMessage))
        .verifyComplete();
        
    verify(conversationMapper).selectById(conversationId);
    verify(conversationMapper).updateById(any(Conversation.class));
  }

  @Test
  void shouldHandleConversationNotFoundInTitleGeneration() {
    // Given
    Long conversationId = 999L;
    String userMessage = "test message";
    
    when(conversationMapper.selectById(conversationId))
        .thenThrow(new RuntimeException("对话不存在"));

    // When & Then
    StepVerifier.create(conversationService.generateTitleIfNeededAsync(conversationId, userMessage))
        .verifyComplete();  // 应该优雅处理错误
        
    verify(conversationMapper).selectById(conversationId);
  }

  @Test
  void shouldHandleTitleGenerationError() {
    // Given
    Long conversationId = 1L;
    String userMessage = "test";
    
    when(conversationMapper.selectById(conversationId))
        .thenThrow(new RuntimeException("数据库连接失败"));

    // When & Then
    StepVerifier.create(conversationService.generateTitleIfNeededAsync(conversationId, userMessage))
        .verifyComplete();  // 错误应该被捕获并返回空Mono
        
    verify(conversationMapper).selectById(conversationId);
  }

  @Test
  void testGenerateTitleFromMessage_NullMessage() {
    // When
    String result = conversationService.generateTitleFromMessage(null);
    
    // Then
    assertEquals("新对话", result);
  }

  @Test
  void testGenerateTitleFromMessage_EmptyMessage() {
    // When
    String result = conversationService.generateTitleFromMessage("");
    
    // Then
    assertEquals("新对话", result);
  }

  @Test
  void testGenerateTitleFromMessage_WhitespaceMessage() {
    // When
    String result = conversationService.generateTitleFromMessage("   ");
    
    // Then
    assertEquals("新对话", result);
  }

  @Test
  void testGenerateTitleFromMessage_ShortMessage() {
    // When
    String result = conversationService.generateTitleFromMessage("你好");
    
    // Then
    assertEquals("你好", result);
  }

  @Test
  void testGenerateTitleFromMessage_20CharMessage() {
    // Given
    String message = "这是一个20个字符长的消息测试";  // 正好20个字符
    
    // When
    String result = conversationService.generateTitleFromMessage(message);
    
    // Then
    assertEquals("这是一个20个字符长的消息测试", result);
  }

  @Test
  void testGenerateTitleFromMessage_FirstSentence() {
    // Given
    String message = "什么是AI。它是如何工作的？请详细解释一下原理";
    
    // When
    String result = conversationService.generateTitleFromMessage(message);
    
    // Then
    assertEquals("什么是AI", result);
  }

  @Test
  void testGenerateTitleFromMessage_FirstSentenceWithExclamation() {
    // Given
    String message = "帮我分析数据！这个很重要，需要快速处理";
    
    // When
    String result = conversationService.generateTitleFromMessage(message);
    
    // Then
    assertTrue(result.length() <= 25);
  }

  @Test
  void testGenerateTitleFromMessage_FirstSentenceWithQuestion() {
    // Given
    String message = "如何学习编程？我是一个初学者，需要什么基础知识";
    
    // When
    String result = conversationService.generateTitleFromMessage(message);
    
    // Then
    assertEquals("如何学习编程", result);
  }

  @Test
  void testGenerateTitleFromMessage_FirstSentenceWithNewline() {
    // Given
    String message = "Python基础语法\n请详细介绍变量和数据类型";
    
    // When
    String result = conversationService.generateTitleFromMessage(message);
    
    // Then
    assertEquals("Python基础语法", result);
  }

  @Test
  void testGenerateTitleFromMessage_LongMessage() {
    // Given
    String message = "这是一个很长的消息，用来测试标题截断功能，应该被正确地截断并添加省略号";
    
    // When
    String result = conversationService.generateTitleFromMessage(message);
    
    // Then
    assertNotNull(result);
    assertTrue(result.length() <= 23); // 20个字符 + "..."
  }

  @Test
  void testGenerateTitleFromMessage_WithPunctuation() {
    // Given
    String message = "分析数据，这是一个简单的任务";
    
    // When
    String result = conversationService.generateTitleFromMessage(message);
    
    // Then
    assertNotNull(result);
    assertTrue(result.length() > 0);
  }

  @Test
  void testGenerateTitleFromMessage_EmptyFirstSentence() {
    // Given
    String message = "。这是第二句话，第一句是空的";
    
    // When
    String result = conversationService.generateTitleFromMessage(message);
    
    // Then
    assertNotNull(result);
    assertTrue(result.length() > 0);
  }
}
