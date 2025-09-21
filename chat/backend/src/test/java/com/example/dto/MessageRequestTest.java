package com.example.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.example.dto.request.MessageRequest;
import org.junit.jupiter.api.Test;

class MessageRequestTest {

  @Test
  void testDefaultConstructor() {
    // When
    MessageRequest request = new MessageRequest();

    // Then
    assertNull(request.getContent());
    assertTrue(request.getSearchEnabled()); // 默认开启搜索
  }

  @Test
  void testSettersAndGetters() {
    // Given
    MessageRequest request = new MessageRequest();
    String content = "测试消息内容";
    Boolean searchEnabled = false;

    // When
    request.setContent(content);
    request.setSearchEnabled(searchEnabled);

    // Then
    assertEquals(content, request.getContent());
    assertEquals(searchEnabled, request.getSearchEnabled());
  }

  @Test
  void testSearchEnabledDefault() {
    // Given & When
    MessageRequest request = new MessageRequest();

    // Then
    assertTrue(request.getSearchEnabled());
  }

  @Test
  void testSetSearchEnabledToNull() {
    // Given
    MessageRequest request = new MessageRequest();

    // When
    request.setSearchEnabled(null);

    // Then
    assertNull(request.getSearchEnabled());
  }

  @Test
  void testEqualsAndHashCode() {
    // Given
    MessageRequest request1 = new MessageRequest();
    request1.setContent("test content");
    request1.setSearchEnabled(true);

    MessageRequest request2 = new MessageRequest();
    request2.setContent("test content");
    request2.setSearchEnabled(true);

    MessageRequest request3 = new MessageRequest();
    request3.setContent("different content");
    request3.setSearchEnabled(true);

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

    // Test with different searchEnabled
    MessageRequest requestDiffSearch = new MessageRequest();
    requestDiffSearch.setContent("test content");
    requestDiffSearch.setSearchEnabled(false);

    assertNotEquals(request1, requestDiffSearch);

    // Test with null content
    MessageRequest requestNullContent1 = new MessageRequest();
    requestNullContent1.setContent(null);
    requestNullContent1.setSearchEnabled(true);

    MessageRequest requestNullContent2 = new MessageRequest();
    requestNullContent2.setContent(null);
    requestNullContent2.setSearchEnabled(true);

    assertEquals(requestNullContent1, requestNullContent2);
    assertEquals(requestNullContent1.hashCode(), requestNullContent2.hashCode());

    // Test with null searchEnabled
    MessageRequest requestNullSearch1 = new MessageRequest();
    requestNullSearch1.setContent("content");
    requestNullSearch1.setSearchEnabled(null);

    MessageRequest requestNullSearch2 = new MessageRequest();
    requestNullSearch2.setContent("content");
    requestNullSearch2.setSearchEnabled(null);

    assertEquals(requestNullSearch1, requestNullSearch2);
    assertEquals(requestNullSearch1.hashCode(), requestNullSearch2.hashCode());

    // Test null vs non-null content
    MessageRequest requestContentNull = new MessageRequest();
    requestContentNull.setContent(null);

    MessageRequest requestContentNotNull = new MessageRequest();
    requestContentNotNull.setContent("content");

    assertNotEquals(requestContentNull, requestContentNotNull);
    assertNotEquals(requestContentNotNull, requestContentNull);

    // Test null vs non-null searchEnabled
    MessageRequest requestSearchNull = new MessageRequest();
    requestSearchNull.setSearchEnabled(null);

    MessageRequest requestSearchNotNull = new MessageRequest();
    requestSearchNotNull.setSearchEnabled(true);

    assertNotEquals(requestSearchNull, requestSearchNotNull);
    assertNotEquals(requestSearchNotNull, requestSearchNull);

    // Test hashCode consistency
    int hashCode1 = request1.hashCode();
    int hashCode2 = request1.hashCode();
    assertEquals(hashCode1, hashCode2);
  }

  @Test
  void testToString() {
    // Given
    MessageRequest request = new MessageRequest();
    request.setContent("test message");
    request.setSearchEnabled(false);

    // When
    String toString = request.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("content=test message"));
    assertTrue(toString.contains("searchEnabled=false"));
  }

  @Test
  void testWithNullContent() {
    // Given
    MessageRequest request = new MessageRequest();

    // When
    request.setContent(null);

    // Then
    assertNull(request.getContent());
    assertTrue(request.getSearchEnabled()); // 默认值不变
  }

  @Test
  void testWithEmptyContent() {
    // Given
    MessageRequest request = new MessageRequest();
    String emptyContent = "";

    // When
    request.setContent(emptyContent);

    // Then
    assertEquals(emptyContent, request.getContent());
  }
}
