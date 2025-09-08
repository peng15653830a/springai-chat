package com.example.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.example.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;

class LoginRequestTest {

  @Test
  void testDefaultConstructor() {
    // When
    LoginRequest request = new LoginRequest();

    // Then
    assertNull(request.getUsername());
    assertNull(request.getNickname());
  }

  @Test
  void testSettersAndGetters() {
    // Given
    LoginRequest request = new LoginRequest();
    String username = "testuser";
    String nickname = "测试用户";

    // When
    request.setUsername(username);
    request.setNickname(nickname);

    // Then
    assertEquals(username, request.getUsername());
    assertEquals(nickname, request.getNickname());
  }

  @Test
  void testWithNullValues() {
    // Given
    LoginRequest request = new LoginRequest();

    // When
    request.setUsername(null);
    request.setNickname(null);

    // Then
    assertNull(request.getUsername());
    assertNull(request.getNickname());
  }

  @Test
  void testWithEmptyValues() {
    // Given
    LoginRequest request = new LoginRequest();
    String emptyUsername = "";
    String emptyNickname = "";

    // When
    request.setUsername(emptyUsername);
    request.setNickname(emptyNickname);

    // Then
    assertEquals(emptyUsername, request.getUsername());
    assertEquals(emptyNickname, request.getNickname());
  }

  @Test
  void testEqualsAndHashCode() {
    // Given
    LoginRequest request1 = new LoginRequest();
    request1.setUsername("user1");
    request1.setNickname("用户1");

    LoginRequest request2 = new LoginRequest();
    request2.setUsername("user1");
    request2.setNickname("用户1");

    LoginRequest request3 = new LoginRequest();
    request3.setUsername("user2");
    request3.setNickname("用户1");

    // Then
    assertEquals(request1, request2);
    assertEquals(request1.hashCode(), request2.hashCode());
    assertNotEquals(request1, request3);

    // Test equals with null
    assertNotEquals(request1, null);

    // Test equals with different class
    assertNotEquals(request1, "not a request");

    // Test equals with same object
    assertEquals(request1, request1);

    
    // Test with null username
    LoginRequest requestNullUsername1 = new LoginRequest();
    requestNullUsername1.setUsername(null);
    requestNullUsername1.setNickname("nick");

    LoginRequest requestNullUsername2 = new LoginRequest();
    requestNullUsername2.setUsername(null);
    requestNullUsername2.setNickname("nick");

    assertEquals(requestNullUsername1, requestNullUsername2);
    assertEquals(requestNullUsername1.hashCode(), requestNullUsername2.hashCode());

    // Test with null nickname
    LoginRequest requestNullNickname1 = new LoginRequest();
    requestNullNickname1.setUsername("user");
    requestNullNickname1.setNickname(null);

    LoginRequest requestNullNickname2 = new LoginRequest();
    requestNullNickname2.setUsername("user");
    requestNullNickname2.setNickname(null);

    assertEquals(requestNullNickname1, requestNullNickname2);
    assertEquals(requestNullNickname1.hashCode(), requestNullNickname2.hashCode());

    // Test with both null
    LoginRequest requestBothNull1 = new LoginRequest();
    LoginRequest requestBothNull2 = new LoginRequest();

    assertEquals(requestBothNull1, requestBothNull2);
    assertEquals(requestBothNull1.hashCode(), requestBothNull2.hashCode());

    // Test null vs non-null username
    LoginRequest requestUsernameNull = new LoginRequest();
    requestUsernameNull.setUsername(null);

    LoginRequest requestUsernameNotNull = new LoginRequest();
    requestUsernameNotNull.setUsername("user");

    assertNotEquals(requestUsernameNull, requestUsernameNotNull);
    assertNotEquals(requestUsernameNotNull, requestUsernameNull);

    // Test null vs non-null nickname
    LoginRequest requestNicknameNull = new LoginRequest();
    requestNicknameNull.setNickname(null);

    LoginRequest requestNicknameNotNull = new LoginRequest();
    requestNicknameNotNull.setNickname("nick");

    assertNotEquals(requestNicknameNull, requestNicknameNotNull);
    assertNotEquals(requestNicknameNotNull, requestNicknameNull);

    // Test hashCode consistency
    int hashCode1 = request1.hashCode();
    int hashCode2 = request1.hashCode();
    assertEquals(hashCode1, hashCode2);
  }

  @Test
  void testToString() {
    // Given
    LoginRequest request = new LoginRequest();
    request.setUsername("testuser");
    request.setNickname("测试用户");

    // When
    String toString = request.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("username=testuser"));
    assertTrue(toString.contains("nickname=测试用户"));
  }

  @Test
  void testPartialData() {
    // Given
    LoginRequest request = new LoginRequest();

    // When - 只设置用户名
    request.setUsername("onlyuser");

    // Then
    assertEquals("onlyuser", request.getUsername());
    assertNull(request.getNickname());
  }

  @Test
  void testSpecialCharacters() {
    // Given
    LoginRequest request = new LoginRequest();
    String specialUsername = "user@123";
    String specialNickname = "用户#123";

    // When
    request.setUsername(specialUsername);
    request.setNickname(specialNickname);

    // Then
    assertEquals(specialUsername, request.getUsername());
    assertEquals(specialNickname, request.getNickname());
  }

  }
