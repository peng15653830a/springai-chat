package com.example.service;

import com.example.entity.User;
import com.example.mapper.UserMapper;
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
public class UserServiceTest {

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
        testUser.setNickname("TestUser");
        testUser.setCreatedAt(new Date());
    }

    @Test
    void testGetUserByUsername_UserExists() {
        // Given
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        // When
        User result = userService.getUserByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("TestUser", result.getNickname());
        verify(userMapper).selectByUsername("testuser");
    }

    @Test
    void testGetUserByUsername_UserNotExists() {
        // Given
        when(userMapper.selectByUsername("nonexistent")).thenReturn(null);

        // When
        User result = userService.getUserByUsername("nonexistent");

        // Then
        assertNull(result);
        verify(userMapper).selectByUsername("nonexistent");
    }

    @Test
    void testCreateUser_Success() {
        // Given
        doNothing().when(userMapper).insert(any(User.class));

        // When
        User result = userService.createUser("newuser", "NewUser");

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("NewUser", result.getNickname());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void testLoginOrCreate_ExistingUser() {
        // Given
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        // When
        User result = userService.loginOrCreate("testuser", "TestUser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("TestUser", result.getNickname());
        verify(userMapper).selectByUsername("testuser");
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testLoginOrCreate_NewUser() {
        // Given
        when(userMapper.selectByUsername("newuser")).thenReturn(null);
        doNothing().when(userMapper).insert(any(User.class));

        // When
        User result = userService.loginOrCreate("newuser", "NewUser");

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("NewUser", result.getNickname());
        verify(userMapper).selectByUsername("newuser");
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void testGetUserById_UserExists() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // When
        User result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TestUser", result.getNickname());
        verify(userMapper).selectById(1L);
    }

    @Test
    void testGetUserById_UserNotExists() {
        // Given
        when(userMapper.selectById(999L)).thenReturn(null);

        // When
        User result = userService.getUserById(999L);

        // Then
        assertNull(result);
        verify(userMapper).selectById(999L);
    }

    @Test
    void testUpdateUser_Success() {
        // Given
        doNothing().when(userMapper).updateById(any(User.class));

        // When
        userService.updateUser(testUser);

        // Then
        verify(userMapper).updateById(testUser);
    }

    @Test
    void testCreateUser_NullUsername() {
        // Given
        doNothing().when(userMapper).insert(any(User.class));

        // When
        User result = userService.createUser(null, "Nickname");

        // Then
        assertNotNull(result);
        assertNull(result.getUsername());
        assertEquals("Nickname", result.getNickname());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void testCreateUser_EmptyUsername() {
        // Given
        doNothing().when(userMapper).insert(any(User.class));

        // When
        User result = userService.createUser("", "Nickname");

        // Then
        assertNotNull(result);
        assertEquals("", result.getUsername());
        assertEquals("Nickname", result.getNickname());
        verify(userMapper).insert(any(User.class));
    }
}