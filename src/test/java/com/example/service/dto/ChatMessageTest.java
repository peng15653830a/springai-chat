package com.example.service.dto;

import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

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

  @Test
  void shouldCreateSystemMessageUsingFactory() {
    // Given
    String content = "You are a helpful assistant";
    
    // When
    ChatMessage message = ChatMessage.createSystemMessage(content);
    
    // Then
    assertEquals("system", message.getRole());
    assertEquals(content, message.getContent());
  }

  @Test
  void shouldCreateUserMessageUsingFactory() {
    // Given
    String content = "How can I help you today?";
    
    // When
    ChatMessage message = ChatMessage.createUserMessage(content);
    
    // Then
    assertEquals("user", message.getRole());
    assertEquals(content, message.getContent());
  }

  @Test
  void shouldCreateAssistantMessageUsingFactory() {
    // Given
    String content = "I'm here to help!";
    
    // When
    ChatMessage message = ChatMessage.createAssistantMessage(content);
    
    // Then
    assertEquals("assistant", message.getRole());
    assertEquals(content, message.getContent());
  }

  @Test
  void shouldCreateSystemMessageWithNullContent() {
    // When
    ChatMessage message = ChatMessage.createSystemMessage(null);
    
    // Then
    assertEquals("system", message.getRole());
    assertNull(message.getContent());
  }

  @Test
  void shouldCreateUserMessageWithNullContent() {
    // When
    ChatMessage message = ChatMessage.createUserMessage(null);
    
    // Then
    assertEquals("user", message.getRole());
    assertNull(message.getContent());
  }

  @Test
  void shouldCreateAssistantMessageWithNullContent() {
    // When
    ChatMessage message = ChatMessage.createAssistantMessage(null);
    
    // Then
    assertEquals("assistant", message.getRole());
    assertNull(message.getContent());
  }

  @Test
  void shouldCreateSystemMessageWithEmptyContent() {
    // When
    ChatMessage message = ChatMessage.createSystemMessage("");
    
    // Then
    assertEquals("system", message.getRole());
    assertEquals("", message.getContent());
  }

  @Test
  void shouldCreateUserMessageWithEmptyContent() {
    // When
    ChatMessage message = ChatMessage.createUserMessage("");
    
    // Then
    assertEquals("user", message.getRole());
    assertEquals("", message.getContent());
  }

  @Test
  void shouldCreateAssistantMessageWithEmptyContent() {
    // When
    ChatMessage message = ChatMessage.createAssistantMessage("");
    
    // Then
    assertEquals("assistant", message.getRole());
    assertEquals("", message.getContent());
  }

  @Test
  void shouldTestEqualityWithNullRole() {
    // Given
    ChatMessage message1 = new ChatMessage();
    message1.setContent("content");
    
    ChatMessage message2 = new ChatMessage();
    message2.setContent("content");
    
    // Then
    assertEquals(message1, message2); // 两个都是null role
  }

  @Test
  void shouldTestEqualityWithDifferentRole() {
    // Given
    ChatMessage message1 = new ChatMessage();
    message1.setRole("user");
    message1.setContent("content");
    
    ChatMessage message2 = new ChatMessage();
    message2.setRole("assistant");
    message2.setContent("content");
    
    // Then
    assertNotEquals(message1, message2);
  }

  @Test
  void shouldTestEqualityWithNullContent() {
    // Given
    ChatMessage message1 = new ChatMessage();
    message1.setRole("user");
    
    ChatMessage message2 = new ChatMessage();
    message2.setRole("user");
    
    // Then
    assertEquals(message1, message2); // 两个都是null content
  }

  @Test
  void shouldTestEqualityWithDifferentContent() {
    // Given
    ChatMessage message1 = new ChatMessage();
    message1.setRole("user");
    message1.setContent("content1");
    
    ChatMessage message2 = new ChatMessage();
    message2.setRole("user");
    message2.setContent("content2");
    
    // Then
    assertNotEquals(message1, message2);
  }

  @Test
  void shouldTestHashCodeConsistency() {
    // Given
    ChatMessage message = new ChatMessage("user", "content");
    
    int hashCode1 = message.hashCode();
    int hashCode2 = message.hashCode();
    
    // Then
    assertEquals(hashCode1, hashCode2);
  }

  @Test
  void shouldTestToStringWithNullFields() {
    // Given
    ChatMessage message = new ChatMessage();
    
    // When
    String toString = message.toString();
    
    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("ChatMessage"));
  }

  @Test
  void shouldHandleSpecialCharactersInContent() {
    // Given
    String specialContent = "Content with special chars: @#$%^&*()";
    ChatMessage message = new ChatMessage("user", specialContent);
    
    // Then
    assertEquals("user", message.getRole());
    assertEquals(specialContent, message.getContent());
  }

  @Test
  void shouldHandleMultilineContent() {
    // Given
    String multilineContent = "Line 1\nLine 2\nLine 3";
    ChatMessage message = new ChatMessage("user", multilineContent);
    
    // Then
    assertEquals("user", message.getRole());
    assertEquals(multilineContent, message.getContent());
    assertTrue(message.getContent().contains("\n"));
  }

  @Test
  void shouldHandleVeryLongContent() {
    // Given
    String longContent = "a".repeat(1000);
    ChatMessage message = new ChatMessage("user", longContent);
    
    // Then
    assertEquals("user", message.getRole());
    assertEquals(longContent, message.getContent());
    assertEquals(1000, message.getContent().length());
  }

  @Test
  void shouldCompareFactoryCreatedMessages() {
    // Given
    ChatMessage systemMessage = ChatMessage.createSystemMessage("system prompt");
    ChatMessage userMessage = ChatMessage.createUserMessage("user question");
    ChatMessage assistantMessage = ChatMessage.createAssistantMessage("assistant response");
    
    // Then
    assertNotEquals(systemMessage, userMessage);
    assertNotEquals(userMessage, assistantMessage);
    assertNotEquals(systemMessage, assistantMessage);
    
    assertEquals("system", systemMessage.getRole());
    assertEquals("user", userMessage.getRole());
    assertEquals("assistant", assistantMessage.getRole());
  }

  @Test
  void shouldCreateIdenticalMessagesUsingFactoryAndConstructor() {
    // Given
    String content = "test content";
    
    // When
    ChatMessage factoryMessage = ChatMessage.createUserMessage(content);
    ChatMessage constructorMessage = new ChatMessage("user", content);
    
    // Then
    assertEquals(factoryMessage, constructorMessage);
    assertEquals(factoryMessage.hashCode(), constructorMessage.hashCode());
  }

  @Test
  void shouldTestChatMessageEqualsWithSameInstance() {
    ChatMessage message = new ChatMessage();
    message.setRole("user");
    assertEquals(message, message); // Same instance reference
  }

  @Test
  void shouldTestChatMessageEqualsWithNull() {
    ChatMessage message = new ChatMessage();
    assertNotEquals(message, null); // Null comparison
  }

  @Test
  void shouldTestChatMessageEqualsWithDifferentType() {
    ChatMessage message = new ChatMessage();
    assertNotEquals(message, "not a ChatMessage"); // Different type
  }

  @Test
  void shouldTestChatMessageEqualsWithCanEqualFalse() {
    ChatMessage message1 = new ChatMessage();
    message1.setRole("user");
    
    // Anonymous subclass that overrides canEqual to return false
    ChatMessage message2 = new ChatMessage() {
      @Override
      public boolean canEqual(Object other) {
        return false;
      }
    };
    message2.setRole("user");
    
    assertNotEquals(message1, message2); // canEqual returns false
  }

  @Test
  void shouldTestChatMessageEqualsWithOneNullRoleField() {
    ChatMessage message1 = new ChatMessage();
    message1.setRole("user");
    message1.setContent("content");
    
    ChatMessage message2 = new ChatMessage();
    message2.setRole(null);
    message2.setContent("content");
    
    assertNotEquals(message1, message2); // One has null role, other doesn't
  }

  @Test
  void shouldTestChatMessageEqualsWithOneNullContentField() {
    ChatMessage message1 = new ChatMessage();
    message1.setRole("user");
    message1.setContent("content");
    
    ChatMessage message2 = new ChatMessage();
    message2.setRole("user");
    message2.setContent(null);
    
    assertNotEquals(message1, message2); // One has null content, other doesn't
  }

  @Test
  void shouldTestChatMessageHashCodeWithAllNullFields() {
    ChatMessage message1 = new ChatMessage();
    ChatMessage message2 = new ChatMessage();
    
    assertEquals(message1.hashCode(), message2.hashCode()); // Both have all null fields
  }
}