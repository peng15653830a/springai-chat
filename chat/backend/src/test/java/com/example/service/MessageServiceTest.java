package com.example.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.entity.Message;
import com.example.mapper.MessageMapper;
import com.example.dto.response.SseEventResponse;
import com.example.dto.request.AiMessageSaveRequest;
import com.example.dto.request.MessageSaveRequest;
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
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .build());

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
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(null)
            .role("user")
            .content("内容")
            .build()));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(0L)
            .role("user")
            .content("内容")
            .build()));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(-1L)
            .role("user")
            .content("内容")
            .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_ThreeParams_InvalidRole() {
    // When & Then
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(1L)
        .role(null)
        .content("内容")
        .build()));
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(1L)
        .role("")
        .content("内容")
        .build()));
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(1L)
        .role("   ")
        .content("内容")
        .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_ThreeParams_InvalidContent() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(1L)
            .role("user")
            .content(null)
            .build()));
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(1L)
        .role("user")
        .content("")
        .build()));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(1L)
            .role("user")
            .content("   ")
            .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_ThreeParams_WhitespaceContent() {
    // Given
    Long conversationId = 1L;
    String role = "user";
    String content = "   \t\n   "; // 只包含空白字符

    // When & Then
    assertThrows(IllegalArgumentException.class, 
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(conversationId)
            .role(role)
            .content(content)
            .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_ThreeParams_SpecialCharacters() {
    // Given
    Long conversationId = 1L;
    String role = "user";
    String content = "特殊字符测试：🌟🔍🚀";

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(4L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .build());

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertNull(result.getThinking());
    assertNull(result.getSearchResults());
    assertEquals(4L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_ThreeParams_LongContent() {
    // Given
    Long conversationId = 1L;
    String role = "user";
    StringBuilder longContent = new StringBuilder();
    for (int i = 0; i < 10000; i++) {
      longContent.append("长内容测试");
    }
    String content = longContent.toString();

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(5L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .build());

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
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .searchResults(searchResults)
        .build());

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
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .searchResults(searchResults)
        .build());

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
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .searchResults(searchResults)
        .build());

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
  void testSaveMessage_FourParams_WithSpecialCharacters() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "特殊字符测试：🌟🔍🚀";
    String searchResults = "搜索结果：🔍";

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(11L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .searchResults(searchResults)
        .build());

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertNull(result.getThinking());
    assertEquals(searchResults, result.getSearchResults());
    assertEquals(11L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FourParams_WithUnicode() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "Unicode测试：😊";
    String searchResults = "搜索结果：😊";

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(12L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .searchResults(searchResults)
        .build());

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertNull(result.getThinking());
    assertEquals(searchResults, result.getSearchResults());
    assertEquals(12L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FourParams_InvalidConversationId() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(null)
            .role("user")
            .content("内容")
            .searchResults("搜索")
            .build()));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(0L)
            .role("user")
            .content("内容")
            .searchResults("搜索")
            .build()));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(-1L)
            .role("user")
            .content("内容")
            .searchResults("搜索")
            .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_FourParams_InvalidRole() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(1L)
            .role(null)
            .content("内容")
            .searchResults("搜索")
            .build()));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(1L)
            .role("")
            .content("内容")
            .searchResults("搜索")
            .build()));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(1L)
            .role("   ")
            .content("内容")
            .searchResults("搜索")
            .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_FourParams_InvalidContent() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(1L)
            .role("user")
            .content(null)
            .searchResults("搜索")
            .build()));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(1L)
            .role("user")
            .content("")
            .searchResults("搜索")
            .build()));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(1L)
            .role("user")
            .content("   ")
            .searchResults("搜索")
            .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_FourParams_WhitespaceContent() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "   \t\n   "; // 只包含空白字符
    String searchResults = "搜索结果";

    // When & Then
    assertThrows(IllegalArgumentException.class, 
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(conversationId)
            .role(role)
            .content(content)
            .searchResults(searchResults)
            .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_FourParams_SpecialCharacters() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "特殊字符测试：🌟🔍🚀";
    String searchResults = "搜索结果：🔍";

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(7L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .searchResults(searchResults)
        .build());

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertNull(result.getThinking());
    assertEquals(searchResults, result.getSearchResults());
    assertEquals(7L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FourParams_LongContent() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    StringBuilder longContent = new StringBuilder();
    for (int i = 0; i < 10000; i++) {
      longContent.append("长内容测试");
    }
    String content = longContent.toString();
    String searchResults = "搜索结果";

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(8L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .searchResults(searchResults)
        .build());

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertNull(result.getThinking());
    assertEquals(searchResults, result.getSearchResults());
    assertEquals(8L, result.getId());
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
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .thinking(thinking)
        .searchResults(searchResults)
        .build());

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
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .thinking(thinking)
        .searchResults(searchResults)
        .build());

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
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(null)
            .role("user")
            .content("内容")
            .thinking("思考")
            .searchResults("搜索")
            .build()));
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(0L)
            .role("user")
            .content("内容")
            .thinking("思考")
            .searchResults("搜索")
            .build()));
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(-1L)
            .role("user")
            .content("内容")
            .thinking("思考")
            .searchResults("搜索")
            .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_FiveParams_InvalidRole() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(1L)
            .role(null)
            .content("内容")
            .thinking("思考")
            .searchResults("搜索")
            .build()));
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(1L)
            .role("")
            .content("内容")
            .thinking("思考")
            .searchResults("搜索")
            .build()));
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(1L)
            .role("   ")
            .content("内容")
            .thinking("思考")
            .searchResults("搜索")
            .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_FiveParams_InvalidContent() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(1L)
            .role("user")
            .content(null)
            .thinking("思考")
            .searchResults("搜索")
            .build()));
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(1L)
            .role("user")
            .content("")
            .thinking("思考")
            .searchResults("搜索")
            .build()));
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(1L)
            .role("user")
            .content("   ")
            .thinking("思考")
            .searchResults("搜索")
            .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_FiveParams_WhitespaceContent() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "   \t\n   "; // 只包含空白字符
    String thinking = "思考过程";
    String searchResults = "搜索结果";

    // When & Then
    assertThrows(IllegalArgumentException.class, 
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(conversationId)
            .role(role)
            .content(content)
            .thinking(thinking)
            .searchResults(searchResults)
            .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_FiveParams_SpecialCharacters() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "特殊字符测试：🌟🔍🚀";
    String thinking = "思考过程：🌟";
    String searchResults = "搜索结果：🔍";

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(9L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .thinking(thinking)
        .searchResults(searchResults)
        .build());

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertEquals(thinking, result.getThinking());
    assertEquals(searchResults, result.getSearchResults());
    assertEquals(9L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FiveParams_LongContent() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    StringBuilder longContent = new StringBuilder();
    for (int i = 0; i < 10000; i++) {
      longContent.append("长内容测试");
    }
    String content = longContent.toString();
    String thinking = "思考过程";
    String searchResults = "搜索结果";

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(10L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .thinking(thinking)
        .searchResults(searchResults)
        .build());

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertEquals(thinking, result.getThinking());
    assertEquals(searchResults, result.getSearchResults());
    assertEquals(10L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FiveParams_NullRole() {
    // Given
    Long conversationId = 1L;
    String role = null;
    String content = "内容";
    String thinking = "思考";
    String searchResults = "搜索结果";

    // When & Then
    assertThrows(IllegalArgumentException.class, 
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(conversationId)
            .role(role)
            .content(content)
            .thinking(thinking)
            .searchResults(searchResults)
            .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_FiveParams_EmptyRole() {
    // Given
    Long conversationId = 1L;
    String role = "";
    String content = "内容";
    String thinking = "思考";
    String searchResults = "搜索结果";

    // When & Then
    assertThrows(IllegalArgumentException.class, 
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(conversationId)
            .role(role)
            .content(content)
            .thinking(thinking)
            .searchResults(searchResults)
            .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_FiveParams_WhitespaceRole() {
    // Given
    Long conversationId = 1L;
    String role = "   ";
    String content = "内容";
    String thinking = "思考";
    String searchResults = "搜索结果";

    // When & Then
    assertThrows(IllegalArgumentException.class, 
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(conversationId)
            .role(role)
            .content(content)
            .thinking(thinking)
            .searchResults(searchResults)
            .build()));

    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_FiveParams_NullContent() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = null;
    String thinking = "思考";
    String searchResults = "搜索结果";

    // When & Then
    assertThrows(IllegalArgumentException.class, 
        () -> messageService.saveMessage(MessageSaveRequest.builder()
            .conversationId(conversationId)
            .role(role)
            .content(content)
            .thinking(thinking)
            .searchResults(searchResults)
            .build()));

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

  @Test
  void testGetMessagesByConversationId_ErrorHandling() {
    // Given
    Long conversationId = 1L;
    
    when(messageMapper.selectByConversationId(conversationId))
        .thenThrow(new RuntimeException("数据库错误"));

    // When & Then
    assertThrows(RuntimeException.class, () -> messageService.getMessagesByConversationId(conversationId));
    verify(messageMapper).selectByConversationId(conversationId);
  }

  // ========== 额外的分支测试 ==========

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
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .thinking(thinking)
        .searchResults(searchResults)
        .build());

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
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .thinking(thinking)
        .searchResults(searchResults)
        .build());

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

    // When & Then
    assertDoesNotThrow(() -> messageService.deleteMessage(messageId));
    verify(messageMapper).deleteById(messageId);
  }

  @Test
  void testDeleteMessage_InvalidId() {
    // When & Then
    assertThrows(IllegalArgumentException.class, () -> messageService.deleteMessage(null));
    assertThrows(IllegalArgumentException.class, () -> messageService.deleteMessage(0L));
    assertThrows(IllegalArgumentException.class, () -> messageService.deleteMessage(-1L));
    
    verify(messageMapper, never()).deleteById(any());
  }

  @Test
  void testDeleteMessage_ErrorHandling() {
    // Given
    Long messageId = 1L;
    
    doThrow(new RuntimeException("数据库错误"))
        .when(messageMapper)
        .deleteById(messageId);

    // When & Then
    assertDoesNotThrow(() -> messageService.deleteMessage(messageId));
    verify(messageMapper).deleteById(messageId);
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
  void testSaveUserMessageAsync_ErrorHandling() {
    // Given
    Long conversationId = 1L;
    String content = "测试消息";
    
    doThrow(new RuntimeException("数据库错误"))
        .when(messageMapper)
        .insert(any(Message.class));

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
  void testSaveAiMessageAsync_ErrorHandling() {
    // Given
    Long conversationId = 1L;
    String content = "AI回复";
    String thinking = "思考过程";
    
    doThrow(new RuntimeException("数据库错误"))
        .when(messageMapper)
        .insert(any(Message.class));

    // When & Then
    StepVerifier.create(messageService.saveAiMessageAsync(conversationId, content, thinking))
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
    AiMessageSaveRequest request = AiMessageSaveRequest.builder()
      .conversationId(conversationId)
      .content(content)
      .thinking(thinking)
      .searchResults(searchResults)
      .build();
    StepVerifier.create(messageService.saveAiMessageWithSearchAsync(request))
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
    AiMessageSaveRequest request = AiMessageSaveRequest.builder()
      .conversationId(conversationId)
      .content(content)
      .thinking(thinking)
      .searchResults(searchResults)
      .build();
    StepVerifier.create(messageService.saveAiMessageWithSearchAsync(request))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(5L);
        })
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveAiMessageWithSearchAsync_NullSearchResults() {
    // Given
    Long conversationId = 1L;
    String content = "AI回复";
    String thinking = "思考过程";
    List<String> searchResults = null;
    
    Message expectedMessage = new Message();
    expectedMessage.setId(11L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setThinking(thinking);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(11L);
          return null;
        })
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    AiMessageSaveRequest request = AiMessageSaveRequest.builder()
      .conversationId(conversationId)
      .content(content)
      .thinking(thinking)
      .searchResults(searchResults)
      .build();
    StepVerifier.create(messageService.saveAiMessageWithSearchAsync(request))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(11L);
        })
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveAiMessageWithSearchAsync_EmptySearchResults() {
    // Given
    Long conversationId = 1L;
    String content = "AI回复";
    String thinking = "思考过程";
    List<String> searchResults = Arrays.asList();
    
    Message expectedMessage = new Message();
    expectedMessage.setId(12L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setThinking(thinking);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(12L);
          return null;
        })
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    AiMessageSaveRequest request = AiMessageSaveRequest.builder()
      .conversationId(conversationId)
      .content(content)
      .thinking(thinking)
      .searchResults(searchResults)
      .build();
    StepVerifier.create(messageService.saveAiMessageWithSearchAsync(request))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(12L);
        })
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveAiMessageWithSearchAsync_ComplexObjectSerialization() {
    // Given
    Long conversationId = 1L;
    String content = "AI回复";
    String thinking = "思考过程";
    
    // 创建复杂的搜索结果对象
    class ComplexSearchResult {
      private String title;
      private String content;
      private String url;
      
      public ComplexSearchResult(String title, String content, String url) {
        this.title = title;
        this.content = content;
        this.url = url;
      }
      
      // Getters
      public String getTitle() { return title; }
      public String getContent() { return content; }
      public String getUrl() { return url; }
    }
    
    List<ComplexSearchResult> searchResults = Arrays.asList(
        new ComplexSearchResult("标题1", "内容1", "http://test1.com"),
        new ComplexSearchResult("标题2", "内容2", "http://test2.com")
    );
    
    Message expectedMessage = new Message();
    expectedMessage.setId(13L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setThinking(thinking);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(13L);
          return null;
        })
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    AiMessageSaveRequest request = AiMessageSaveRequest.builder()
      .conversationId(conversationId)
      .content(content)
      .thinking(thinking)
      .searchResults(searchResults)
      .build();
    StepVerifier.create(messageService.saveAiMessageWithSearchAsync(request))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(13L);
        })
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveAiMessageWithSearchAsync_SerializationError() {
    // Given
    Long conversationId = 1L;
    String content = "AI回复";
    String thinking = "思考过程";
    
    // 创建一个无法序列化的对象（循环引用）
    class CircularReferenceObject {
      private CircularReferenceObject self;
      private String value;
      
      public CircularReferenceObject(String value) {
        this.value = value;
        this.self = this; // 创建循环引用
      }
      
      public CircularReferenceObject getSelf() { return self; }
      public String getValue() { return value; }
    }
    
    List<CircularReferenceObject> searchResults = Arrays.asList(
        new CircularReferenceObject("测试")
    );
    
    Message expectedMessage = new Message();
    expectedMessage.setId(14L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setThinking(thinking);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(14L);
          return null;
        })
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    AiMessageSaveRequest request = AiMessageSaveRequest.builder()
      .conversationId(conversationId)
      .content(content)
      .thinking(thinking)
      .searchResults(searchResults)
      .build();
    StepVerifier.create(messageService.saveAiMessageWithSearchAsync(request))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(14L);
        })
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveAiMessageWithSearchAsync_ErrorHandling() {
    // Given
    Long conversationId = 1L;
    String content = "AI回复";
    String thinking = "思考过程";
    
    doThrow(new RuntimeException("数据库错误"))
        .when(messageMapper)
        .insert(any(Message.class));

    // When & Then
    AiMessageSaveRequest request = AiMessageSaveRequest.builder()
      .conversationId(conversationId)
      .content(content)
      .thinking(thinking)
      .searchResults(null)
      .build();
    StepVerifier.create(messageService.saveAiMessageWithSearchAsync(request))
        .expectErrorMatches(error -> 
            error instanceof RuntimeException &&
            error.getMessage().contains("保存AI消息失败"))
        .verify();
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

  @Test
  void testGetConversationHistoryAsync_ErrorHandling() {
    // Given
    Long conversationId = 1L;
    
    when(messageMapper.selectByConversationId(conversationId))
        .thenThrow(new RuntimeException("数据库错误"));

    // When & Then
    StepVerifier.create(messageService.getConversationHistoryAsync(conversationId))
        .expectErrorMatches(error -> 
            error instanceof RuntimeException)
        .verify();
  }

  @Test
  void testGetConversationHistoryAsync_InvalidConversationId() {
    // When & Then
    StepVerifier.create(messageService.getConversationHistoryAsync(null))
        .expectError(IllegalArgumentException.class)
        .verify();
        
    StepVerifier.create(messageService.getConversationHistoryAsync(0L))
        .expectError(IllegalArgumentException.class)
        .verify();
        
    StepVerifier.create(messageService.getConversationHistoryAsync(-1L))
        .expectError(IllegalArgumentException.class)
        .verify();
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
    AiMessageSaveRequest request = AiMessageSaveRequest.builder()
      .conversationId(conversationId)
      .content(content)
      .thinking(thinking)
      .searchResults(complexSearchResults)
      .build();
    StepVerifier.create(messageService.saveAiMessageWithSearchAsync(request))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(8L);
        })
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FourParams_WithZeroValues() {
    // Given
    Long conversationId = 0L;
    String role = "user";
    String content = "零值测试";
    String searchResults = "零结果";

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .searchResults(searchResults)
        .build()));
    
    verify(messageMapper, never()).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FourParams_WithNegativeValues() {
    // Given
    Long conversationId = -1L;
    String role = "user";
    String content = "负值测试";
    String searchResults = "负结果";

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .searchResults(searchResults)
        .build()));
    
    verify(messageMapper, never()).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FiveParams_WithSpecialCharacters() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "特殊字符测试：🌟🔍🚀";
    String thinking = "思考过程：🌟";
    String searchResults = "搜索结果：🔍";

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(15L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .thinking(thinking)
        .searchResults(searchResults)
        .build());

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertEquals(thinking, result.getThinking());
    assertEquals(searchResults, result.getSearchResults());
    assertEquals(15L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FiveParams_WithUnicode() {
    // Given
    Long conversationId = 1L;
    String role = "assistant";
    String content = "Unicode测试：测试中文消息";
    String thinking = "思考过程：更多中文";
    String searchResults = "搜索结果：更多中文内容";

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(0);
              message.setId(16L);
              return null;
            })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .thinking(thinking)
        .searchResults(searchResults)
        .build());

    // Then
    assertNotNull(result);
    assertEquals(conversationId, result.getConversationId());
    assertEquals(role, result.getRole());
    assertEquals(content, result.getContent());
    assertEquals(thinking, result.getThinking());
    assertEquals(searchResults, result.getSearchResults());
    assertEquals(16L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FiveParams_WithZeroValues() {
    // Given
    Long conversationId = 0L;
    String role = "user";
    String content = "零值测试";
    String thinking = "零思考";
    String searchResults = "零结果";

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .thinking(thinking)
        .searchResults(searchResults)
        .build()));
    
    verify(messageMapper, never()).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_FiveParams_WithNegativeValues() {
    // Given
    Long conversationId = -1L;
    String role = "user";
    String content = "负值测试";
    String thinking = "负思考";
    String searchResults = "负结果";

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(MessageSaveRequest.builder()
        .conversationId(conversationId)
        .role(role)
        .content(content)
        .thinking(thinking)
        .searchResults(searchResults)
        .build()));
    
    verify(messageMapper, never()).insert(any(Message.class));
  }

  @Test
  void testGetMessageById_WithSpecialCharacters() {
    // Given
    Long messageId = 1L;
    Message specialMessage = new Message();
    specialMessage.setId(messageId);
    specialMessage.setContent("特殊字符测试：🌟🔍🚀");
    specialMessage.setRole("assistant");
    specialMessage.setCreatedAt(LocalDateTime.now());
    when(messageMapper.selectById(messageId)).thenReturn(specialMessage);

    // When
    Message result = messageService.getMessageById(messageId);

    // Then
    assertNotNull(result);
    assertEquals(messageId, result.getId());
    assertEquals("特殊字符测试：🌟🔍🚀", result.getContent());
    verify(messageMapper).selectById(messageId);
  }

  @Test
  void testGetMessageById_WithUnicode() {
    // Given
    Long messageId = 1L;
    Message unicodeMessage = new Message();
    unicodeMessage.setId(messageId);
    unicodeMessage.setContent("Unicode测试：测试中文消息");
    unicodeMessage.setRole("assistant");
    unicodeMessage.setCreatedAt(LocalDateTime.now());
    when(messageMapper.selectById(messageId)).thenReturn(unicodeMessage);

    // When
    Message result = messageService.getMessageById(messageId);

    // Then
    assertNotNull(result);
    assertEquals(messageId, result.getId());
    assertEquals("Unicode测试：测试中文消息", result.getContent());
    verify(messageMapper).selectById(messageId);
  }

  @Test
  void testGetMessageById_WithZeroId() {
    // Given
    Long messageId = 0L;
    Message zeroMessage = new Message();
    zeroMessage.setId(messageId);
    zeroMessage.setContent("零ID测试");
    zeroMessage.setRole("assistant");
    zeroMessage.setCreatedAt(LocalDateTime.now());
    when(messageMapper.selectById(messageId)).thenReturn(zeroMessage);

    // When
    Message result = messageService.getMessageById(messageId);

    // Then
    assertNotNull(result);
    assertEquals(messageId, result.getId());
    assertEquals("零ID测试", result.getContent());
    verify(messageMapper).selectById(messageId);
  }

  @Test
  void testGetMessagesByConversationId_WithSpecialCharacters() {
    // Given
    Long conversationId = 1L;
    Message message1 = new Message();
    message1.setId(1L);
    message1.setRole("user");
    message1.setContent("特殊字符测试：🌟🔍🚀");

    Message message2 = new Message();
    message2.setId(2L);
    message2.setRole("assistant");
    message2.setContent("特殊回复：🚀🔍🌟");

    List<Message> messages = Arrays.asList(message1, message2);
    when(messageMapper.selectByConversationId(conversationId)).thenReturn(messages);

    // When
    List<Message> result = messageService.getMessagesByConversationId(conversationId);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("特殊字符测试：🌟🔍🚀", result.get(0).getContent());
    assertEquals("特殊回复：🚀🔍🌟", result.get(1).getContent());
    verify(messageMapper).selectByConversationId(conversationId);
  }

  @Test
  void testGetMessagesByConversationId_WithUnicode() {
    // Given
    Long conversationId = 1L;
    Message message1 = new Message();
    message1.setId(1L);
    message1.setRole("user");
    message1.setContent("Unicode测试：测试中文");

    Message message2 = new Message();
    message2.setId(2L);
    message2.setRole("assistant");
    message2.setContent("Unicode回复：更多中文");

    List<Message> messages = Arrays.asList(message1, message2);
    when(messageMapper.selectByConversationId(conversationId)).thenReturn(messages);

    // When
    List<Message> result = messageService.getMessagesByConversationId(conversationId);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Unicode测试：测试中文", result.get(0).getContent());
    assertEquals("Unicode回复：更多中文", result.get(1).getContent());
    verify(messageMapper).selectByConversationId(conversationId);
  }

  @Test
  void testGetMessagesByConversationId_WithZeroId() {
    // When & Then
    assertThrows(IllegalArgumentException.class, () -> messageService.getMessagesByConversationId(0L));
    
    verify(messageMapper, never()).selectByConversationId(any());
  }

  @Test
  void testDeleteMessage_WithSpecialCharacters() {
    // Given
    Long messageId = 1L;
    
    doNothing().when(messageMapper).deleteById(messageId);

    // When & Then
    assertDoesNotThrow(() -> messageService.deleteMessage(messageId));
    verify(messageMapper).deleteById(messageId);
  }

  @Test
  void testDeleteMessage_WithZeroId() {
    // When & Then
    assertThrows(IllegalArgumentException.class, () -> messageService.deleteMessage(0L));
    
    verify(messageMapper, never()).deleteById(any());
  }

  @Test
  void shouldSaveUserMessageAsync_WithSpecialCharacters() {
    // Given
    Long conversationId = 1L;
    String content = "特殊字符测试：🌟🔍🚀";
    
    Message expectedMessage = new Message();
    expectedMessage.setId(19L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("user");
    expectedMessage.setContent(content);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(19L);
          return null;
        })
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    StepVerifier.create(messageService.saveUserMessageAsync(conversationId, content))
        .expectNextMatches(message -> 
            message.getId().equals(19L) &&
            message.getContent().equals(content) &&
            "user".equals(message.getRole()))
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void shouldSaveUserMessageAsync_WithUnicode() {
    // Given
    Long conversationId = 1L;
    String content = "Unicode测试：测试中文消息";
    
    Message expectedMessage = new Message();
    expectedMessage.setId(20L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("user");
    expectedMessage.setContent(content);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(20L);
          return null;
        })
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    StepVerifier.create(messageService.saveUserMessageAsync(conversationId, content))
        .expectNextMatches(message -> 
            message.getId().equals(20L) &&
            message.getContent().equals(content) &&
            "user".equals(message.getRole()))
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void shouldSaveAiMessageAsync_WithSpecialCharacters() {
    // Given
    Long conversationId = 1L;
    String content = "特殊回复：🌟🔍🚀";
    String thinking = "特殊思考：🚀🔍🌟";
    
    Message expectedMessage = new Message();
    expectedMessage.setId(21L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setThinking(thinking);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(21L);
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
  void shouldSaveAiMessageAsync_WithUnicode() {
    // Given
    Long conversationId = 1L;
    String content = "Unicode回复：测试中文";
    String thinking = "Unicode思考：更多中文";
    
    Message expectedMessage = new Message();
    expectedMessage.setId(22L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setThinking(thinking);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(22L);
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
  void shouldSaveAiMessageWithSearchAsync_WithSpecialCharacters() {
    // Given
    Long conversationId = 1L;
    String content = "特殊回复：🌟🔍🚀";
    String thinking = "特殊思考：🚀🔍🌟";
    List<String> searchResults = Arrays.asList("结果1🌟", "结果2🔍", "结果3🚀");
    
    Message expectedMessage = new Message();
    expectedMessage.setId(23L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setThinking(thinking);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(23L);
          return null;
        })
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    AiMessageSaveRequest request = AiMessageSaveRequest.builder()
      .conversationId(conversationId)
      .content(content)
      .thinking(thinking)
      .searchResults(searchResults)
      .build();
    StepVerifier.create(messageService.saveAiMessageWithSearchAsync(request))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(23L);
        })
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void shouldSaveAiMessageWithSearchAsync_WithUnicode() {
    // Given
    Long conversationId = 1L;
    String content = "Unicode回复：测试中文";
    String thinking = "Unicode思考：更多中文";
    List<String> searchResults = Arrays.asList("结果1：中文", "结果2：更多中文");
    
    Message expectedMessage = new Message();
    expectedMessage.setId(24L);
    expectedMessage.setConversationId(conversationId);
    expectedMessage.setRole("assistant");
    expectedMessage.setContent(content);
    expectedMessage.setThinking(thinking);
    expectedMessage.setCreatedAt(LocalDateTime.now());
    
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(24L);
          return null;
        })
        .when(messageMapper).insert(any(Message.class));

    // When & Then
    AiMessageSaveRequest request = AiMessageSaveRequest.builder()
      .conversationId(conversationId)
      .content(content)
      .thinking(thinking)
      .searchResults(searchResults)
      .build();
    StepVerifier.create(messageService.saveAiMessageWithSearchAsync(request))
        .expectNextMatches(event -> {
            SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
            return "end".equals(event.getType()) && endData.getMessageId().equals(24L);
        })
        .verifyComplete();
        
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void shouldGetConversationHistoryAsync_WithSpecialCharacters() {
    // Given
    Long conversationId = 1L;
    
    Message message1 = new Message();
    message1.setId(1L);
    message1.setConversationId(conversationId);
    message1.setRole("user");
    message1.setContent("特殊字符测试：🌟🔍🚀");
    message1.setCreatedAt(LocalDateTime.now().minusMinutes(5));
    
    Message message2 = new Message();
    message2.setId(2L);
    message2.setConversationId(conversationId);
    message2.setRole("assistant");
    message2.setContent("特殊回复：🚀🔍🌟");
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
  void shouldGetConversationHistoryAsync_WithUnicode() {
    // Given
    Long conversationId = 1L;
    
    Message message1 = new Message();
    message1.setId(1L);
    message1.setConversationId(conversationId);
    message1.setRole("user");
    message1.setContent("Unicode测试：测试中文");
    message1.setCreatedAt(LocalDateTime.now().minusMinutes(5));
    
    Message message2 = new Message();
    message2.setId(2L);
    message2.setConversationId(conversationId);
    message2.setRole("assistant");
    message2.setContent("Unicode回复：更多中文");
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

  // ========== saveMessage DTO方法测试 ==========

  @Test
  void testSaveMessage_WithMessageSaveRequest_BasicFields() {
    // Given
    MessageSaveRequest request = MessageSaveRequest.builder()
        .conversationId(1L)
        .role("user")
        .content("测试消息内容")
        .build();

    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(100L);
          return null;
        })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(request);

    // Then
    assertNotNull(result);
    assertEquals(1L, result.getConversationId());
    assertEquals("user", result.getRole());
    assertEquals("测试消息内容", result.getContent());
    assertNull(result.getThinking());
    assertNull(result.getSearchResults());
    assertEquals(100L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_WithMessageSaveRequest_AllFields() {
    // Given
    MessageSaveRequest request = MessageSaveRequest.builder()
        .conversationId(1L)
        .role("assistant")
        .content("AI回复内容")
        .thinking("思考过程")
        .searchResults("搜索结果")
        .build();

    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(101L);
          return null;
        })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(request);

    // Then
    assertNotNull(result);
    assertEquals(1L, result.getConversationId());
    assertEquals("assistant", result.getRole());
    assertEquals("AI回复内容", result.getContent());
    assertEquals("思考过程", result.getThinking());
    assertEquals("搜索结果", result.getSearchResults());
    assertEquals(101L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_WithMessageSaveRequest_PartialFields() {
    // Given
    MessageSaveRequest request = MessageSaveRequest.builder()
        .conversationId(2L)
        .role("assistant")
        .content("部分字段测试")
        .thinking("有思考")
        // searchResults为null
        .build();

    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(102L);
          return null;
        })
        .when(messageMapper)
        .insert(any(Message.class));

    // When
    Message result = messageService.saveMessage(request);

    // Then
    assertNotNull(result);
    assertEquals(2L, result.getConversationId());
    assertEquals("assistant", result.getRole());
    assertEquals("部分字段测试", result.getContent());
    assertEquals("有思考", result.getThinking());
    assertNull(result.getSearchResults());
    assertEquals(102L, result.getId());
    verify(messageMapper).insert(any(Message.class));
  }

  @Test
  void testSaveMessage_WithMessageSaveRequest_ValidationError() {
    // Given - null request
    MessageSaveRequest request = null;

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(request));
    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_WithMessageSaveRequest_InvalidConversationId() {
    // Given
    MessageSaveRequest request = MessageSaveRequest.builder()
        .conversationId(null)
        .role("user")
        .content("测试内容")
        .build();

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(request));
    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_WithMessageSaveRequest_InvalidRole() {
    // Given
    MessageSaveRequest request = MessageSaveRequest.builder()
        .conversationId(1L)
        .role("") // empty role
        .content("测试内容")
        .build();

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(request));
    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_WithMessageSaveRequest_InvalidContent() {
    // Given
    MessageSaveRequest request = MessageSaveRequest.builder()
        .conversationId(1L)
        .role("user")
        .content("   ") // whitespace content
        .build();

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(request));
    verify(messageMapper, never()).insert(any());
  }

  @Test
  void testSaveMessage_WithMessageSaveRequest_UsingBuilderMethods() {
    // Given - 使用便利方法创建request
    MessageSaveRequest userRequest = MessageSaveRequest.forUser(3L, "用户消息");
    MessageSaveRequest assistantRequest = MessageSaveRequest.forAssistant(3L, "AI回复");
    MessageSaveRequest assistantWithThinkingRequest = MessageSaveRequest
        .forAssistantWithThinking(3L, "AI回复", "思考过程");
    MessageSaveRequest assistantWithSearchRequest = MessageSaveRequest
        .forAssistantWithSearch(3L, "AI回复", "搜索结果");
    MessageSaveRequest completeRequest = MessageSaveRequest
        .forAssistantComplete(3L, "完整AI回复", "思考", "搜索");

    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(200L);
          return null;
        })
        .when(messageMapper)
        .insert(any(Message.class));

    // When & Then - 测试用户消息
    Message userResult = messageService.saveMessage(userRequest);
    assertNotNull(userResult);
    assertEquals("user", userResult.getRole());
    assertEquals("用户消息", userResult.getContent());

    // Reset mock counter
    doAnswer(invocation -> {
          Message message = invocation.getArgument(0);
          message.setId(201L);
          return null;
        })
        .when(messageMapper)
        .insert(any(Message.class));

    // 测试AI回复
    Message assistantResult = messageService.saveMessage(assistantRequest);
    assertNotNull(assistantResult);
    assertEquals("assistant", assistantResult.getRole());
    assertEquals("AI回复", assistantResult.getContent());

    // 验证调用次数
    verify(messageMapper, times(2)).insert(any(Message.class));
  }
}