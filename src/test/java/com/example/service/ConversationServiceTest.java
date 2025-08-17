package com.example.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.entity.Conversation;
import com.example.entity.Message;
import com.example.mapper.ConversationMapper;
import com.example.mapper.MessageMapper;
import com.example.service.impl.ConversationServiceImpl;
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
}
