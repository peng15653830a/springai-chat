package com.example.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.entity.Message;
import com.example.mapper.MessageMapper;
import com.example.service.dto.SseEventResponse;
import com.example.service.impl.MessageServiceImpl;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

  @Mock private MessageMapper messageMapper;

  @InjectMocks private MessageServiceImpl messageService;

  private Message testMessage;

  @BeforeEach
  void setUp() {
    testMessage = new Message();
    testMessage.setId(1L);
    testMessage.setConversationId(1L);
    testMessage.setRole("user");
    testMessage.setContent("测试消息");
    testMessage.setCreatedAt(LocalDateTime.now());
  }

  // ========== saveMessage 三参数方法测试 ==========

  @Test
  void testSaveMessage_ThreeParams_Success() {
    // Given
    Long conversationId = 1L;
    String role = "user";
    String content = "测试消息";

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(1L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(conversationId, role, content);

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertNull(result.getThinking());
    assertNull(result.getSearchResults());
    assertEquals(1L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_ThreeParams_InvalidConversationId() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(null, "user", "内容"));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(0L, "user", "内容"));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(-1L, "user", "内容"));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_ThreeParams_InvalidRole() {
    // When & Then
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(1L, null, "内容"));
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(1L, "", "内容"));
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(1L, "   ", "内容"));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_ThreeParams_InvalidContent() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(1L, "user", null));
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(1L, "user", ""));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(1L, "user", "   "));

    verify(messageMapper, never()).insert(any());
  }

  // ========== saveMessage 四参数方法测试 ==========

  @Test
  void testSaveMessage_FourParams_Success() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "AI回复";
    String searchResults = "搜索结果";

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(2L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(conversationId, role, content, searchResults);

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertNull(result.getThinking());
    assertEquals(searchResults, result.getSearchResults());
    assertEquals(2L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FourParams_NullSearchResults() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "AI回复";
    String searchResults = null;

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(3L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(conversationId, role, content, searchResults);

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertNull(result.getThinking());
    assertNull(result.getSearchResults());
    assertEquals(3L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  // ========== saveMessage 五参数方法测试 ==========

  @Test
  void testSaveMessage_FiveParams_Success() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "AI回复";
    String thinking = "思考过程";
    String searchResults = "搜索结果";

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(4L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result =
        messageService.saveMessage(conversationId, role, content, thinking, searchResults);

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertEquals(thinking, result.getThinking());
    assertEquals(searchResults, result.getSearchResults());
    assertEquals(4L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FiveParams_NullOptionalFields() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "AI回复";
    String thinking = null;
    String searchResults = null;

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(5L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result =
        messageService.saveMessage(conversationId, role, content, thinking, searchResults);

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertNull(result.getThinking());
    assertNull(result.getSearchResults());
    assertEquals(5L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FiveParams_InvalidConversationId() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(null, "user", "内容", "思考", "搜索"));
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(0L, "user", "内容", "思考", "搜索"));
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(-1L, "user", "内容", "思考", "搜索"));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_FiveParams_InvalidRole() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(1L, null, "内容", "思考", "搜索"));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(1L, "", "内容", "思考", "搜索"));
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(1L, "   ", "内容", "思考", "搜索"));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_FiveParams_InvalidContent() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(1L, "user", null, "思考", "搜索"));
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(1L, "user", "", "思考", "搜索"));
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(1L, "user", "   ", "思考", "搜索"));

    verify(messageMapper, never()).insert(any());
  }

  // ========== getMessageById 测试 ==========

  @Test
  void testGetMessageById_Success() {
    // Given
    Long messageId = 1L;
    when(messageMapper.selectById(messageId)).thenReturn(testMessage);

    // When
    Message result = messageService.getMessageById(messageId);

    // Then
    assertNotNull(result);
    assertEquals(testMessage.getId(), result.getId());
    assertEquals(testMessage.getContent(), result.getContent());
    verify(messageMapper).selectById(messageId);
  }

  @Test
  void testGetMessageById_NotFound() {
    // Given
    Long messageId = 999L;
    when(messageMapper.selectById(messageId)).thenReturn(null);

    // When
    Message result = messageService.getMessageById(messageId);

    // Then
    assertNull(result);
    verify(messageMapper).selectById(messageId);
  }

  // ========== getMessagesByConversationId 测试 ==========

  @Test
  void testGetMessagesByConversationId_Success() {
    // Given
    Long conversationId = 1L;
    List<Message> messages = Arrays.asList(testMessage);
    when(messageMapper.selectByConversationId(conversationId)).thenReturn(messages);

    // When
    List<Message> result = messageService.getMessagesByConversationId(conversationId);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testMessage.getId(), result.get(0).getId());
    verify(messageMapper).selectByConversationId(conversationId);
  }

  @Test
  void testGetMessagesByConversationId_InvalidId() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> messageService.getMessagesByConversationId(null));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.getMessagesByConversationId(0L));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.getMessagesByConversationId(-1L));

    verify(messageMapper, never()).selectByConversationId(any());
  }

  @Test
  void testGetMessagesByConversationId_EmptyResult() {
    // Given
    Long conversationId = 1L;
    List<Message> emptyMessages = Arrays.asList();
    when(messageMapper.selectByConversationId(conversationId)).thenReturn(emptyMessages);

    // When
    List<Message> result = messageService.getMessagesByConversationId(conversationId);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(messageMapper).selectByConversationId(conversationId);
  }

  // ========== 额外的分支测试 ==========

  @Test
  void testSaveMessage_FourParams_EmptySearchResults() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "AI回复";
    String searchResults = "";

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(6L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(conversationId, role, content, searchResults);

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertNull(result.getThinking());
    assertEquals("", result.getSearchResults());
    assertEquals(6L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FiveParams_EmptyOptionalFields() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "AI回复";
    String thinking = "";
    String searchResults = "";

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(7L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result =
        messageService.saveMessage(conversationId, role, content, thinking, searchResults);

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertEquals("", result.getThinking());
    assertEquals("", result.getSearchResults());
    assertEquals(7L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FiveParams_MixedOptionalFields() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "AI回复";
    String thinking = "思考过程";
    String searchResults = null;

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(8L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result =
        messageService.saveMessage(conversationId, role, content, thinking, searchResults);

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertEquals(thinking, result.getThinking());
    assertNull(result.getSearchResults());
    assertEquals(8L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testGetMessageById_NullId() {
    // Given
    Long messageId = null;
    when(messageMapper.selectById(messageId)).thenReturn(null);

    // When
    Message result = messageService.getMessageById(messageId);

    // Then
    assertNull(result);
    verify(messageMapper).selectById(messageId);
  }

  @Test
  void testGetMessagesByConversationId_MultipleMessages() {
    // Given
    Long conversationId = 1L;
    Message message1 = new Message();
    message1.setId(1L);
    message1.setRole("user");
    message1.setContent("用户消息");

    Message message2 = new Message();
    message2.setId(2L);
    message2.setRole("assistant");
    message2.setContent("AI回复");

    List<Message> messages = Arrays.asList(message1, message2);
    when(messageMapper.selectByConversationId(conversationId)).thenReturn(messages);

    // When
    List<Message> result = messageService.getMessagesByConversationId(conversationId);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("user", result.get(0).getRole());
    assertEquals("assistant", result.get(1).getRole());
    verify(messageMapper).selectByConversationId(conversationId);
  }

  // ========== deleteMessage 测试 ==========

  @Test
  void testDeleteMessage_Success() {
    // Given
    Long messageId = 1L;
    doNothing().when(messageMapper).deleteById(messageId);

    // When
    assertDoesNotThrow(() -> messageService.deleteMessage(messageId));

    // Then
    verify(messageMapper).deleteById(messageId);
  }

  @Test
  void testDeleteMessage_InvalidId() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> messageService.deleteMessage(null));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.deleteMessage(0L));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.deleteMessage(-1L));

    verify(messageMapper, never()).deleteById(any());
  }

  // ========================= 响应式方法测试（从 MessagePersistenceServiceTest 迁移） =========================

  @Test
  void shouldSaveUserMessageAsync() {
    // Given
    Long conversationId = 1L;
    String content = "Hello AI";
    
    Message expectedMessage = new Message();
    expectedMessage.setId(1L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("user");
    expectedMessage.setContent(content);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(1L);
          return null;
        })
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    StepVerifier.create(messageService.saveUserMessageAsync(conversationId, content))
        .expectNextMatches(message -> 
            message.getId().equals(1L) &&
            message.getContent().equals(content) &&
            "user".equals(message.getRole()))
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void shouldHandleSaveUserMessageAsyncError() {
    // Given
    Long conversationId = 1L;
    String content = "Hello AI";
    
    doThrow(new RuntimeException("数据库连接失败"))
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    StepVerifier.create(messageService.saveUserMessageAsync(conversationId, content))
        .expectErrorMatches(error -> 
            error instanceof RuntimeException &&
            error.getMessage().contains("保存用户消息失败"))
        .verify();
  }

  @Test
  void shouldSaveAiMessageAsync() {
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
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(2L);
          return null;
        })
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    StepVerifier.create(messageService.saveAiMessageAsync(conversationId, content, thinking))
        .expectNextMatches(event -> 
            "end".equals(event.getType()))
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void shouldSaveAiMessageAsyncWithoutThinking() {
    // Given
    Long conversationId = 1L;
    String content = "Simple response";
    
    Message expectedMessage = new Message();
    expectedMessage.setId(3L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(3L);
          return null;
        })
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    StepVerifier.create(messageService.saveAiMessageAsync(conversationId, content, null))
        .expectNextMatches(event -> 
            "end".equals(event.getType()))
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void shouldHandleSaveAiMessageAsyncError() {
    // Given
    Long conversationId = 1L;
    String content = "Response content";
    
    doThrow(new RuntimeException("数据库写入失败"))
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    StepVerifier.create(messageService.saveAiMessageAsync(conversationId, content, null))
        .expectErrorMatches(error -> 
            error instanceof RuntimeException &&
            error.getMessage().contains("保存AI消息失败"))
        .verify();
  }

  @Test
  void shouldSaveAiMessageWithSearchAsync() {
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
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(4L);
          return null;
        })
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    StepVerifier.create(messageService.saveAiMessageWithSearchAsync(
            conversationId, content, thinking, searchResults))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(4L);
        })
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void shouldSaveAiMessageWithSearchAsyncEmptyResults() {
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
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(5L);
          return null;
        })
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    StepVerifier.create(messageService.saveAiMessageWithSearchAsync(
            conversationId, content, thinking, searchResults))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(5L);
        })
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void shouldGetConversationHistoryAsync() {
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
    
    when(messageMapper.selectByConversationId(conversationId))
        .thenReturn(expectedMessages);

    // When & Then
    StepVerifier.create(messageService.getConversationHistoryAsync(conversationId))
        .expectNextMatches(messages -> 
            messages.size() == 2 &&
            messages.get(0).getId().equals(1L) &&
            messages.get(1).getId().equals(2L))
        .verifyComplete();
        
    verify(messageMapper).selectByConversationId(conversationId);
  }

  @Test
  void shouldGetEmptyConversationHistoryAsync() {
    // Given
    Long conversationId = 1L;
    List<Message> emptyMessages = Collections.emptyList();
    
    when(messageMapper.selectByConversationId(conversationId))
        .thenReturn(emptyMessages);

    // When & Then
    StepVerifier.create(messageService.getConversationHistoryAsync(conversationId))
        .expectNextMatches(messages -> messages.isEmpty())
        .verifyComplete();
        
    verify(messageMapper).selectByConversationId(conversationId);
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
  void shouldHandleComplexSearchResultsSerialization() {
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
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(8L);
          return null;
        })
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    StepVerifier.create(messageService.saveAiMessageWithSearchAsync(
            conversationId, content, thinking, complexSearchResults))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(8L);
        })
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }
}
