package com.example.service;

import com.example.entity.Message;
import com.example.mapper.MessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageService messageService;

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
        
        doAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            message.setId(1L);
            return null;
        }).when(messageMapper).insert(any(Message.class));

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
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(null, "user", "内容"));
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(0L, "user", "内容"));
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(-1L, "user", "内容"));
        
        verify(messageMapper, never()).insert(any());
    }

    @Test
    void testSaveMessage_ThreeParams_InvalidRole() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(1L, null, "内容"));
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(1L, "", "内容"));
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(1L, "   ", "内容"));
        
        verify(messageMapper, never()).insert(any());
    }

    @Test
    void testSaveMessage_ThreeParams_InvalidContent() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(1L, "user", null));
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(1L, "user", ""));
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(1L, "user", "   "));
        
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
        
        doAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            message.setId(2L);
            return null;
        }).when(messageMapper).insert(any(Message.class));

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
        
        doAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            message.setId(3L);
            return null;
        }).when(messageMapper).insert(any(Message.class));

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
        
        doAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            message.setId(4L);
            return null;
        }).when(messageMapper).insert(any(Message.class));

        // When
        Message result = messageService.saveMessage(conversationId, role, content, thinking, searchResults);

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
        
        doAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            message.setId(5L);
            return null;
        }).when(messageMapper).insert(any(Message.class));

        // When
        Message result = messageService.saveMessage(conversationId, role, content, thinking, searchResults);

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
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(null, "user", "内容", "思考", "搜索"));
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(0L, "user", "内容", "思考", "搜索"));
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(-1L, "user", "内容", "思考", "搜索"));
        
        verify(messageMapper, never()).insert(any());
    }

    @Test
    void testSaveMessage_FiveParams_InvalidRole() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(1L, null, "内容", "思考", "搜索"));
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(1L, "", "内容", "思考", "搜索"));
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(1L, "   ", "内容", "思考", "搜索"));
        
        verify(messageMapper, never()).insert(any());
    }

    @Test
    void testSaveMessage_FiveParams_InvalidContent() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(1L, "user", null, "思考", "搜索"));
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.saveMessage(1L, "user", "", "思考", "搜索"));
        assertThrows(IllegalArgumentException.class, 
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
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.getMessagesByConversationId(null));
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.getMessagesByConversationId(0L));
        assertThrows(IllegalArgumentException.class, 
            () -> messageService.getMessagesByConversationId(-1L));
        
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
        
        doAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            message.setId(6L);
            return null;
        }).when(messageMapper).insert(any(Message.class));

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
        
        doAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            message.setId(7L);
            return null;
        }).when(messageMapper).insert(any(Message.class));

        // When
        Message result = messageService.saveMessage(conversationId, role, content, thinking, searchResults);

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
        
        doAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            message.setId(8L);
            return null;
        }).when(messageMapper).insert(any(Message.class));

        // When
        Message result = messageService.saveMessage(conversationId, role, content, thinking, searchResults);

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
}