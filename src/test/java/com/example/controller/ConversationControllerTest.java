package com.example.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.dto.ConversationRequest;
import com.example.entity.Conversation;
import com.example.entity.Message;
import com.example.service.ConversationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ConversationController.class)
@ContextConfiguration(
    classes = {ConversationController.class, com.example.exception.GlobalExceptionHandler.class})
public class ConversationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ConversationService conversationService;

  @MockBean private com.example.service.MessageService messageService;

  @Autowired private ObjectMapper objectMapper;

  private Conversation testConversation;
  private Message testMessage;
  private ConversationRequest conversationRequest;

  @BeforeEach
  void setUp() {
    testConversation = new Conversation();
    testConversation.setId(1L);
    testConversation.setUserId(1L);
    testConversation.setTitle("Test Conversation");
    testConversation.setCreatedAt(LocalDateTime.now());
    testConversation.setUpdatedAt(LocalDateTime.now());

    testMessage = new Message();
    testMessage.setId(1L);
    testMessage.setConversationId(1L);
    testMessage.setRole("user");
    testMessage.setContent("Test message");
    testMessage.setCreatedAt(LocalDateTime.now());

    conversationRequest = new ConversationRequest();
    conversationRequest.setTitle("New Conversation");
  }

  @Test
  void testGetUserConversations_Success() throws Exception {
    // Given
    List<Conversation> conversations = Arrays.asList(testConversation);
    when(conversationService.getUserConversations(1L)).thenReturn(conversations);

    // When & Then
    mockMvc
        .perform(get("/api/conversations?userId=1"))
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
    mockMvc
        .perform(get("/api/conversations?userId=0"))
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
    mockMvc
        .perform(get("/api/conversations?userId=1"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("系统运行异常: Database error"));
  }

  @Test
  void testCreateConversation_Success() throws Exception {
    // Given
    when(conversationService.createConversation(1L, "New Conversation"))
        .thenReturn(testConversation);

    // When & Then
    mockMvc
        .perform(
            post("/api/conversations?userId=1")
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
    mockMvc
        .perform(
            post("/api/conversations?userId=1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conversationRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("对话标题不能为空"));
  }

  @Test
  void testCreateConversation_InvalidUserId() throws Exception {
    // When & Then
    mockMvc
        .perform(
            post("/api/conversations?userId=0")
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
    mockMvc
        .perform(delete("/api/conversations/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("删除成功"));

    verify(conversationService).deleteConversation(1L);
  }

  @Test
  void testDeleteConversation_InvalidId() throws Exception {
    // When & Then
    mockMvc
        .perform(delete("/api/conversations/0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("对话ID无效"));
  }

  @Test
  void testDeleteConversation_ServiceException() throws Exception {
    // Given
    doThrow(new RuntimeException("Database error"))
        .when(conversationService)
        .deleteConversation(1L);

    // When & Then
    mockMvc
        .perform(delete("/api/conversations/1"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("系统运行异常: Database error"));
  }

  @Test
  void testGetConversationMessages_Success() throws Exception {
    // Given
    List<Message> messages = Arrays.asList(testMessage);
    when(conversationService.getConversationMessages(1L)).thenReturn(messages);

    // When & Then
    mockMvc
        .perform(get("/api/conversations/1/messages"))
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
    mockMvc
        .perform(get("/api/conversations/0/messages"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("对话ID无效"));
  }

  @Test
  void testGetConversationMessages_ServiceException() throws Exception {
    // Given
    when(conversationService.getConversationMessages(1L))
        .thenThrow(new RuntimeException("Database error"));

    // When & Then
    mockMvc
        .perform(get("/api/conversations/1/messages"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("系统运行异常: Database error"));
  }

  @Test
  void testCreateConversation_NullTitle() throws Exception {
    // Given
    conversationRequest.setTitle(null);

    // When & Then
    mockMvc
        .perform(
            post("/api/conversations?userId=1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conversationRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("对话标题不能为空"));
  }

  @Test
  void testCreateConversation_WhitespaceTitle() throws Exception {
    // Given
    conversationRequest.setTitle("   ");

    // When & Then
    mockMvc
        .perform(
            post("/api/conversations?userId=1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conversationRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("对话标题不能为空"));
  }

  @Test
  void testGetUserConversations_EmptyResult() throws Exception {
    // Given
    when(conversationService.getUserConversations(1L)).thenReturn(Arrays.asList());

    // When & Then
    mockMvc
        .perform(get("/api/conversations?userId=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data").isEmpty());

    verify(conversationService).getUserConversations(1L);
  }

  @Test
  void testDeleteConversation_NegativeId() throws Exception {
    // When & Then
    mockMvc
        .perform(delete("/api/conversations/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("对话ID无效"));
  }

  @Test
  void testGetUserConversations_NegativeUserId() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/api/conversations?userId=-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("用户ID无效"));
  }

  @Test
  void testCreateConversation_NegativeUserId() throws Exception {
    // When & Then
    mockMvc
        .perform(
            post("/api/conversations?userId=-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conversationRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("用户ID无效"));
  }

  @Test
  void testGetConversation_Success() throws Exception {
    // Given
    when(conversationService.getConversationById(1L)).thenReturn(testConversation);

    // When & Then
    mockMvc
        .perform(get("/api/conversations/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.title").value("Test Conversation"));

    verify(conversationService).getConversationById(1L);
  }

  @Test
  void testGetConversation_ServiceException() throws Exception {
    // Given
    when(conversationService.getConversationById(1L))
        .thenThrow(new RuntimeException("Database error"));

    // When & Then
    mockMvc
        .perform(get("/api/conversations/1"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("系统运行异常: Database error"));
  }

  @Test
  void testSendMessage_Success() throws Exception {
    // Given
    com.example.dto.MessageRequest messageRequest = new com.example.dto.MessageRequest();
    messageRequest.setContent("Test message content");

    when(messageService.saveMessage(1L, "user", "Test message content")).thenReturn(testMessage);

    // When & Then
    mockMvc
        .perform(
            post("/api/conversations/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("消息发送成功"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.content").value("Test message"));

    verify(messageService).saveMessage(1L, "user", "Test message content");
  }

  @Test
  void testSendMessage_ServiceException() throws Exception {
    // Given
    com.example.dto.MessageRequest messageRequest = new com.example.dto.MessageRequest();
    messageRequest.setContent("Test message content");

    when(messageService.saveMessage(1L, "user", "Test message content"))
        .thenThrow(new RuntimeException("Database error"));

    // When & Then
    mockMvc
        .perform(
            post("/api/conversations/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("系统运行异常: Database error"));
  }

  @Test
  void testGetUserConversations_NullUserId() throws Exception {
    // When & Then - 当userId参数缺失时，Spring会返回500错误
    mockMvc.perform(get("/api/conversations")).andExpect(status().isInternalServerError());
  }

  @Test
  void testCreateConversation_NullUserId() throws Exception {
    // When & Then - 当userId参数缺失时，Spring会返回500错误
    mockMvc
        .perform(
            post("/api/conversations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conversationRequest)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void testGetConversationMessages_NegativeId() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/api/conversations/-1/messages"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("对话ID无效"));
  }

  // 直接测试Controller方法以覆盖null值分支
  @Test
  void testDirectCall_GetConversations_NullUserId() {
    // Given
    ConversationController controller = new ConversationController();
    // 使用反射设置conversationService
    try {
      java.lang.reflect.Field field =
          ConversationController.class.getDeclaredField("conversationService");
      field.setAccessible(true);
      field.set(controller, conversationService);
    } catch (Exception e) {
      // 忽略反射异常
    }

    // When & Then
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          controller.getConversations(null);
        });
  }

  @Test
  void testDirectCall_CreateConversation_NullUserId() {
    // Given
    ConversationController controller = new ConversationController();
    // 使用反射设置conversationService
    try {
      java.lang.reflect.Field field =
          ConversationController.class.getDeclaredField("conversationService");
      field.setAccessible(true);
      field.set(controller, conversationService);
    } catch (Exception e) {
      // 忽略反射异常
    }

    // When & Then
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          controller.createConversation(null, conversationRequest);
        });
  }

  @Test
  void testDirectCall_DeleteConversation_NullId() {
    // Given
    ConversationController controller = new ConversationController();
    // 使用反射设置conversationService
    try {
      java.lang.reflect.Field field =
          ConversationController.class.getDeclaredField("conversationService");
      field.setAccessible(true);
      field.set(controller, conversationService);
    } catch (Exception e) {
      // 忽略反射异常
    }

    // When & Then
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          controller.deleteConversation(null);
        });
  }

  @Test
  void testDirectCall_GetMessages_NullId() {
    // Given
    ConversationController controller = new ConversationController();
    // 使用反射设置conversationService
    try {
      java.lang.reflect.Field field =
          ConversationController.class.getDeclaredField("conversationService");
      field.setAccessible(true);
      field.set(controller, conversationService);
    } catch (Exception e) {
      // 忽略反射异常
    }

    // When & Then
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          controller.getMessages(null);
        });
  }
}
