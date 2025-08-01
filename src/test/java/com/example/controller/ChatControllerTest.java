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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ChatController.class)
@ContextConfiguration(classes = {ChatController.class, com.example.exception.GlobalExceptionHandler.class})
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
        testMessage.setCreatedAt(LocalDateTime.now());

        messageRequest = new MessageRequest();
        messageRequest.setContent("Test message");
        messageRequest.setSearchEnabled(true);
    }

    @Test
    void testSendMessage_Success() throws Exception {
        // Given
        when(aiChatService.sendMessage(anyLong(), anyString(), anyBoolean())).thenReturn(testMessage);

        // When & Then
        mockMvc.perform(post("/api/chat/conversations/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("消息发送成功"));

        verify(aiChatService).sendMessage(1L, "Test message", true);
    }

    @Test
    void testSendMessage_WithSearch() throws Exception {
        // Given
        when(aiChatService.sendMessage(anyLong(), anyString(), anyBoolean())).thenReturn(testMessage);

        // When & Then
        mockMvc.perform(post("/api/chat/conversations/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(aiChatService).sendMessage(1L, "Test message", true);
    }

    @Test
    void testSendMessage_EmptyContent() throws Exception {
        // Given
        messageRequest.setContent("");
        when(aiChatService.sendMessage(anyLong(), anyString(), anyBoolean()))
                .thenThrow(new IllegalArgumentException("消息内容不能为空"));

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
        // Given
        when(aiChatService.sendMessage(anyLong(), anyString(), anyBoolean()))
                .thenThrow(new IllegalArgumentException("会话ID无效"));

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
        when(aiChatService.sendMessage(anyLong(), anyString(), anyBoolean()))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/api/chat/conversations/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("系统运行异常: Database error"));
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
        // Given
        when(sseEmitterManager.createEmitter(anyLong()))
                .thenThrow(new IllegalArgumentException("会话ID无效"));

        // When & Then
        mockMvc.perform(get("/api/chat/stream/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("会话ID无效"));
    }

    @Test
    void testSendMessage_NullContent() throws Exception {
        // Given
        messageRequest.setContent(null);
        when(aiChatService.sendMessage(anyLong(), any(), anyBoolean()))
                .thenThrow(new IllegalArgumentException("消息内容不能为空"));

        // When & Then
        mockMvc.perform(post("/api/chat/conversations/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("消息内容不能为空"));
    }

    @Test
    void testSendMessage_WhitespaceContent() throws Exception {
        // Given
        messageRequest.setContent("   ");
        when(aiChatService.sendMessage(anyLong(), anyString(), anyBoolean()))
                .thenThrow(new IllegalArgumentException("消息内容不能为空"));

        // When & Then
        mockMvc.perform(post("/api/chat/conversations/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("消息内容不能为空"));
    }

    @Test
    void testSendMessage_WithoutSearchEnabled() throws Exception {
        // Given
        messageRequest.setSearchEnabled(false);
        when(aiChatService.sendMessage(anyLong(), anyString(), anyBoolean())).thenReturn(testMessage);

        // When & Then
        mockMvc.perform(post("/api/chat/conversations/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(aiChatService).sendMessage(1L, "Test message", false);
    }

    @Test
    void testSendMessage_LongContent() throws Exception {
        // Given
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longContent.append("这是一段很长的内容 ");
        }
        messageRequest.setContent(longContent.toString());
        when(aiChatService.sendMessage(anyLong(), anyString(), anyBoolean())).thenReturn(testMessage);

        // When & Then
        mockMvc.perform(post("/api/chat/conversations/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(aiChatService).sendMessage(eq(1L), eq(longContent.toString()), eq(true));
    }

    @Test
    void testSendMessage_SpecialCharacters() throws Exception {
        // Given
        String specialContent = "特殊字符: @#$%^&*()! 和 emoji: 😀🎉🚀\n多行\n内容";
        messageRequest.setContent(specialContent);
        when(aiChatService.sendMessage(anyLong(), anyString(), anyBoolean())).thenReturn(testMessage);

        // When & Then
        mockMvc.perform(post("/api/chat/conversations/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(aiChatService).sendMessage(eq(1L), eq(specialContent), eq(true));
    }

    @Test
    void testGetSseStream_NegativeConversationId() throws Exception {
        // Given
        when(sseEmitterManager.createEmitter(anyLong()))
                .thenThrow(new IllegalArgumentException("会话ID无效"));

        // When & Then
        mockMvc.perform(get("/api/chat/stream/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("会话ID无效"));
    }

    @Test
    void testGetSseStream_ServiceException() throws Exception {
        // Given
        when(sseEmitterManager.createEmitter(anyLong()))
                .thenThrow(new RuntimeException("Internal server error"));

        // When & Then
        mockMvc.perform(get("/api/chat/stream/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("系统运行异常: Internal server error"));
    }
}