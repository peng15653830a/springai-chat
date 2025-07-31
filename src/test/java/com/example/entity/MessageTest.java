package com.example.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    private Message message;

    @BeforeEach
    void setUp() {
        message = new Message();
    }

    @Test
    void testMessageCreation() {
        // When
        Message newMessage = new Message();

        // Then
        assertNotNull(newMessage);
        assertNull(newMessage.getId());
        assertNull(newMessage.getConversationId());
        assertNull(newMessage.getRole());
        assertNull(newMessage.getContent());
        assertNull(newMessage.getThinking());
        assertNull(newMessage.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        Long id = 1L;
        Long conversationId = 2L;
        String role = "user";
        String content = "Test message content";
        String thinking = "Thinking process";
        Date createdAt = new Date();

        // When
        message.setId(id);
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setThinking(thinking);
        message.setCreatedAt(createdAt);

        // Then
        assertEquals(id, message.getId());
        assertEquals(conversationId, message.getConversationId());
        assertEquals(role, message.getRole());
        assertEquals(content, message.getContent());
        assertEquals(thinking, message.getThinking());
        assertEquals(createdAt, message.getCreatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        Message msg1 = new Message();
        msg1.setId(1L);
        msg1.setContent("Test");

        Message msg2 = new Message();
        msg2.setId(1L);
        msg2.setContent("Test");

        Message msg3 = new Message();
        msg3.setId(2L);
        msg3.setContent("Test");

        // Then
        assertEquals(msg1, msg2);
        assertNotEquals(msg1, msg3);
        assertEquals(msg1.hashCode(), msg2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        message.setId(1L);
        message.setRole("user");
        message.setContent("Test message");

        // When
        String toString = message.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("user"));
        assertTrue(toString.contains("1"));
    }

    @Test
    void testValidRoles() {
        // When & Then
        assertDoesNotThrow(() -> {
            message.setRole("user");
            assertEquals("user", message.getRole());

            message.setRole("assistant");
            assertEquals("assistant", message.getRole());

            message.setRole("system");
            assertEquals("system", message.getRole());
        });
    }

    @Test
    void testLongContent() {
        // Given
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append("This is a very long message content that might exceed normal limits but should still be handled properly by the entity. ");
        }
        String longContent = sb.toString();

        // When & Then
        assertDoesNotThrow(() -> {
            message.setContent(longContent);
        });

        assertEquals(longContent, message.getContent());
    }

    @Test
    void testSpecialCharactersInContent() {
        // Given
        String specialContent = "Message with special chars: @#$%^&*()! and emojis: 😀🎉🚀";

        // When & Then
        assertDoesNotThrow(() -> {
            message.setContent(specialContent);
        });

        assertEquals(specialContent, message.getContent());
    }

    @Test
    void testThinkingField() {
        // Given
        String thinking = "This is the AI's thinking process before generating the response.";

        // When & Then
        assertDoesNotThrow(() -> {
            message.setThinking(thinking);
        });

        assertEquals(thinking, message.getThinking());
    }

    @Test
    void testNullValues() {
        // When & Then
        assertDoesNotThrow(() -> {
            message.setId(null);
            message.setConversationId(null);
            message.setRole(null);
            message.setContent(null);
            message.setThinking(null);
            message.setCreatedAt(null);
        });

        assertNull(message.getId());
        assertNull(message.getConversationId());
        assertNull(message.getRole());
        assertNull(message.getContent());
        assertNull(message.getThinking());
        assertNull(message.getCreatedAt());
    }

    @Test
    void testEmptyValues() {
        // When & Then
        assertDoesNotThrow(() -> {
            message.setRole("");
            message.setContent("");
            message.setThinking("");
        });

        assertEquals("", message.getRole());
        assertEquals("", message.getContent());
        assertEquals("", message.getThinking());
    }
}