package com.example.service;

import com.example.entity.Message;
import com.example.mapper.MessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

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
        testMessage.setContent("Test message");
        testMessage.setCreatedAt(new Date());
    }

    @Test
    void testSaveMessage_Success() {
        // Given
        doNothing().when(messageMapper).insert(any(Message.class));

        // When
        Message result = messageService.saveMessage(1L, "user", "Test message");

        // Then
        assertNotNull(result);
        assertEquals("Test message", result.getContent());
        assertEquals("user", result.getRole());
        assertEquals(1L, result.getConversationId());
        verify(messageMapper).insert(any(Message.class));
    }

    @Test
    void testSaveMessage_WithThinking() {
        // Given
        doNothing().when(messageMapper).insert(any(Message.class));

        // When
        Message result = messageService.saveMessage(1L, "assistant", "Response", "Thinking", "Search results");

        // Then
        assertNotNull(result);
        assertEquals("Response", result.getContent());
        assertEquals("assistant", result.getRole());
        assertEquals("Thinking", result.getThinking());
        assertEquals("Search results", result.getSearchResults());
        verify(messageMapper).insert(argThat(message -> 
            message.getThinking() != null && 
            message.getThinking().equals("Thinking") &&
            message.getSearchResults() != null &&
            message.getSearchResults().equals("Search results")
        ));
    }

    @Test
    void testSaveMessage_WithSearchResults() {
        // Given
        doNothing().when(messageMapper).insert(any(Message.class));

        // When
        Message result = messageService.saveMessage(1L, "assistant", "Response", "Search results");

        // Then
        assertNotNull(result);
        assertEquals("Response", result.getContent());
        assertEquals("assistant", result.getRole());
        assertEquals("Search results", result.getSearchResults());
        verify(messageMapper).insert(any(Message.class));
    }

    @Test
    void testGetMessageById_Success() {
        // Given
        when(messageMapper.selectById(1L)).thenReturn(testMessage);

        // When
        Message result = messageService.getMessageById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test message", result.getContent());
        verify(messageMapper).selectById(1L);
    }

    @Test
    void testGetMessageById_NotFound() {
        // Given
        when(messageMapper.selectById(999L)).thenReturn(null);

        // When
        Message result = messageService.getMessageById(999L);

        // Then
        assertNull(result);
        verify(messageMapper).selectById(999L);
    }

    @Test
    void testSaveMessage_NullThinking() {
        // Given
        doNothing().when(messageMapper).insert(any(Message.class));

        // When
        Message result = messageService.saveMessage(1L, "assistant", "Response", null, "Search");

        // Then
        assertNotNull(result);
        assertNull(result.getThinking());
        assertEquals("Search", result.getSearchResults());
        verify(messageMapper).insert(any(Message.class));
    }

    @Test
    void testSaveMessage_EmptyContent() {
        // Given
        doNothing().when(messageMapper).insert(any(Message.class));

        // When
        Message result = messageService.saveMessage(1L, "user", "");

        // Then
        assertNotNull(result);
        assertEquals("", result.getContent());
        verify(messageMapper).insert(any(Message.class));
    }
}