package com.example.service;

import com.example.entity.User;
import com.example.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

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
        
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return null;
        }).when(userMapper).insert(any(User.class));

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
        
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(3L);
            return null;
        }).when(userMapper).insert(any(User.class));

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
        
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(4L);
            return null;
        }).when(userMapper).insert(any(User.class));

        // When
        User result = userService.createUser(username, nickname);

        // Then
        assertNotNull(result);
        assertEquals("", result.getUsername());
        assertEquals("", result.getNickname());
        assertEquals(4L, result.getId());
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
        
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(5L);
            return null;
        }).when(userMapper).insert(any(User.class));

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
        
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(6L);
            return null;
        }).when(userMapper).insert(any(User.class));

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
    void testLoginOrCreate_EmptyUsername() {
        // Given
        String username = "";
        String nickname = "用户";
        when(userMapper.selectByUsername(username)).thenReturn(null);
        
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(8L);
            return null;
        }).when(userMapper).insert(any(User.class));

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
        
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(9L);
            return null;
        }).when(userMapper).insert(any(User.class));

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
}