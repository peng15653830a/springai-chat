package com.example.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testUserCreation() {
        // When
        User newUser = new User();

        // Then
        assertNotNull(newUser);
        assertNull(newUser.getId());
        assertNull(newUser.getNickname());
        assertNull(newUser.getUsername());
        assertNull(newUser.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        Long id = 1L;
        String username = "testuser";
        String nickname = "TestUser";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        user.setId(id);
        user.setUsername(username);
        user.setNickname(nickname);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        // Then
        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(nickname, user.getNickname());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(updatedAt, user.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("testuser");

        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("testuser");

        User user3 = new User();
        user3.setId(2L);
        user3.setUsername("testuser");

        // Then
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        user.setId(1L);
        user.setUsername("testuser");
        user.setNickname("TestUser");

        // When
        String toString = user.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("TestUser"));
        assertTrue(toString.contains("testuser"));
    }

    @Test
    void testNullValues() {
        // When & Then
        assertDoesNotThrow(() -> {
            user.setId(null);
            user.setUsername(null);
            user.setNickname(null);
            user.setCreatedAt(null);
            user.setUpdatedAt(null);
        });

        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getNickname());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }

    @Test
    void testEmptyValues() {
        // When & Then
        assertDoesNotThrow(() -> {
            user.setUsername("");
            user.setNickname("");
        });

        assertEquals("", user.getUsername());
        assertEquals("", user.getNickname());
    }
}