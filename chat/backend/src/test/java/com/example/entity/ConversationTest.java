package com.example.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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
    LocalDateTime createdAt = LocalDateTime.now();
    LocalDateTime updatedAt = LocalDateTime.now();

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
    // Test basic equals and hashCode
    Conversation conv1 = new Conversation();
    conv1.setId(1L);
    conv1.setUserId(2L);
    conv1.setTitle("Test");

    Conversation conv2 = new Conversation();
    conv2.setId(1L);
    conv2.setUserId(2L);
    conv2.setTitle("Test");

    Conversation conv3 = new Conversation();
    conv3.setId(2L);
    conv3.setUserId(2L);
    conv3.setTitle("Test");

    assertEquals(conv1, conv2);
    assertNotEquals(conv1, conv3);
    assertEquals(conv1.hashCode(), conv2.hashCode());

    // Test equals with null
    assertNotEquals(conv1, null);

    // Test equals with different class
    assertNotEquals(conv1, "not a conversation");

    // Test equals with same object
    assertEquals(conv1, conv1);

    // Test canEqual method
    assertTrue(conv1.canEqual(conv2));
    assertFalse(conv1.canEqual("not a conversation"));
    assertFalse(conv1.canEqual(null));

    // Test each field individually for equals
    // Test id field
    Conversation convDiffId = new Conversation();
    convDiffId.setId(2L);
    convDiffId.setUserId(2L);
    convDiffId.setTitle("Test");
    assertNotEquals(conv1, convDiffId);

    // Test userId field
    Conversation convDiffUserId = new Conversation();
    convDiffUserId.setId(1L);
    convDiffUserId.setUserId(3L);
    convDiffUserId.setTitle("Test");
    assertNotEquals(conv1, convDiffUserId);

    // Test title field
    Conversation convDiffTitle = new Conversation();
    convDiffTitle.setId(1L);
    convDiffTitle.setUserId(2L);
    convDiffTitle.setTitle("Different");
    assertNotEquals(conv1, convDiffTitle);

    // Test createdAt field
    LocalDateTime time1 = LocalDateTime.of(2023, 1, 1, 12, 0);
    LocalDateTime time2 = LocalDateTime.of(2023, 1, 2, 12, 0);

    Conversation convWithTime1 = new Conversation();
    convWithTime1.setId(1L);
    convWithTime1.setUserId(2L);
    convWithTime1.setTitle("Test");
    convWithTime1.setCreatedAt(time1);

    Conversation convWithTime2 = new Conversation();
    convWithTime2.setId(1L);
    convWithTime2.setUserId(2L);
    convWithTime2.setTitle("Test");
    convWithTime2.setCreatedAt(time2);

    assertNotEquals(convWithTime1, convWithTime2);

    // Test updatedAt field
    Conversation convWithUpdated1 = new Conversation();
    convWithUpdated1.setId(1L);
    convWithUpdated1.setUserId(2L);
    convWithUpdated1.setTitle("Test");
    convWithUpdated1.setUpdatedAt(time1);

    Conversation convWithUpdated2 = new Conversation();
    convWithUpdated2.setId(1L);
    convWithUpdated2.setUserId(2L);
    convWithUpdated2.setTitle("Test");
    convWithUpdated2.setUpdatedAt(time2);

    assertNotEquals(convWithUpdated1, convWithUpdated2);

    // Test with null values in different fields
    Conversation convNullId1 = new Conversation();
    convNullId1.setId(null);
    convNullId1.setUserId(2L);
    convNullId1.setTitle("Test");

    Conversation convNullId2 = new Conversation();
    convNullId2.setId(null);
    convNullId2.setUserId(2L);
    convNullId2.setTitle("Test");

    assertEquals(convNullId1, convNullId2);

    // Test with all fields set
    LocalDateTime now = LocalDateTime.now();
    Conversation fullConv1 = new Conversation();
    fullConv1.setId(1L);
    fullConv1.setUserId(2L);
    fullConv1.setTitle("Full Test");
    fullConv1.setCreatedAt(now);
    fullConv1.setUpdatedAt(now);

    Conversation fullConv2 = new Conversation();
    fullConv2.setId(1L);
    fullConv2.setUserId(2L);
    fullConv2.setTitle("Full Test");
    fullConv2.setCreatedAt(now);
    fullConv2.setUpdatedAt(now);

    assertEquals(fullConv1, fullConv2);
    assertEquals(fullConv1.hashCode(), fullConv2.hashCode());

    // Test hashCode with null values
    Conversation nullConv = new Conversation();
    assertNotEquals(0, nullConv.hashCode());

    // Test hashCode consistency
    int hashCode1 = fullConv1.hashCode();
    int hashCode2 = fullConv1.hashCode();
    assertEquals(hashCode1, hashCode2);
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
    assertDoesNotThrow(
        () -> {
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
  void testEmptyValues() {
    // When & Then
    assertDoesNotThrow(
        () -> {
          conversation.setTitle("");
        });

    assertEquals("", conversation.getTitle());
  }

  @Test
  void testEqualsEdgeCases() {
    // Test equals with mixed null and non-null values for each field
    Conversation conv1 = new Conversation();
    conv1.setId(1L);
    conv1.setUserId(null);
    conv1.setTitle("title");
    conv1.setCreatedAt(null);
    conv1.setUpdatedAt(LocalDateTime.now());

    Conversation conv2 = new Conversation();
    conv2.setId(null);
    conv2.setUserId(2L);
    conv2.setTitle(null);
    conv2.setCreatedAt(LocalDateTime.now());
    conv2.setUpdatedAt(null);

    assertNotEquals(conv1, conv2);

    // Test with one field null vs non-null for each field
    Conversation convIdNull = new Conversation();
    convIdNull.setId(null);

    Conversation convIdNotNull = new Conversation();
    convIdNotNull.setId(1L);

    assertNotEquals(convIdNull, convIdNotNull);
    assertNotEquals(convIdNotNull, convIdNull);

    // Test userId null vs non-null
    Conversation convUserIdNull = new Conversation();
    convUserIdNull.setUserId(null);

    Conversation convUserIdNotNull = new Conversation();
    convUserIdNotNull.setUserId(1L);

    assertNotEquals(convUserIdNull, convUserIdNotNull);
    assertNotEquals(convUserIdNotNull, convUserIdNull);

    // Test title null vs non-null
    Conversation convTitleNull = new Conversation();
    convTitleNull.setTitle(null);

    Conversation convTitleNotNull = new Conversation();
    convTitleNotNull.setTitle("title");

    assertNotEquals(convTitleNull, convTitleNotNull);
    assertNotEquals(convTitleNotNull, convTitleNull);

    // Test createdAt null vs non-null
    Conversation convCreatedNull = new Conversation();
    convCreatedNull.setCreatedAt(null);

    Conversation convCreatedNotNull = new Conversation();
    convCreatedNotNull.setCreatedAt(LocalDateTime.now());

    assertNotEquals(convCreatedNull, convCreatedNotNull);
    assertNotEquals(convCreatedNotNull, convCreatedNull);

    // Test updatedAt null vs non-null
    Conversation convUpdatedNull = new Conversation();
    convUpdatedNull.setUpdatedAt(null);

    Conversation convUpdatedNotNull = new Conversation();
    convUpdatedNotNull.setUpdatedAt(LocalDateTime.now());

    assertNotEquals(convUpdatedNull, convUpdatedNotNull);
    assertNotEquals(convUpdatedNotNull, convUpdatedNull);
  }

  @Test
  void testHashCodeEdgeCases() {
    // Test hashCode with various null combinations
    Conversation conv1 = new Conversation();
    conv1.setId(null);
    conv1.setUserId(null);
    conv1.setTitle(null);
    conv1.setCreatedAt(null);
    conv1.setUpdatedAt(null);

    Conversation conv2 = new Conversation();
    conv2.setId(null);
    conv2.setUserId(null);
    conv2.setTitle(null);
    conv2.setCreatedAt(null);
    conv2.setUpdatedAt(null);

    assertEquals(conv1.hashCode(), conv2.hashCode());

    // Test hashCode with partial null values
    Conversation conv3 = new Conversation();
    conv3.setId(1L);
    conv3.setUserId(null);
    conv3.setTitle("title");
    conv3.setCreatedAt(null);
    conv3.setUpdatedAt(LocalDateTime.of(2023, 1, 1, 12, 0));

    Conversation conv4 = new Conversation();
    conv4.setId(1L);
    conv4.setUserId(null);
    conv4.setTitle("title");
    conv4.setCreatedAt(null);
    conv4.setUpdatedAt(LocalDateTime.of(2023, 1, 1, 12, 0));

    assertEquals(conv3.hashCode(), conv4.hashCode());
  }

  @Test
  void testSpecialCases() {
    Conversation conv = new Conversation();
    conv.setId(1L);

    // Test with completely different object types
    Object differentObject = new Object();
    assertNotEquals(conv, differentObject);

    // Test canEqual with different object type
    assertFalse(conv.canEqual(differentObject));

    // Test equals reflexivity with complex object
    Conversation complexConv = new Conversation();
    complexConv.setId(Long.MAX_VALUE);
    complexConv.setUserId(Long.MIN_VALUE);
    complexConv.setTitle("Complex conversation with special chars 测试对话 🚀");
    complexConv.setCreatedAt(LocalDateTime.of(2023, 12, 31, 23, 59, 59));
    complexConv.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 0, 0, 0));

    assertEquals(complexConv, complexConv);

    // Test with extreme values
    Conversation extremeConv1 = new Conversation();
    extremeConv1.setId(0L);
    extremeConv1.setUserId(0L);
    extremeConv1.setTitle("");
    extremeConv1.setCreatedAt(LocalDateTime.MIN);
    extremeConv1.setUpdatedAt(LocalDateTime.MIN);

    Conversation extremeConv2 = new Conversation();
    extremeConv2.setId(0L);
    extremeConv2.setUserId(0L);
    extremeConv2.setTitle("");
    extremeConv2.setCreatedAt(LocalDateTime.MIN);
    extremeConv2.setUpdatedAt(LocalDateTime.MIN);

    assertEquals(extremeConv1, extremeConv2);
    assertEquals(extremeConv1.hashCode(), extremeConv2.hashCode());

    // Test additional edge cases for better coverage
    Conversation convWithMaxValues = new Conversation();
    convWithMaxValues.setId(Long.MAX_VALUE);
    convWithMaxValues.setUserId(Long.MAX_VALUE);

    StringBuilder longTitle = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      longTitle.append("Very long conversation title ");
    }
    convWithMaxValues.setTitle(longTitle.toString());
    convWithMaxValues.setCreatedAt(LocalDateTime.MAX);
    convWithMaxValues.setUpdatedAt(LocalDateTime.MAX);

    assertNotNull(convWithMaxValues.toString());
    assertNotEquals(0, convWithMaxValues.hashCode());
  }

  @Test
  void testConversationTitleValidation() {
    // Test various title formats
    String[] validTitles = {
      "Simple Title",
      "Title with numbers 123",
      "Title with special chars: @#$%",
      "多语言标题测试",
      "Title with emojis 🚀💬",
      "Very long title that might be used in real conversations and should be handled properly by the system"
    };

    for (String title : validTitles) {
      assertDoesNotThrow(
          () -> {
            conversation.setTitle(title);
            assertEquals(title, conversation.getTitle());
          });
    }
  }

  @Test
  void testUserIdValidation() {
    // Test various userId values
    Long[] validUserIds = {1L, 100L, 999999L, Long.MAX_VALUE, 0L};

    for (Long userId : validUserIds) {
      assertDoesNotThrow(
          () -> {
            conversation.setUserId(userId);
            assertEquals(userId, conversation.getUserId());
          });
    }
  }

  @Test
  void testEqualsWithCanEqualFalse() {
    // Create a mock object that returns false for canEqual
    Conversation conv1 =
        new Conversation() {
          @Override
          public boolean canEqual(Object other) {
            return false;
          }
        };
    conv1.setId(1L);
    conv1.setTitle("test");

    Conversation conv2 = new Conversation();
    conv2.setId(1L);
    conv2.setTitle("test");

    // Test the canEqual method directly
    assertFalse(conv1.canEqual(conv2));
    assertTrue(conv2.canEqual(conv1));

    // Test equals - conv2.equals(conv1) should return false because conv1.canEqual(conv2) returns
    // false
    assertEquals(conv1, conv2); // conv1.equals(conv2) - conv1 doesn't check canEqual on conv2
    assertNotEquals(
        conv2,
        conv1); // conv2.equals(conv1) - conv2 calls conv1.canEqual(conv2) which returns false
  }
}
