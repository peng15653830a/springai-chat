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
    assertNull(newUser.getUpdatedAt());
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
    // Test basic equals and hashCode
    User user1 = new User();
    user1.setId(1L);
    user1.setUsername("testuser");

    User user2 = new User();
    user2.setId(1L);
    user2.setUsername("testuser");

    User user3 = new User();
    user3.setId(2L);
    user3.setUsername("testuser");

    assertEquals(user1, user2);
    assertNotEquals(user1, user3);
    assertEquals(user1.hashCode(), user2.hashCode());

    // Test equals with null
    assertNotEquals(user1, null);

    // Test equals with different class
    assertNotEquals(user1, "not a user");

    // Test equals with same object
    assertEquals(user1, user1);

    // Test canEqual method
    assertTrue(user1.canEqual(user2));
    assertFalse(user1.canEqual("not a user"));
    assertFalse(user1.canEqual(null));

    // Test each field individually for equals
    // Test id field
    User userDiffId = new User();
    userDiffId.setId(2L);
    userDiffId.setUsername("testuser");
    assertNotEquals(user1, userDiffId);

    // Test username field
    User userDiffUsername = new User();
    userDiffUsername.setId(1L);
    userDiffUsername.setUsername("different");
    assertNotEquals(user1, userDiffUsername);

    // Test nickname field
    User userWithNickname1 = new User();
    userWithNickname1.setId(1L);
    userWithNickname1.setUsername("testuser");
    userWithNickname1.setNickname("Nick1");

    User userWithNickname2 = new User();
    userWithNickname2.setId(1L);
    userWithNickname2.setUsername("testuser");
    userWithNickname2.setNickname("Nick2");

    assertNotEquals(userWithNickname1, userWithNickname2);

    // Test createdAt field
    LocalDateTime time1 = LocalDateTime.of(2023, 1, 1, 12, 0);
    LocalDateTime time2 = LocalDateTime.of(2023, 1, 2, 12, 0);

    User userWithTime1 = new User();
    userWithTime1.setId(1L);
    userWithTime1.setUsername("testuser");
    userWithTime1.setCreatedAt(time1);

    User userWithTime2 = new User();
    userWithTime2.setId(1L);
    userWithTime2.setUsername("testuser");
    userWithTime2.setCreatedAt(time2);

    assertNotEquals(userWithTime1, userWithTime2);

    // Test updatedAt field
    User userWithUpdated1 = new User();
    userWithUpdated1.setId(1L);
    userWithUpdated1.setUsername("testuser");
    userWithUpdated1.setUpdatedAt(time1);

    User userWithUpdated2 = new User();
    userWithUpdated2.setId(1L);
    userWithUpdated2.setUsername("testuser");
    userWithUpdated2.setUpdatedAt(time2);

    assertNotEquals(userWithUpdated1, userWithUpdated2);

    // Test with null values in different fields
    User userNullId1 = new User();
    userNullId1.setId(null);
    userNullId1.setUsername("testuser");

    User userNullId2 = new User();
    userNullId2.setId(null);
    userNullId2.setUsername("testuser");

    assertEquals(userNullId1, userNullId2);

    // Test with all fields set
    LocalDateTime now = LocalDateTime.now();
    User fullUser1 = new User();
    fullUser1.setId(1L);
    fullUser1.setUsername("testuser");
    fullUser1.setNickname("TestNick");
    fullUser1.setCreatedAt(now);
    fullUser1.setUpdatedAt(now);

    User fullUser2 = new User();
    fullUser2.setId(1L);
    fullUser2.setUsername("testuser");
    fullUser2.setNickname("TestNick");
    fullUser2.setCreatedAt(now);
    fullUser2.setUpdatedAt(now);

    assertEquals(fullUser1, fullUser2);
    assertEquals(fullUser1.hashCode(), fullUser2.hashCode());

    // Test hashCode with null values
    User nullUser = new User();
    assertNotEquals(0, nullUser.hashCode());

    // Test hashCode consistency
    int hashCode1 = fullUser1.hashCode();
    int hashCode2 = fullUser1.hashCode();
    assertEquals(hashCode1, hashCode2);
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
    assertTrue(toString.contains("1"));
  }

  @Test
  void testNullValues() {
    // When & Then
    assertDoesNotThrow(
        () -> {
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
    assertDoesNotThrow(
        () -> {
          user.setUsername("");
          user.setNickname("");
        });

    assertEquals("", user.getUsername());
    assertEquals("", user.getNickname());
  }

  @Test
  void testEqualsEdgeCases() {
    // Test equals with mixed null and non-null values for each field
    User user1 = new User();
    user1.setId(1L);
    user1.setUsername(null);
    user1.setNickname("nick");
    user1.setCreatedAt(null);
    user1.setUpdatedAt(LocalDateTime.now());

    User user2 = new User();
    user2.setId(null);
    user2.setUsername("username");
    user2.setNickname(null);
    user2.setCreatedAt(LocalDateTime.now());
    user2.setUpdatedAt(null);

    assertNotEquals(user1, user2);

    // Test with one field null vs non-null for each field
    User userIdNull = new User();
    userIdNull.setId(null);

    User userIdNotNull = new User();
    userIdNotNull.setId(1L);

    assertNotEquals(userIdNull, userIdNotNull);
    assertNotEquals(userIdNotNull, userIdNull);

    // Test username null vs non-null
    User userUsernameNull = new User();
    userUsernameNull.setUsername(null);

    User userUsernameNotNull = new User();
    userUsernameNotNull.setUsername("user");

    assertNotEquals(userUsernameNull, userUsernameNotNull);
    assertNotEquals(userUsernameNotNull, userUsernameNull);

    // Test nickname null vs non-null
    User userNicknameNull = new User();
    userNicknameNull.setNickname(null);

    User userNicknameNotNull = new User();
    userNicknameNotNull.setNickname("nick");

    assertNotEquals(userNicknameNull, userNicknameNotNull);
    assertNotEquals(userNicknameNotNull, userNicknameNull);

    // Test createdAt null vs non-null
    User userCreatedNull = new User();
    userCreatedNull.setCreatedAt(null);

    User userCreatedNotNull = new User();
    userCreatedNotNull.setCreatedAt(LocalDateTime.now());

    assertNotEquals(userCreatedNull, userCreatedNotNull);
    assertNotEquals(userCreatedNotNull, userCreatedNull);

    // Test updatedAt null vs non-null
    User userUpdatedNull = new User();
    userUpdatedNull.setUpdatedAt(null);

    User userUpdatedNotNull = new User();
    userUpdatedNotNull.setUpdatedAt(LocalDateTime.now());

    assertNotEquals(userUpdatedNull, userUpdatedNotNull);
    assertNotEquals(userUpdatedNotNull, userUpdatedNull);
  }

  @Test
  void testHashCodeEdgeCases() {
    // Test hashCode with various null combinations
    User user1 = new User();
    user1.setId(null);
    user1.setUsername(null);
    user1.setNickname(null);
    user1.setCreatedAt(null);
    user1.setUpdatedAt(null);

    User user2 = new User();
    user2.setId(null);
    user2.setUsername(null);
    user2.setNickname(null);
    user2.setCreatedAt(null);
    user2.setUpdatedAt(null);

    assertEquals(user1.hashCode(), user2.hashCode());

    // Test hashCode with partial null values
    User user3 = new User();
    user3.setId(1L);
    user3.setUsername(null);
    user3.setNickname("nick");
    user3.setCreatedAt(null);
    user3.setUpdatedAt(LocalDateTime.of(2023, 1, 1, 12, 0));

    User user4 = new User();
    user4.setId(1L);
    user4.setUsername(null);
    user4.setNickname("nick");
    user4.setCreatedAt(null);
    user4.setUpdatedAt(LocalDateTime.of(2023, 1, 1, 12, 0));

    assertEquals(user3.hashCode(), user4.hashCode());
  }

  @Test
  void testSpecialCases() {
    User user = new User();
    user.setId(1L);

    // Test with completely different object types
    Object differentObject = new Object();
    assertNotEquals(user, differentObject);

    // Test canEqual with different object type
    assertFalse(user.canEqual(differentObject));

    // Test equals reflexivity with complex object
    User complexUser = new User();
    complexUser.setId(Long.MAX_VALUE);
    complexUser.setUsername("complex_user_with_special_chars_测试用户");
    complexUser.setNickname("Complex Nick 🚀");
    complexUser.setCreatedAt(LocalDateTime.of(2023, 12, 31, 23, 59, 59));
    complexUser.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 0, 0, 0));

    assertEquals(complexUser, complexUser);

    // Test with extreme values
    User extremeUser1 = new User();
    extremeUser1.setId(0L);
    extremeUser1.setUsername("");
    extremeUser1.setNickname("");
    extremeUser1.setCreatedAt(LocalDateTime.MIN);
    extremeUser1.setUpdatedAt(LocalDateTime.MIN);

    User extremeUser2 = new User();
    extremeUser2.setId(0L);
    extremeUser2.setUsername("");
    extremeUser2.setNickname("");
    extremeUser2.setCreatedAt(LocalDateTime.MIN);
    extremeUser2.setUpdatedAt(LocalDateTime.MIN);

    assertEquals(extremeUser1, extremeUser2);
    assertEquals(extremeUser1.hashCode(), extremeUser2.hashCode());

    // Test additional edge cases for better coverage
    User userWithMaxValues = new User();
    userWithMaxValues.setId(Long.MAX_VALUE);
    userWithMaxValues.setUsername(
        "very_long_username_that_exceeds_normal_expectations_and_contains_special_characters_测试");

    StringBuilder longNickname = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      longNickname.append("Very long nickname ");
    }
    userWithMaxValues.setNickname(longNickname.toString());
    userWithMaxValues.setCreatedAt(LocalDateTime.MAX);
    userWithMaxValues.setUpdatedAt(LocalDateTime.MAX);

    assertNotNull(userWithMaxValues.toString());
    assertNotEquals(0, userWithMaxValues.hashCode());
  }

  @Test
  void testEqualsWithCanEqualFalse() {
    // Create a mock object that returns false for canEqual
    User user1 =
        new User() {
          @Override
          public boolean canEqual(Object other) {
            return false;
          }
        };
    user1.setId(1L);
    user1.setUsername("test");

    User user2 = new User();
    user2.setId(1L);
    user2.setUsername("test");

    // Test the canEqual method directly
    assertFalse(user1.canEqual(user2));
    assertTrue(user2.canEqual(user1));

    // Test equals - user2.equals(user1) should return false because user1.canEqual(user2) returns
    // false
    assertEquals(user1, user2); // user1.equals(user2) - user1 doesn't check canEqual on user2
    assertNotEquals(
        user2,
        user1); // user2.equals(user1) - user2 calls user1.canEqual(user2) which returns false
  }
}
