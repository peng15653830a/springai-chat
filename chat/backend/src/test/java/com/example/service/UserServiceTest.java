package com.example.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.impl.UserServiceImpl;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserMapper userMapper;

  @InjectMocks private UserServiceImpl userService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setNickname("测试用户");
    testUser.setCreatedAt(LocalDateTime.now());
    testUser.setUpdatedAt(LocalDateTime.now());
  }

  // ========== createUser 测试 ==========

  @Test
  void testCreateUser_Success() {
    // Given
    String username = "newuser";
    String nickname = "新用户";

    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(2L);
              return null;
            })
        .when(userMapper)
        .insert(any(User.class));

    // When
    User result = userService.createUser(username, nickname);

    // Then
    assertNotNull(result);
    assertEquals(username, result.getUsername());
    assertEquals(nickname, result.getNickname());
    assertEquals(2L, result.getId());
    verify(userMapper).insert(any(User.class));
  }

  @Test
  void testCreateUser_WithNullValues() {
    // Given
    String username = null;
    String nickname = null;

    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(3L);
              return null;
            })
        .when(userMapper)
        .insert(any(User.class));

    // When
    User result = userService.createUser(username, nickname);

    // Then
    assertNotNull(result);
    assertNull(result.getUsername());
    assertNull(result.getNickname());
    assertEquals(3L, result.getId());
    verify(userMapper).insert(any(User.class));
  }

  @Test
  void testCreateUser_WithEmptyValues() {
    // Given
    String username = "";
    String nickname = "";

    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(4L);
              return null;
            })
        .when(userMapper)
        .insert(any(User.class));

    // When
    User result = userService.createUser(username, nickname);

    // Then
    assertNotNull(result);
    assertEquals("", result.getUsername());
    assertEquals("", result.getNickname());
    assertEquals(4L, result.getId());
    verify(userMapper).insert(any(User.class));
  }

  @Test
  void testCreateUser_WithSpecialCharacters() {
    // Given
    String username = "user🌟🔍🚀";
    String nickname = "测试用户🌟";

    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(10L);
              return null;
            })
        .when(userMapper)
        .insert(any(User.class));

    // When
    User result = userService.createUser(username, nickname);

    // Then
    assertNotNull(result);
    assertEquals(username, result.getUsername());
    assertEquals(nickname, result.getNickname());
    assertEquals(10L, result.getId());
    verify(userMapper).insert(any(User.class));
  }

  @Test
  void testCreateUser_WithLongValues() {
    // Given
    StringBuilder longUsername = new StringBuilder();
    StringBuilder longNickname = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longUsername.append("longusername");
      longNickname.append("longnickname");
    }
    String username = longUsername.toString();
    String nickname = longNickname.toString();

    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(11L);
              return null;
            })
        .when(userMapper)
        .insert(any(User.class));

    // When
    User result = userService.createUser(username, nickname);

    // Then
    assertNotNull(result);
    assertEquals(username, result.getUsername());
    assertEquals(nickname, result.getNickname());
    assertEquals(11L, result.getId());
    verify(userMapper).insert(any(User.class));
  }

  @Test
  void testCreateUser_WithUnicodeCharacters() {
    // Given
    String username = "测试用户";
    String nickname = "用户昵称";

    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(12L);
              return null;
            })
        .when(userMapper)
        .insert(any(User.class));

    // When
    User result = userService.createUser(username, nickname);

    // Then
    assertNotNull(result);
    assertEquals(username, result.getUsername());
    assertEquals(nickname, result.getNickname());
    assertEquals(12L, result.getId());
    verify(userMapper).insert(any(User.class));
  }

  @Test
  void testCreateUser_WithWhitespace() {
    // Given
    String username = "  username  ";
    String nickname = "  nickname  ";

    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(16L);
              return null;
            })
        .when(userMapper)
        .insert(any(User.class));

    // When
    User result = userService.createUser(username, nickname);

    // Then
    assertNotNull(result);
    assertEquals(username, result.getUsername());
    assertEquals(nickname, result.getNickname());
    assertEquals(16L, result.getId());
    verify(userMapper).insert(any(User.class));
  }

  // ========== getUserById 测试 ==========

  @Test
  void testGetUserById_Success() {
    // Given
    Long userId = 1L;
    when(userMapper.selectById(userId)).thenReturn(testUser);

    // When
    User result = userService.getUserById(userId);

    // Then
    assertNotNull(result);
    assertEquals(testUser.getId(), result.getId());
    assertEquals(testUser.getUsername(), result.getUsername());
    assertEquals(testUser.getNickname(), result.getNickname());
    verify(userMapper).selectById(userId);
  }

  @Test
  void testGetUserById_NotFound() {
    // Given
    Long userId = 999L;
    when(userMapper.selectById(userId)).thenReturn(null);

    // When
    User result = userService.getUserById(userId);

    // Then
    assertNull(result);
    verify(userMapper).selectById(userId);
  }

  @Test
  void testGetUserById_NullId() {
    // Given
    Long userId = null;
    when(userMapper.selectById(userId)).thenReturn(null);

    // When
    User result = userService.getUserById(userId);

    // Then
    assertNull(result);
    verify(userMapper).selectById(userId);
  }

  @Test
  void testGetUserById_NegativeId() {
    // Given
    Long userId = -1L;
    when(userMapper.selectById(userId)).thenReturn(null);

    // When
    User result = userService.getUserById(userId);

    // Then
    assertNull(result);
    verify(userMapper).selectById(userId);
  }

  @Test
  void testGetUserById_ZeroId() {
    // Given
    Long userId = 0L;
    when(userMapper.selectById(userId)).thenReturn(null);

    // When
    User result = userService.getUserById(userId);

    // Then
    assertNull(result);
    verify(userMapper).selectById(userId);
  }

  @Test
  void testGetUserById_ExceptionHandling() {
    // Given
    Long userId = 1L;
    when(userMapper.selectById(userId)).thenThrow(new RuntimeException("Database error"));

    // When
    User result = userService.getUserById(userId);

    // Then
    assertNull(result);
    verify(userMapper).selectById(userId);
  }

  // ========== getUserByUsername 测试 ==========

  @Test
  void testGetUserByUsername_Success() {
    // Given
    String username = "testuser";
    when(userMapper.selectByUsername(username)).thenReturn(testUser);

    // When
    User result = userService.getUserByUsername(username);

    // Then
    assertNotNull(result);
    assertEquals(testUser.getId(), result.getId());
    assertEquals(testUser.getUsername(), result.getUsername());
    assertEquals(testUser.getNickname(), result.getNickname());
    verify(userMapper).selectByUsername(username);
  }

  @Test
  void testGetUserByUsername_NotFound() {
    // Given
    String username = "nonexistent";
    when(userMapper.selectByUsername(username)).thenReturn(null);

    // When
    User result = userService.getUserByUsername(username);

    // Then
    assertNull(result);
    verify(userMapper).selectByUsername(username);
  }

  @Test
  void testGetUserByUsername_NullUsername() {
    // Given
    String username = null;
    when(userMapper.selectByUsername(username)).thenReturn(null);

    // When
    User result = userService.getUserByUsername(username);

    // Then
    assertNull(result);
    verify(userMapper).selectByUsername(username);
  }

  @Test
  void testGetUserByUsername_WithSpecialCharacters() {
    // Given
    String username = "user🌟🔍🚀";
    User specialUser = new User();
    specialUser.setId(2L);
    specialUser.setUsername(username);
    specialUser.setNickname("特殊字符用户");
    specialUser.setCreatedAt(LocalDateTime.now());
    specialUser.setUpdatedAt(LocalDateTime.now());

    when(userMapper.selectByUsername(username)).thenReturn(specialUser);

    // When
    User result = userService.getUserByUsername(username);

    // Then
    assertNotNull(result);
    assertEquals(specialUser.getId(), result.getId());
    assertEquals(username, result.getUsername());
    assertEquals(specialUser.getNickname(), result.getNickname());
    verify(userMapper).selectByUsername(username);
  }

  @Test
  void testGetUserByUsername_WithLongUsername() {
    // Given
    StringBuilder longUsername = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longUsername.append("longusername");
    }
    String username = longUsername.toString();

    User longUser = new User();
    longUser.setId(3L);
    longUser.setUsername(username);
    longUser.setNickname("长用户名用户");
    longUser.setCreatedAt(LocalDateTime.now());
    longUser.setUpdatedAt(LocalDateTime.now());

    when(userMapper.selectByUsername(username)).thenReturn(longUser);

    // When
    User result = userService.getUserByUsername(username);

    // Then
    assertNotNull(result);
    assertEquals(longUser.getId(), result.getId());
    assertEquals(username, result.getUsername());
    assertEquals(longUser.getNickname(), result.getNickname());
    verify(userMapper).selectByUsername(username);
  }

  @Test
  void testGetUserByUsername_WithUnicode() {
    // Given
    String username = "测试用户名";
    User unicodeUser = new User();
    unicodeUser.setId(4L);
    unicodeUser.setUsername(username);
    unicodeUser.setNickname("Unicode用户");
    unicodeUser.setCreatedAt(LocalDateTime.now());
    unicodeUser.setUpdatedAt(LocalDateTime.now());

    when(userMapper.selectByUsername(username)).thenReturn(unicodeUser);

    // When
    User result = userService.getUserByUsername(username);

    // Then
    assertNotNull(result);
    assertEquals(unicodeUser.getId(), result.getId());
    assertEquals(username, result.getUsername());
    assertEquals(unicodeUser.getNickname(), result.getNickname());
    verify(userMapper).selectByUsername(username);
  }

  @Test
  void testGetUserByUsername_EmptyUsername() {
    // Given
    String username = "";
    when(userMapper.selectByUsername(username)).thenReturn(null);

    // When
    User result = userService.getUserByUsername(username);

    // Then
    assertNull(result);
    verify(userMapper).selectByUsername(username);
  }

  @Test
  void testGetUserByUsername_ExceptionHandling() {
    // Given
    String username = "testuser";
    when(userMapper.selectByUsername(username)).thenThrow(new RuntimeException("Database error"));

    // When
    User result = userService.getUserByUsername(username);

    // Then
    assertNull(result);
    verify(userMapper).selectByUsername(username);
  }

  // ========== loginOrCreate 测试 ==========

  @Test
  void testLoginOrCreate_ExistingUser() {
    // Given
    String username = "testuser";
    String nickname = "测试用户";
    when(userMapper.selectByUsername(username)).thenReturn(testUser);

    // When
    User result = userService.loginOrCreate(username, nickname);

    // Then
    assertNotNull(result);
    assertEquals(testUser.getId(), result.getId());
    assertEquals(testUser.getUsername(), result.getUsername());
    assertEquals(testUser.getNickname(), result.getNickname());
    verify(userMapper).selectByUsername(username);
    verify(userMapper, never()).insert(any(User.class));
  }

  @Test
  void testLoginOrCreate_NewUser() {
    // Given
    String username = "newuser";
    String nickname = "新用户";
    when(userMapper.selectByUsername(username)).thenReturn(null);

    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(5L);
              return null;
            })
        .when(userMapper)
        .insert(any(User.class));

    // When
    User result = userService.loginOrCreate(username, nickname);

    // Then
    assertNotNull(result);
    assertEquals(username, result.getUsername());
    assertEquals(nickname, result.getNickname());
    assertEquals(5L, result.getId());
    verify(userMapper).selectByUsername(username);
    verify(userMapper).insert(any(User.class));
  }

  @Test
  void testLoginOrCreate_NullUsername() {
    // Given
    String username = null;
    String nickname = "用户";
    when(userMapper.selectByUsername(username)).thenReturn(null);

    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(6L);
              return null;
            })
        .when(userMapper)
        .insert(any(User.class));

    // When
    User result = userService.loginOrCreate(username, nickname);

    // Then
    assertNotNull(result);
    assertNull(result.getUsername());
    assertEquals(nickname, result.getNickname());
    assertEquals(6L, result.getId());
    verify(userMapper).selectByUsername(username);
    verify(userMapper).insert(any(User.class));
  }

  @Test
  void testLoginOrCreate_WithSpecialCharacters() {
    // Given
    String username = "user🌟🔍🚀";
    String nickname = "特殊字符用户";
    when(userMapper.selectByUsername(username)).thenReturn(null);

    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(13L);
              return null;
            })
        .when(userMapper)
        .insert(any(User.class));

    // When
    User result = userService.loginOrCreate(username, nickname);

    // Then
    assertNotNull(result);
    assertEquals(username, result.getUsername());
    assertEquals(nickname, result.getNickname());
    assertEquals(13L, result.getId());
    verify(userMapper).selectByUsername(username);
    verify(userMapper).insert(any(User.class));
  }

  @Test
  void testLoginOrCreate_WithLongValues() {
    // Given
    StringBuilder longUsername = new StringBuilder();
    StringBuilder longNickname = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longUsername.append("longusername");
      longNickname.append("longnickname");
    }
    String username = longUsername.toString();
    String nickname = longNickname.toString();

    when(userMapper.selectByUsername(username)).thenReturn(null);

    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(14L);
              return null;
            })
        .when(userMapper)
        .insert(any(User.class));

    // When
    User result = userService.loginOrCreate(username, nickname);

    // Then
    assertNotNull(result);
    assertEquals(username, result.getUsername());
    assertEquals(nickname, result.getNickname());
    assertEquals(14L, result.getId());
    verify(userMapper).selectByUsername(username);
    verify(userMapper).insert(any(User.class));
  }

  @Test
  void testLoginOrCreate_WithUnicode() {
    // Given
    String username = "测试用户名";
    String nickname = "Unicode昵称";
    when(userMapper.selectByUsername(username)).thenReturn(null);

    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(15L);
              return null;
            })
        .when(userMapper)
        .insert(any(User.class));

    // When
    User result = userService.loginOrCreate(username, nickname);

    // Then
    assertNotNull(result);
    assertEquals(username, result.getUsername());
    assertEquals(nickname, result.getNickname());
    assertEquals(15L, result.getId());
    verify(userMapper).selectByUsername(username);
    verify(userMapper).insert(any(User.class));
  }

  @Test
  void testLoginOrCreate_EmptyUsername() {
    // Given
    String username = "";
    String nickname = "用户";
    when(userMapper.selectByUsername(username)).thenReturn(null);

    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(8L);
              return null;
            })
        .when(userMapper)
        .insert(any(User.class));

    // When
    User result = userService.loginOrCreate(username, nickname);

    // Then
    assertNotNull(result);
    assertEquals("", result.getUsername());
    assertEquals(nickname, result.getNickname());
    assertEquals(8L, result.getId());
    verify(userMapper).selectByUsername(username);
    verify(userMapper).insert(any(User.class));
  }

  @Test
  void testLoginOrCreate_BothEmpty() {
    // Given
    String username = "";
    String nickname = "";
    when(userMapper.selectByUsername(username)).thenReturn(null);

    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(9L);
              return null;
            })
        .when(userMapper)
        .insert(any(User.class));

    // When
    User result = userService.loginOrCreate(username, nickname);

    // Then
    assertNotNull(result);
    assertEquals("", result.getUsername());
    assertEquals("", result.getNickname());
    assertEquals(9L, result.getId());
    verify(userMapper).selectByUsername(username);
    verify(userMapper).insert(any(User.class));
  }

  @Test
  void testLoginOrCreate_UserExistsWithSpecialCharacters() {
    // Given
    String username = "existing🌟user";
    String nickname = "现有用户";

    User existingUser = new User();
    existingUser.setId(5L);
    existingUser.setUsername(username);
    existingUser.setNickname(nickname);
    existingUser.setCreatedAt(LocalDateTime.now());
    existingUser.setUpdatedAt(LocalDateTime.now());

    when(userMapper.selectByUsername(username)).thenReturn(existingUser);

    // When
    User result = userService.loginOrCreate(username, nickname);

    // Then
    assertNotNull(result);
    assertEquals(existingUser.getId(), result.getId());
    assertEquals(username, result.getUsername());
    assertEquals(nickname, result.getNickname());
    verify(userMapper).selectByUsername(username);
    verify(userMapper, never()).insert(any(User.class));
  }

  @Test
  void testLoginOrCreate_ExceptionHandling() {
    // Given
    String username = "newuser";
    String nickname = "新用户";

    doThrow(new RuntimeException("Database error")).when(userMapper).insert(any(User.class));

    // When & Then
    assertDoesNotThrow(() -> userService.createUser(username, nickname));
    verify(userMapper).insert(any(User.class));
  }

  // ========== updateUser 测试 ==========

  @Test
  void testUpdateUser_Success() {
    // Given
    User userToUpdate = new User();
    userToUpdate.setId(1L);
    userToUpdate.setUsername("updateduser");
    userToUpdate.setNickname("更新用户");

    // When
    userService.updateUser(userToUpdate);

    // Then
    verify(userMapper).updateById(userToUpdate);
  }

  @Test
  void testUpdateUser_NullUser() {
    // Given
    User userToUpdate = null;

    // When
    userService.updateUser(userToUpdate);

    // Then
    verify(userMapper).updateById(userToUpdate);
  }

  @Test
  void testUpdateUser_UserWithNullFields() {
    // Given
    User userToUpdate = new User();
    userToUpdate.setId(null);
    userToUpdate.setUsername(null);
    userToUpdate.setNickname(null);

    // When
    userService.updateUser(userToUpdate);

    // Then
    verify(userMapper).updateById(userToUpdate);
  }

  @Test
  void testUpdateUser_UserWithEmptyFields() {
    // Given
    User userToUpdate = new User();
    userToUpdate.setId(1L);
    userToUpdate.setUsername("");
    userToUpdate.setNickname("");

    // When
    userService.updateUser(userToUpdate);

    // Then
    verify(userMapper).updateById(userToUpdate);
  }

  @Test
  void testUpdateUser_UserWithMixedFields() {
    // Given
    User userToUpdate = new User();
    userToUpdate.setId(1L);
    userToUpdate.setUsername("validuser");
    userToUpdate.setNickname(null);

    // When
    userService.updateUser(userToUpdate);

    // Then
    verify(userMapper).updateById(userToUpdate);
  }

  @Test
  void testUpdateUser_WithSpecialCharacters() {
    // Given
    User userToUpdate = new User();
    userToUpdate.setId(1L);
    userToUpdate.setUsername("user🌟🔍🚀");
    userToUpdate.setNickname("特殊字符昵称");

    // When
    userService.updateUser(userToUpdate);

    // Then
    verify(userMapper).updateById(userToUpdate);
  }

  @Test
  void testUpdateUser_WithLongValues() {
    // Given
    StringBuilder longUsername = new StringBuilder();
    StringBuilder longNickname = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longUsername.append("longusername");
      longNickname.append("longnickname");
    }

    User userToUpdate = new User();
    userToUpdate.setId(1L);
    userToUpdate.setUsername(longUsername.toString());
    userToUpdate.setNickname(longNickname.toString());

    // When
    userService.updateUser(userToUpdate);

    // Then
    verify(userMapper).updateById(userToUpdate);
  }

  @Test
  void testUpdateUser_WithUnicode() {
    // Given
    User userToUpdate = new User();
    userToUpdate.setId(1L);
    userToUpdate.setUsername("测试用户名");
    userToUpdate.setNickname("Unicode昵称");

    // When
    userService.updateUser(userToUpdate);

    // Then
    verify(userMapper).updateById(userToUpdate);
  }

  @Test
  void testUpdateUser_ExceptionHandling() {
    // Given
    User userToUpdate = new User();
    userToUpdate.setId(1L);
    userToUpdate.setUsername("updateduser");
    userToUpdate.setNickname("更新用户");

    doThrow(new RuntimeException("Database error")).when(userMapper).updateById(userToUpdate);

    // When & Then
    assertDoesNotThrow(() -> userService.updateUser(userToUpdate));
    verify(userMapper).updateById(userToUpdate);
  }
}
