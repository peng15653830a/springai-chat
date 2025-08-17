package com.example.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.dto.LoginRequest;
import com.example.entity.User;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = {UserController.class})
public class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserService userService;

  @Autowired private ObjectMapper objectMapper;

  private User testUser;
  private LoginRequest loginRequest;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setNickname("TestUser");
    testUser.setCreatedAt(LocalDateTime.now());

    loginRequest = new LoginRequest();
    loginRequest.setUsername("testuser");
    loginRequest.setNickname("TestUser");
  }

  @Test
  void testLogin_Success() throws Exception {
    // Given
    when(userService.loginOrCreate("testuser", "TestUser")).thenReturn(testUser);

    // When & Then
    mockMvc
        .perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.nickname").value("TestUser"))
        .andExpect(jsonPath("$.data.username").value("testuser"));

    verify(userService).loginOrCreate("testuser", "TestUser");
  }

  @Test
  void testLogin_EmptyUsername() throws Exception {
    // Given
    loginRequest.setUsername("");
    when(userService.loginOrCreate("", "TestUser")).thenReturn(testUser);

    // When & Then
    mockMvc
        .perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(userService).loginOrCreate("", "TestUser");
  }

  @Test
  void testLogin_EmptyNickname() throws Exception {
    // Given
    loginRequest.setNickname("");
    when(userService.loginOrCreate("testuser", "")).thenReturn(testUser);

    // When & Then
    mockMvc
        .perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(userService).loginOrCreate("testuser", "");
  }

  @Test
  void testLogin_ServiceException() throws Exception {
    // Given
    when(userService.loginOrCreate("testuser", "TestUser"))
        .thenThrow(new RuntimeException("Database error"));

    // When & Then
    mockMvc
        .perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("登录失败: Database error"));

    verify(userService).loginOrCreate("testuser", "TestUser");
  }

  @Test
  void testGetProfile_Success() throws Exception {
    // Given
    when(userService.getUserById(1L)).thenReturn(testUser);

    // When & Then
    mockMvc
        .perform(get("/api/users/profile/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.username").value("testuser"))
        .andExpect(jsonPath("$.data.nickname").value("TestUser"));

    verify(userService).getUserById(1L);
  }

  @Test
  void testGetProfile_UserNotFound() throws Exception {
    // Given
    when(userService.getUserById(999L)).thenReturn(null);

    // When & Then
    mockMvc
        .perform(get("/api/users/profile/999"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("用户不存在"));

    verify(userService).getUserById(999L);
  }

  @Test
  void testGetProfile_ServiceException() throws Exception {
    // Given
    when(userService.getUserById(1L)).thenThrow(new RuntimeException("Database error"));

    // When & Then
    mockMvc
        .perform(get("/api/users/profile/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("获取用户信息失败: Database error"));

    verify(userService).getUserById(1L);
  }

  @Test
  void testLogin_NullUsername() throws Exception {
    // Given
    loginRequest.setUsername(null);
    when(userService.loginOrCreate(null, "TestUser")).thenReturn(testUser);

    // When & Then
    mockMvc
        .perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(userService).loginOrCreate(null, "TestUser");
  }

  @Test
  void testLogin_NullNickname() throws Exception {
    // Given
    loginRequest.setNickname(null);
    when(userService.loginOrCreate("testuser", null)).thenReturn(testUser);

    // When & Then
    mockMvc
        .perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(userService).loginOrCreate("testuser", null);
  }

  @Test
  void testLogin_BothNull() throws Exception {
    // Given
    loginRequest.setUsername(null);
    loginRequest.setNickname(null);
    when(userService.loginOrCreate(null, null)).thenReturn(testUser);

    // When & Then
    mockMvc
        .perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(userService).loginOrCreate(null, null);
  }

  @Test
  void testLogin_SpecialCharacters() throws Exception {
    // Given
    loginRequest.setUsername("user@123");
    loginRequest.setNickname("用户#123");
    when(userService.loginOrCreate("user@123", "用户#123")).thenReturn(testUser);

    // When & Then
    mockMvc
        .perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(userService).loginOrCreate("user@123", "用户#123");
  }

  @Test
  void testGetProfile_ZeroId() throws Exception {
    // Given
    when(userService.getUserById(0L)).thenReturn(null);

    // When & Then
    mockMvc
        .perform(get("/api/users/profile/0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("用户不存在"));

    verify(userService).getUserById(0L);
  }

  @Test
  void testGetProfile_NegativeId() throws Exception {
    // Given
    when(userService.getUserById(-1L)).thenReturn(null);

    // When & Then
    mockMvc
        .perform(get("/api/users/profile/-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("用户不存在"));

    verify(userService).getUserById(-1L);
  }

  @Test
  void testLogin_LongUsername() throws Exception {
    // Given
    StringBuilder longUsername = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      longUsername.append("user");
    }
    loginRequest.setUsername(longUsername.toString());
    when(userService.loginOrCreate(longUsername.toString(), "TestUser")).thenReturn(testUser);

    // When & Then
    mockMvc
        .perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(userService).loginOrCreate(longUsername.toString(), "TestUser");
  }
}
