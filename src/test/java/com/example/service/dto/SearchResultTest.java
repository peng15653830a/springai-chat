package com.example.service.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SearchResult测试
 *
 * @author xupeng
 */
class SearchResultTest {

  @Test
  void shouldCreateSearchResult() {
    // Given
    String title = "测试标题";
    String content = "测试内容";
    String url = "https://test.com";
    Double score = 0.95;

    // When
    SearchResult result = new SearchResult(title, content, url, score);

    // Then
    assertEquals(title, result.getTitle());
    assertEquals(content, result.getContent());
    assertEquals(url, result.getUrl());
    assertEquals(score, result.getScore());
  }

  @Test
  void shouldCreateEmptySearchResult() {
    // When
    SearchResult result = new SearchResult();

    // Then
    assertNull(result.getTitle());
    assertNull(result.getContent());
    assertNull(result.getUrl());
    assertNull(result.getScore());
  }

  @Test
  void shouldSetProperties() {
    // Given
    SearchResult result = new SearchResult();

    // When
    result.setTitle("新标题");
    result.setContent("新内容");
    result.setUrl("https://new.com");
    result.setScore(0.8);

    // Then
    assertEquals("新标题", result.getTitle());
    assertEquals("新内容", result.getContent());
    assertEquals("https://new.com", result.getUrl());
    assertEquals(0.8, result.getScore());
  }

  @Test
  void shouldTestEquality() {
    // Given
    SearchResult result1 = new SearchResult("title", "content", "url", 0.9);
    SearchResult result2 = new SearchResult("title", "content", "url", 0.9);
    SearchResult result3 = new SearchResult("different", "content", "url", 0.9);

    // Then
    assertEquals(result1, result2);
    assertNotEquals(result1, result3);
    assertEquals(result1.hashCode(), result2.hashCode());
  }

  @Test
  void shouldTestToString() {
    // Given
    SearchResult result = new SearchResult("title", "content", "url", 0.9);

    // When
    String toString = result.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("title"));
    assertTrue(toString.contains("content"));
    assertTrue(toString.contains("url"));
    assertTrue(toString.contains("0.9"));
  }

  @Test
  void shouldHandleNullValues() {
    // Given
    SearchResult result = new SearchResult(null, null, null, null);

    // Then
    assertNull(result.getTitle());
    assertNull(result.getContent());
    assertNull(result.getUrl());
    assertNull(result.getScore());
  }

  @Test
  void shouldCompareScores() {
    // Given
    SearchResult highScore = new SearchResult("title1", "content1", "url1", 0.9);
    SearchResult lowScore = new SearchResult("title2", "content2", "url2", 0.5);

    // Then
    assertTrue(highScore.getScore() > lowScore.getScore());
  }
}