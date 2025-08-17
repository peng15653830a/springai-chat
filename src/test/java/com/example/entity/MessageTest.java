package com.example.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    LocalDateTime createdAt = LocalDateTime.now();

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
    // Test basic equals and hashCode
    Message msg1 = new Message();
    msg1.setId(1L);
    msg1.setContent("Test");

    Message msg2 = new Message();
    msg2.setId(1L);
    msg2.setContent("Test");

    Message msg3 = new Message();
    msg3.setId(2L);
    msg3.setContent("Test");

    assertEquals(msg1, msg2);
    assertNotEquals(msg1, msg3);
    assertEquals(msg1.hashCode(), msg2.hashCode());

    // Test equals with null
    assertNotEquals(msg1, null);

    // Test equals with different class
    assertNotEquals(msg1, "not a message");

    // Test equals with same object
    assertEquals(msg1, msg1);

    // Test canEqual method
    assertTrue(msg1.canEqual(msg2));
    assertFalse(msg1.canEqual("not a message"));

    // Test each field individually for equals
    // Test id field
    Message msgDiffId = new Message();
    msgDiffId.setId(2L);
    msgDiffId.setContent("Test");
    assertNotEquals(msg1, msgDiffId);

    // Test conversationId field
    Message msgWithConvId1 = new Message();
    msgWithConvId1.setId(1L);
    msgWithConvId1.setConversationId(100L);
    msgWithConvId1.setContent("Test");

    Message msgWithConvId2 = new Message();
    msgWithConvId2.setId(1L);
    msgWithConvId2.setConversationId(200L);
    msgWithConvId2.setContent("Test");

    assertNotEquals(msgWithConvId1, msgWithConvId2);

    // Test role field
    Message msgWithRole1 = new Message();
    msgWithRole1.setId(1L);
    msgWithRole1.setRole("user");
    msgWithRole1.setContent("Test");

    Message msgWithRole2 = new Message();
    msgWithRole2.setId(1L);
    msgWithRole2.setRole("assistant");
    msgWithRole2.setContent("Test");

    assertNotEquals(msgWithRole1, msgWithRole2);

    // Test content field
    Message msgDiffContent = new Message();
    msgDiffContent.setId(1L);
    msgDiffContent.setContent("Different");
    assertNotEquals(msg1, msgDiffContent);

    // Test thinking field
    Message msgWithThinking1 = new Message();
    msgWithThinking1.setId(1L);
    msgWithThinking1.setThinking("thinking1");
    msgWithThinking1.setContent("Test");

    Message msgWithThinking2 = new Message();
    msgWithThinking2.setId(1L);
    msgWithThinking2.setThinking("thinking2");
    msgWithThinking2.setContent("Test");

    assertNotEquals(msgWithThinking1, msgWithThinking2);

    // Test searchResults field
    Message msgWithSearch1 = new Message();
    msgWithSearch1.setId(1L);
    msgWithSearch1.setSearchResults("results1");
    msgWithSearch1.setContent("Test");

    Message msgWithSearch2 = new Message();
    msgWithSearch2.setId(1L);
    msgWithSearch2.setSearchResults("results2");
    msgWithSearch2.setContent("Test");

    assertNotEquals(msgWithSearch1, msgWithSearch2);

    // Test createdAt field
    LocalDateTime time1 = LocalDateTime.of(2023, 1, 1, 12, 0);
    LocalDateTime time2 = LocalDateTime.of(2023, 1, 2, 12, 0);

    Message msgWithTime1 = new Message();
    msgWithTime1.setId(1L);
    msgWithTime1.setCreatedAt(time1);
    msgWithTime1.setContent("Test");

    Message msgWithTime2 = new Message();
    msgWithTime2.setId(1L);
    msgWithTime2.setCreatedAt(time2);
    msgWithTime2.setContent("Test");

    assertNotEquals(msgWithTime1, msgWithTime2);

    // Test with null values in different fields
    Message msgNullId1 = new Message();
    msgNullId1.setId(null);
    msgNullId1.setContent("Test");

    Message msgNullId2 = new Message();
    msgNullId2.setId(null);
    msgNullId2.setContent("Test");

    assertEquals(msgNullId1, msgNullId2);

    Message msgNullConvId1 = new Message();
    msgNullConvId1.setId(1L);
    msgNullConvId1.setConversationId(null);
    msgNullConvId1.setContent("Test");

    Message msgNullConvId2 = new Message();
    msgNullConvId2.setId(1L);
    msgNullConvId2.setConversationId(null);
    msgNullConvId2.setContent("Test");

    assertEquals(msgNullConvId1, msgNullConvId2);

    // Test with all fields set
    LocalDateTime now = LocalDateTime.now();
    Message fullMsg1 = new Message();
    fullMsg1.setId(1L);
    fullMsg1.setConversationId(2L);
    fullMsg1.setRole("user");
    fullMsg1.setContent("Hello");
    fullMsg1.setThinking("thinking");
    fullMsg1.setSearchResults("results");
    fullMsg1.setCreatedAt(now);

    Message fullMsg2 = new Message();
    fullMsg2.setId(1L);
    fullMsg2.setConversationId(2L);
    fullMsg2.setRole("user");
    fullMsg2.setContent("Hello");
    fullMsg2.setThinking("thinking");
    fullMsg2.setSearchResults("results");
    fullMsg2.setCreatedAt(now);

    assertEquals(fullMsg1, fullMsg2);
    assertEquals(fullMsg1.hashCode(), fullMsg2.hashCode());

    // Test hashCode with null values
    Message nullMsg = new Message();
    assertNotEquals(0, nullMsg.hashCode());

    // Test hashCode consistency
    int hashCode1 = fullMsg1.hashCode();
    int hashCode2 = fullMsg1.hashCode();
    assertEquals(hashCode1, hashCode2);
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
    assertDoesNotThrow(
        () -> {
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
      sb.append(
          "This is a very long message content that might exceed normal limits but should still be handled properly by the entity. ");
    }
    String longContent = sb.toString();

    // When & Then
    assertDoesNotThrow(
        () -> {
          message.setContent(longContent);
        });

    assertEquals(longContent, message.getContent());
  }

  @Test
  void testSpecialCharactersInContent() {
    // Given
    String specialContent = "Message with special chars: @#$%^&*()! and emojis: 😀🎉🚀";

    // When & Then
    assertDoesNotThrow(
        () -> {
          message.setContent(specialContent);
        });

    assertEquals(specialContent, message.getContent());
  }

  @Test
  void testThinkingField() {
    // Given
    String thinking = "This is the AI's thinking process before generating the response.";

    // When & Then
    assertDoesNotThrow(
        () -> {
          message.setThinking(thinking);
        });

    assertEquals(thinking, message.getThinking());
  }

  @Test
  void testNullValues() {
    // When & Then
    assertDoesNotThrow(
        () -> {
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
    assertDoesNotThrow(
        () -> {
          message.setRole("");
          message.setContent("");
          message.setThinking("");
          message.setSearchResults("");
        });

    assertEquals("", message.getRole());
    assertEquals("", message.getContent());
    assertEquals("", message.getThinking());
    assertEquals("", message.getSearchResults());
  }

  @Test
  void testSearchResultsField() {
    // Given
    String searchResults = "Search result 1\nSearch result 2\nSearch result 3";

    // When & Then
    assertDoesNotThrow(
        () -> {
          message.setSearchResults(searchResults);
        });

    assertEquals(searchResults, message.getSearchResults());

    // Test with JSON-like search results
    String jsonResults = "{\"results\": [{\"title\": \"Test\", \"url\": \"http://example.com\"}]}";
    message.setSearchResults(jsonResults);
    assertEquals(jsonResults, message.getSearchResults());
  }

  @Test
  void testEqualsEdgeCases() {
    // Test equals with mixed null and non-null values for each field
    Message msg1 = new Message();
    msg1.setId(1L);
    msg1.setConversationId(null);
    msg1.setRole("user");
    msg1.setContent(null);
    msg1.setThinking("thinking");
    msg1.setSearchResults(null);
    msg1.setCreatedAt(LocalDateTime.now());

    Message msg2 = new Message();
    msg2.setId(null);
    msg2.setConversationId(2L);
    msg2.setRole(null);
    msg2.setContent("content");
    msg2.setThinking(null);
    msg2.setSearchResults("results");
    msg2.setCreatedAt(null);

    assertNotEquals(msg1, msg2);

    // Test with one field null vs non-null for each field
    Message msgIdNull = new Message();
    msgIdNull.setId(null);

    Message msgIdNotNull = new Message();
    msgIdNotNull.setId(1L);

    assertNotEquals(msgIdNull, msgIdNotNull);
    assertNotEquals(msgIdNotNull, msgIdNull);

    // Test conversationId null vs non-null
    Message msgConvNull = new Message();
    msgConvNull.setConversationId(null);

    Message msgConvNotNull = new Message();
    msgConvNotNull.setConversationId(1L);

    assertNotEquals(msgConvNull, msgConvNotNull);
    assertNotEquals(msgConvNotNull, msgConvNull);

    // Test role null vs non-null
    Message msgRoleNull = new Message();
    msgRoleNull.setRole(null);

    Message msgRoleNotNull = new Message();
    msgRoleNotNull.setRole("user");

    assertNotEquals(msgRoleNull, msgRoleNotNull);
    assertNotEquals(msgRoleNotNull, msgRoleNull);

    // Test content null vs non-null
    Message msgContentNull = new Message();
    msgContentNull.setContent(null);

    Message msgContentNotNull = new Message();
    msgContentNotNull.setContent("content");

    assertNotEquals(msgContentNull, msgContentNotNull);
    assertNotEquals(msgContentNotNull, msgContentNull);

    // Test thinking null vs non-null
    Message msgThinkingNull = new Message();
    msgThinkingNull.setThinking(null);

    Message msgThinkingNotNull = new Message();
    msgThinkingNotNull.setThinking("thinking");

    assertNotEquals(msgThinkingNull, msgThinkingNotNull);
    assertNotEquals(msgThinkingNotNull, msgThinkingNull);

    // Test searchResults null vs non-null
    Message msgSearchNull = new Message();
    msgSearchNull.setSearchResults(null);

    Message msgSearchNotNull = new Message();
    msgSearchNotNull.setSearchResults("results");

    assertNotEquals(msgSearchNull, msgSearchNotNull);
    assertNotEquals(msgSearchNotNull, msgSearchNull);

    // Test createdAt null vs non-null
    Message msgTimeNull = new Message();
    msgTimeNull.setCreatedAt(null);

    Message msgTimeNotNull = new Message();
    msgTimeNotNull.setCreatedAt(LocalDateTime.now());

    assertNotEquals(msgTimeNull, msgTimeNotNull);
    assertNotEquals(msgTimeNotNull, msgTimeNull);
  }

  @Test
  void testHashCodeEdgeCases() {
    // Test hashCode with various null combinations
    Message msg1 = new Message();
    msg1.setId(null);
    msg1.setConversationId(null);
    msg1.setRole(null);
    msg1.setContent(null);
    msg1.setThinking(null);
    msg1.setSearchResults(null);
    msg1.setCreatedAt(null);

    Message msg2 = new Message();
    msg2.setId(null);
    msg2.setConversationId(null);
    msg2.setRole(null);
    msg2.setContent(null);
    msg2.setThinking(null);
    msg2.setSearchResults(null);
    msg2.setCreatedAt(null);

    assertEquals(msg1.hashCode(), msg2.hashCode());

    // Test hashCode with partial null values
    Message msg3 = new Message();
    msg3.setId(1L);
    msg3.setConversationId(null);
    msg3.setRole("user");
    msg3.setContent(null);
    msg3.setThinking("thinking");
    msg3.setSearchResults(null);
    msg3.setCreatedAt(LocalDateTime.of(2023, 1, 1, 12, 0));

    Message msg4 = new Message();
    msg4.setId(1L);
    msg4.setConversationId(null);
    msg4.setRole("user");
    msg4.setContent(null);
    msg4.setThinking("thinking");
    msg4.setSearchResults(null);
    msg4.setCreatedAt(LocalDateTime.of(2023, 1, 1, 12, 0));

    assertEquals(msg3.hashCode(), msg4.hashCode());
  }

  @Test
  void testEqualsSpecialCases() {
    Message msg = new Message();
    msg.setId(1L);

    // Test with completely different object types
    Object differentObject = new Object();
    assertNotEquals(msg, differentObject);

    // Test canEqual with different object type
    assertFalse(msg.canEqual(differentObject));
    assertFalse(msg.canEqual(null));

    // Test equals reflexivity with complex object
    Message complexMsg = new Message();
    complexMsg.setId(Long.MAX_VALUE);
    complexMsg.setConversationId(Long.MIN_VALUE);
    complexMsg.setRole("system");
    complexMsg.setContent("Complex content with unicode: 测试内容 🚀");
    complexMsg.setThinking("Complex thinking process");
    complexMsg.setSearchResults("Complex search results");
    complexMsg.setCreatedAt(LocalDateTime.of(2023, 12, 31, 23, 59, 59));

    assertEquals(complexMsg, complexMsg);

    // Test with extreme values
    Message extremeMsg1 = new Message();
    extremeMsg1.setId(0L);
    extremeMsg1.setConversationId(0L);
    extremeMsg1.setRole("");
    extremeMsg1.setContent("");
    extremeMsg1.setThinking("");
    extremeMsg1.setSearchResults("");
    extremeMsg1.setCreatedAt(LocalDateTime.MIN);

    Message extremeMsg2 = new Message();
    extremeMsg2.setId(0L);
    extremeMsg2.setConversationId(0L);
    extremeMsg2.setRole("");
    extremeMsg2.setContent("");
    extremeMsg2.setThinking("");
    extremeMsg2.setSearchResults("");
    extremeMsg2.setCreatedAt(LocalDateTime.MIN);

    assertEquals(extremeMsg1, extremeMsg2);
    assertEquals(extremeMsg1.hashCode(), extremeMsg2.hashCode());

    // Test additional edge cases for better coverage
    Message msgWithMaxValues = new Message();
    msgWithMaxValues.setId(Long.MAX_VALUE);
    msgWithMaxValues.setConversationId(Long.MAX_VALUE);
    msgWithMaxValues.setRole("very_long_role_name_that_exceeds_normal_expectations");

    StringBuilder longContent = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      longContent.append("Very long content ");
    }
    msgWithMaxValues.setContent(longContent.toString());

    StringBuilder longThinking = new StringBuilder();
    for (int i = 0; i < 50; i++) {
      longThinking.append("Very long thinking ");
    }
    msgWithMaxValues.setThinking(longThinking.toString());

    StringBuilder longSearchResults = new StringBuilder();
    for (int i = 0; i < 30; i++) {
      longSearchResults.append("Very long search results ");
    }
    msgWithMaxValues.setSearchResults(longSearchResults.toString());
    msgWithMaxValues.setCreatedAt(LocalDateTime.MAX);

    assertNotNull(msgWithMaxValues.toString());
    assertNotEquals(0, msgWithMaxValues.hashCode());
  }

  @Test
  void testEqualsWithCanEqualFalse() {
    // Create a mock object that returns false for canEqual
    Message msg1 =
        new Message() {
          @Override
          public boolean canEqual(Object other) {
            return false;
          }
        };
    msg1.setId(1L);
    msg1.setContent("test");

    Message msg2 = new Message();
    msg2.setId(1L);
    msg2.setContent("test");

    // Test the canEqual method directly
    assertFalse(msg1.canEqual(msg2));
    assertTrue(msg2.canEqual(msg1));

    // Test equals - msg2.equals(msg1) should return false because msg1.canEqual(msg2) returns false
    assertEquals(msg1, msg2); // msg1.equals(msg2) - msg1 doesn't check canEqual on msg2
    assertNotEquals(
        msg2, msg1); // msg2.equals(msg1) - msg2 calls msg1.canEqual(msg2) which returns false
  }

  @Test
  void testEqualsWithDifferentClassTypes() {
    Message msg = new Message();
    msg.setId(1L);

    // Test with a class that extends Message but has different canEqual behavior
    Object differentTypeObject =
        new Object() {
          @Override
          public boolean equals(Object obj) {
            return obj instanceof Message;
          }
        };

    assertNotEquals(msg, differentTypeObject);

    // Test with String
    assertNotEquals(msg, "string");

    // Test with Integer
    assertNotEquals(msg, 123);

    // Test with null
    assertNotEquals(msg, null);
  }
}
