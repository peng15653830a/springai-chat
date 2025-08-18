package com.example.service.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatMessage测试
 *
 * @author xupeng
 */
class ChatMessageTest {

  @Test
  void shouldCreateChatMessage() {
    // Given
    String role = "user";
    String content = "Hello, how are you?";

    // When
    ChatMessage message = new ChatMessage(role, content);

    // Then
    assertEquals(role, message.getRole());
    assertEquals(content, message.getContent());
  }

  @Test
  void shouldCreateEmptyChatMessage() {
    // When
    ChatMessage message = new ChatMessage();

    // Then
    assertNull(message.getRole());
    assertNull(message.getContent());
  }

  @Test
  void shouldSetProperties() {
    // Given
    ChatMessage message = new ChatMessage();

    // When
    message.setRole("assistant");
    message.setContent("I'm doing well, thank you!");

    // Then
    assertEquals("assistant", message.getRole());
    assertEquals("I'm doing well, thank you!", message.getContent());
  }

  @Test
  void shouldTestEquality() {
    // Given
    ChatMessage message1 = new ChatMessage("user", "Hello");
    ChatMessage message2 = new ChatMessage("user", "Hello");
    ChatMessage message3 = new ChatMessage("assistant", "Hello");

    // Then
    assertEquals(message1, message2);
    assertNotEquals(message1, message3);
    assertEquals(message1.hashCode(), message2.hashCode());
  }

  @Test
  void shouldTestToString() {
    // Given
    ChatMessage message = new ChatMessage("system", "You are a helpful assistant");

    // When
    String toString = message.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("system"));
    assertTrue(toString.contains("You are a helpful assistant"));
  }

  @Test
  void shouldHandleNullRole() {
    // Given
    ChatMessage message = new ChatMessage(null, "content");

    // Then
    assertNull(message.getRole());
    assertEquals("content", message.getContent());
  }

  @Test
  void shouldHandleNullContent() {
    // Given
    ChatMessage message = new ChatMessage("user", null);

    // Then
    assertEquals("user", message.getRole());
    assertNull(message.getContent());
  }

  @Test
  void shouldHandleEmptyStrings() {
    // Given
    ChatMessage message = new ChatMessage("", "");

    // Then
    assertEquals("", message.getRole());
    assertEquals("", message.getContent());
  }

  @Test
  void shouldCreateUserMessage() {
    // Given
    String content = "What is the weather like?";

    // When
    ChatMessage message = new ChatMessage("user", content);

    // Then
    assertEquals("user", message.getRole());
    assertEquals(content, message.getContent());
  }

  @Test
  void shouldCreateAssistantMessage() {
    // Given
    String content = "I can help you with that.";

    // When
    ChatMessage message = new ChatMessage("assistant", content);

    // Then
    assertEquals("assistant", message.getRole());
    assertEquals(content, message.getContent());
  }

  @Test
  void shouldCreateSystemMessage() {
    // Given
    String content = "You are a helpful AI assistant.";

    // When
    ChatMessage message = new ChatMessage("system", content);

    // Then
    assertEquals("system", message.getRole());
    assertEquals(content, message.getContent());
  }

  @Test
  void shouldHandleLongContent() {
    // Given
    String longContent = "This is a very long message that contains a lot of text to test how the ChatMessage class handles longer content strings and whether it can store and retrieve them correctly without any issues.";
    
    // When
    ChatMessage message = new ChatMessage("user", longContent);

    // Then
    assertEquals("user", message.getRole());
    assertEquals(longContent, message.getContent());
  }

  @Test
  void shouldCompareMessages() {
    // Given
    ChatMessage message1 = new ChatMessage("user", "First message");
    ChatMessage message2 = new ChatMessage("user", "Second message");

    // Then
    assertNotEquals(message1, message2);
    assertNotEquals(message1.getContent(), message2.getContent());
  }

  @Test
  void shouldEqualItself() {
    // Given
    ChatMessage message = new ChatMessage("user", "Hello");

    // Then
    assertEquals(message, message);
    assertEquals(message.hashCode(), message.hashCode());
  }
}