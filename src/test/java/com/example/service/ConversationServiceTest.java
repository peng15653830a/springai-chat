package com.example.service;

import com.example.entity.Conversation;
import com.example.entity.Message;
import com.example.mapper.ConversationMapper;
import com.example.mapper.MessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConversationServiceTest {

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private ConversationService conversationService;

    private Conversation testConversation;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        testConversation = new Conversation();
        testConversation.setId(1L);
        testConversation.setUserId(1L);
        testConversation.setTitle("Test Conversation");
        testConversation.setCreatedAt(new Date());
        testConversation.setUpdatedAt(new Date());

        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setConversationId(1L);
        testMessage.setRole("user");
        testMessage.setContent("Test message");
        testMessage.setCreatedAt(new Date());
    }

    @Test
    void testGetUserConversations_Success() {
        // Given
        List<Conversation> conversations = Arrays.asList(testConversation);
        when(conversationMapper.selectByUserId(1L)).thenReturn(conversations);

        // When
        List<Conversation> result = conversationService.getUserConversations(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Conversation", result.get(0).getTitle());
        verify(conversationMapper).selectByUserId(1L);
    }

    @Test
    void testCreateConversation_Success() {
        // Given
        doNothing().when(conversationMapper).insert(any(Conversation.class));

        // When
        Conversation result = conversationService.createConversation(1L, "New Conversation");

        // Then
        assertNotNull(result);
        assertEquals("New Conversation", result.getTitle());
        assertEquals(1L, result.getUserId());
        verify(conversationMapper).insert(any(Conversation.class));
    }

    @Test
    void testCreateConversation_NullTitle() {
        // Given
        doNothing().when(conversationMapper).insert(any(Conversation.class));

        // When
        Conversation result = conversationService.createConversation(1L, null);

        // Then
        assertNotNull(result);
        assertEquals("新对话", result.getTitle());
        verify(conversationMapper).insert(any(Conversation.class));
    }

    @Test
    void testCreateConversation_EmptyTitle() {
        // Given
        doNothing().when(conversationMapper).insert(any(Conversation.class));

        // When
        Conversation result = conversationService.createConversation(1L, "");

        // Then
        assertNotNull(result);
        assertEquals("新对话", result.getTitle());
        verify(conversationMapper).insert(any(Conversation.class));
    }

    @Test
    void testDeleteConversation_Success() {
        // Given
        doNothing().when(messageMapper).deleteByConversationId(1L);
        doNothing().when(conversationMapper).deleteById(1L);

        // When
        conversationService.deleteConversation(1L);

        // Then
        verify(messageMapper).deleteByConversationId(1L);
        verify(conversationMapper).deleteById(1L);
    }

    @Test
    void testGetConversationMessages_Success() {
        // Given
        List<Message> messages = Arrays.asList(testMessage);
        when(messageMapper.selectByConversationId(1L)).thenReturn(messages);

        // When
        List<Message> result = conversationService.getConversationMessages(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test message", result.get(0).getContent());
        verify(messageMapper).selectByConversationId(1L);
    }

    @Test
    void testGetRecentMessages_Success() {
        // Given
        List<Message> messages = Arrays.asList(testMessage);
        when(messageMapper.selectRecentMessages(1L, 10)).thenReturn(messages);

        // When
        List<Message> result = conversationService.getRecentMessages(1L, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test message", result.get(0).getContent());
        verify(messageMapper).selectRecentMessages(1L, 10);
    }

    @Test
    void testGetConversationById_Success() {
        // Given
        when(conversationMapper.selectById(1L)).thenReturn(testConversation);

        // When
        Conversation result = conversationService.getConversationById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Conversation", result.getTitle());
        verify(conversationMapper).selectById(1L);
    }

    @Test
    void testGetConversationById_NotFound() {
        // Given
        when(conversationMapper.selectById(999L)).thenReturn(null);

        // When
        Conversation result = conversationService.getConversationById(999L);

        // Then
        assertNull(result);
        verify(conversationMapper).selectById(999L);
    }

    @Test
    void testUpdateConversationTitle_Success() {
        // Given
        doNothing().when(conversationMapper).updateById(any(Conversation.class));

        // When
        conversationService.updateConversationTitle(1L, "Updated Title");

        // Then
        verify(conversationMapper).updateById(argThat(conversation -> 
            conversation.getId().equals(1L) && 
            conversation.getTitle().equals("Updated Title")
        ));
    }

    @Test
    void testGetRecentConversations_Success() {
        // Given
        List<Conversation> conversations = Arrays.asList(testConversation);
        when(conversationMapper.selectRecentByUserId(1L, 5)).thenReturn(conversations);

        // When
        List<Conversation> result = conversationService.getRecentConversations(1L, 5);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(conversationMapper).selectRecentByUserId(1L, 5);
    }
}