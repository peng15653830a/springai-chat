package com.example.controller;

import com.example.dto.ConversationRequest;
import com.example.entity.Conversation;
import com.example.entity.Message;
import com.example.service.ConversationService;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ConversationController.class)
@ContextConfiguration(classes = {ConversationController.class})
public class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConversationService conversationService;
    
    @MockBean
    private com.example.service.MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    private Conversation testConversation;
    private Message testMessage;
    private ConversationRequest conversationRequest;

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

        conversationRequest = new ConversationRequest();
        conversationRequest.setTitle("New Conversation");
    }

    @Test
    void testGetUserConversations_Success() throws Exception {
        // Given
        List<Conversation> conversations = Arrays.asList(testConversation);
        when(conversationService.getUserConversations(1L)).thenReturn(conversations);

        // When & Then
        mockMvc.perform(get("/api/conversations?userId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Test Conversation"));

        verify(conversationService).getUserConversations(1L);
    }

    @Test
    void testGetUserConversations_InvalidUserId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/conversations?userId=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户ID无效"));
    }

    @Test
    void testGetUserConversations_ServiceException() throws Exception {
        // Given
        when(conversationService.getUserConversations(1L))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/conversations?userId=1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("获取对话列表失败: Database error"));
    }

    @Test
    void testCreateConversation_Success() throws Exception {
        // Given
        when(conversationService.createConversation(1L, "New Conversation")).thenReturn(testConversation);

        // When & Then
        mockMvc.perform(post("/api/conversations?userId=1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conversationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Conversation"));

        verify(conversationService).createConversation(1L, "New Conversation");
    }

    @Test
    void testCreateConversation_EmptyTitle() throws Exception {
        // Given
        conversationRequest.setTitle("");

        // When & Then
        mockMvc.perform(post("/api/conversations?userId=1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conversationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("对话标题不能为空"));
    }

    @Test
    void testCreateConversation_InvalidUserId() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/conversations?userId=0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conversationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户ID无效"));
    }

    @Test
    void testDeleteConversation_Success() throws Exception {
        // Given
        doNothing().when(conversationService).deleteConversation(1L);

        // When & Then
        mockMvc.perform(delete("/api/conversations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(conversationService).deleteConversation(1L);
    }

    @Test
    void testDeleteConversation_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/conversations/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("对话ID无效"));
    }

    @Test
    void testDeleteConversation_ServiceException() throws Exception {
        // Given
        doThrow(new RuntimeException("Database error")).when(conversationService).deleteConversation(1L);

        // When & Then
        mockMvc.perform(delete("/api/conversations/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("删除失败: Database error"));
    }

    @Test
    void testGetConversationMessages_Success() throws Exception {
        // Given
        List<Message> messages = Arrays.asList(testMessage);
        when(conversationService.getConversationMessages(1L)).thenReturn(messages);

        // When & Then
        mockMvc.perform(get("/api/conversations/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].content").value("Test message"));

        verify(conversationService).getConversationMessages(1L);
    }

    @Test
    void testGetConversationMessages_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/conversations/0/messages"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("对话ID无效"));
    }
}