package com.example.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class ConversationTest {

    private Conversation conversation;

    @BeforeEach
    void setUp() {
        conversation = new Conversation();
    }

    @Test
    void testConversationCreation() {
        // When
        Conversation newConversation = new Conversation();

        // Then
        assertNotNull(newConversation);
        assertNull(newConversation.getId());
        assertNull(newConversation.getUserId());
        assertNull(newConversation.getTitle());
        assertNull(newConversation.getCreatedAt());
        assertNull(newConversation.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        Long id = 1L;
        Long userId = 2L;
        String title = "Test Conversation";
        Date createdAt = new Date();
        Date updatedAt = new Date();

        // When
        conversation.setId(id);
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversation.setCreatedAt(createdAt);
        conversation.setUpdatedAt(updatedAt);

        // Then
        assertEquals(id, conversation.getId());
        assertEquals(userId, conversation.getUserId());
        assertEquals(title, conversation.getTitle());
        assertEquals(createdAt, conversation.getCreatedAt());
        assertEquals(updatedAt, conversation.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        Conversation conv1 = new Conversation();
        conv1.setId(1L);
        conv1.setTitle("Test");

        Conversation conv2 = new Conversation();
        conv2.setId(1L);
        conv2.setTitle("Test");

        Conversation conv3 = new Conversation();
        conv3.setId(2L);
        conv3.setTitle("Test");

        // Then
        assertEquals(conv1, conv2);
        assertNotEquals(conv1, conv3);
        assertEquals(conv1.hashCode(), conv2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        conversation.setId(1L);
        conversation.setUserId(2L);
        conversation.setTitle("Test Conversation");

        // When
        String toString = conversation.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("Test Conversation"));
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("2"));
    }

    @Test
    void testNullValues() {
        // When & Then
        assertDoesNotThrow(() -> {
            conversation.setId(null);
            conversation.setUserId(null);
            conversation.setTitle(null);
            conversation.setCreatedAt(null);
            conversation.setUpdatedAt(null);
        });

        assertNull(conversation.getId());
        assertNull(conversation.getUserId());
        assertNull(conversation.getTitle());
        assertNull(conversation.getCreatedAt());
        assertNull(conversation.getUpdatedAt());
    }

    @Test
    void testLongTitle() {
        // Given
        String longTitle = "This is a very long conversation title that might exceed normal limits but should still be handled properly by the entity";

        // When & Then
        assertDoesNotThrow(() -> {
            conversation.setTitle(longTitle);
        });

        assertEquals(longTitle, conversation.getTitle());
    }

    @Test
    void testSpecialCharactersInTitle() {
        // Given
        String specialTitle = "Conversation with special chars: @#$%^&*()!";

        // When & Then
        assertDoesNotThrow(() -> {
            conversation.setTitle(specialTitle);
        });

        assertEquals(specialTitle, conversation.getTitle());
    }
}