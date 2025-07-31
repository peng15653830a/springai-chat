package com.example.controller;

import com.example.dto.MessageRequest;
import com.example.entity.Message;
import com.example.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ChatController.class)
@ContextConfiguration(classes = {ChatController.class})
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConversationService conversationService;

    @MockBean
    private MessageService messageService;

    @MockBean
    private AiChatService aiChatService;

    @MockBean
    private SearchService searchService;

    @MockBean
    private SseEmitterManager sseEmitterManager;

    @Autowired
    private ObjectMapper objectMapper;

    private Message testMessage;
    private MessageRequest messageRequest;

    @BeforeEach
    void setUp() {
        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setConversationId(1L);
        testMessage.setRole("user");
        testMessage.setContent("Test message");
        testMessage.setCreatedAt(new Date());

        messageRequest = new MessageRequest();
        messageRequest.setContent("Test message");
        messageRequest.setSearchEnabled(true);
    }

    @Test
    void testSendMessage_Success() throws Exception {
        // Given
        when(messageService.saveMessage(anyLong(), anyString(), anyString())).thenReturn(testMessage);
        when(conversationService.getRecentMessages(anyLong(), anyInt())).thenReturn(Arrays.asList(testMessage));
        when(aiChatService.chatWithAI(anyString(), anyList())).thenReturn(new AiResponse("AI response", null));
        when(aiChatService.splitResponseForStreaming(anyString())).thenReturn(Arrays.asList("AI", " response"));

        // When & Then
        mockMvc.perform(post("/api/chat/conversations/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("消息发送成功"));

        verify(messageService).saveMessage(1L, "user", "Test message");
        verify(conversationService).getRecentMessages(1L, 10);
        verify(aiChatService).chatWithAI(anyString(), anyList());
    }

    @Test
    void testSendMessage_WithSearch() throws Exception {
        // Given
        when(messageService.saveMessage(anyLong(), anyString(), anyString())).thenReturn(testMessage);
        when(conversationService.getRecentMessages(anyLong(), anyInt())).thenReturn(Arrays.asList(testMessage));
        when(searchService.shouldSearch(anyString())).thenReturn(true);
        when(searchService.searchMetaso(anyString())).thenReturn(java.util.Arrays.asList());
        when(searchService.formatSearchResults(anyList())).thenReturn("搜索结果");
        when(aiChatService.chatWithAI(anyString(), anyList())).thenReturn(new AiResponse("AI response", null));
        when(aiChatService.splitResponseForStreaming(anyString())).thenReturn(Arrays.asList("AI", " response"));

        // When & Then
        mockMvc.perform(post("/api/chat/conversations/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(searchService).shouldSearch("Test message");
        verify(searchService).searchMetaso("Test message");
    }

    @Test
    void testSendMessage_EmptyContent() throws Exception {
        // Given
        messageRequest.setContent("");

        // When & Then
        mockMvc.perform(post("/api/chat/conversations/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("消息内容不能为空"));
    }

    @Test
    void testSendMessage_InvalidConversationId() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/chat/conversations/0/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("会话ID无效"));
    }

    @Test
    void testSendMessage_ServiceException() throws Exception {
        // Given
        when(messageService.saveMessage(anyLong(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/api/chat/conversations/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("发送消息失败: Database error"));
    }

    @Test
    void testGetSseStream() throws Exception {
        // Given
        SseEmitter mockEmitter = mock(SseEmitter.class);
        when(sseEmitterManager.createEmitter(anyLong())).thenReturn(mockEmitter);

        // When & Then
        mockMvc.perform(get("/api/chat/stream/1"))
                .andExpect(status().isOk());

        verify(sseEmitterManager).createEmitter(1L);
    }

    @Test
    void testGetSseStream_InvalidConversationId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/chat/stream/0"))
                .andExpect(status().isBadRequest());
    }
}