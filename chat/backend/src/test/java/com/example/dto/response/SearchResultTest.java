package com.example.dto.response;

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
    SearchResult result = new SearchResult(title, content, url, score, null);

    // Then
    assertEquals(title, result.getTitle());
    assertEquals(content, result.getSnippet());
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
    SearchResult result1 = new SearchResult("title", "content", "url", 0.9, null);
    SearchResult result2 = new SearchResult("title", "content", "url", 0.9, null);
    SearchResult result3 = new SearchResult("different", "content", "url", 0.9, null);

    // Then
    assertEquals(result1, result2);
    assertNotEquals(result1, result3);
    assertEquals(result1.hashCode(), result2.hashCode());
  }

  @Test
  void shouldTestToString() {
    // Given
    SearchResult result = new SearchResult("title", "content", "url", 0.9, null);

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
    SearchResult result = new SearchResult(null, null, null, null, null);

    // Then
    assertNull(result.getTitle());
    assertNull(result.getContent());
    assertNull(result.getUrl());
    assertNull(result.getScore());
  }

  @Test
  void shouldCompareScores() {
    // Given
    SearchResult highScore = new SearchResult("title1", "content1", "url1", 0.9, null);
    SearchResult lowScore = new SearchResult("title2", "content2", "url2", 0.5, null);

    // Then
    assertTrue(highScore.getScore() > lowScore.getScore());
  }

  @Test
  void shouldCreateBasicSearchResult() {
    // Given
    String title = "Basic Title";
    String content = "Basic Content";
    
    // When
    SearchResult result = SearchResult.create(title, null, content, null);
    
    // Then
    assertEquals(title, result.getTitle());
    assertEquals(content, result.getSnippet());
    assertNull(result.getUrl());
    assertNull(result.getScore());
  }

  @Test
  void shouldCreateCompleteSearchResult() {
    // Given
    String title = "Complete Title";
    String content = "Complete Content";
    String url = "https://complete.com";
    Double score = 0.88;
    
    // When
    SearchResult result = SearchResult.create(title, url, content, score.toString());
    
    // Then
    assertEquals(title, result.getTitle());
    assertEquals(content, result.getSnippet());
    assertEquals(url, result.getUrl());
    assertNull(result.getScore());
  }

  @Test
  void shouldCreateWithNullTitle() {
    // When
    SearchResult result = SearchResult.create(null, null, "content", null);
    
    // Then
    assertNull(result.getTitle());
    assertEquals("content", result.getSnippet());
  }

  @Test
  void shouldCreateWithNullContent() {
    // When
    SearchResult result = SearchResult.create("title", null, null, null);
    
    // Then
    assertEquals("title", result.getTitle());
    assertNull(result.getContent());
  }

  @Test
  void shouldCreateCompleteWithNullUrl() {
    // When
    SearchResult result = SearchResult.create("title", null, "content", "0.8");
    
    // Then
    assertEquals("title", result.getTitle());
    assertEquals("content", result.getSnippet());
    assertNull(result.getUrl());
    assertNull(result.getScore());
  }

  @Test
  void shouldCreateCompleteWithNullScore() {
    // When
    SearchResult result = SearchResult.create("title", "https://test.com", "content", null);
    
    // Then
    assertEquals("title", result.getTitle());
    assertEquals("content", result.getSnippet());
    assertEquals("https://test.com", result.getUrl());
    assertNull(result.getScore());
  }

  @Test
  void shouldTestEqualityWithNullTitle() {
    // Given
    SearchResult result1 = new SearchResult();
    result1.setContent("content");
    
    SearchResult result2 = new SearchResult();
    result2.setContent("content");
    
    // Then
    assertEquals(result1, result2); // 两个都是null title
  }

  @Test
  void shouldTestEqualityWithDifferentTitle() {
    // Given
    SearchResult result1 = new SearchResult();
    result1.setTitle("title1");
    result1.setContent("content");
    
    SearchResult result2 = new SearchResult();
    result2.setTitle("title2");
    result2.setContent("content");
    
    // Then
    assertNotEquals(result1, result2);
  }

  @Test
  void shouldTestEqualityWithNullContent() {
    // Given
    SearchResult result1 = new SearchResult();
    result1.setTitle("title");
    
    SearchResult result2 = new SearchResult();
    result2.setTitle("title");
    
    // Then
    assertEquals(result1, result2); // 两个都是null content
  }

  @Test
  void shouldTestEqualityWithDifferentContent() {
    // Given
    SearchResult result1 = new SearchResult();
    result1.setTitle("title");
    result1.setContent("content1");
    
    SearchResult result2 = new SearchResult();
    result2.setTitle("title");
    result2.setContent("content2");
    
    // Then
    assertNotEquals(result1, result2);
  }

  @Test
  void shouldTestEqualityWithNullUrl() {
    // Given
    SearchResult result1 = new SearchResult();
    result1.setTitle("title");
    result1.setContent("content");
    
    SearchResult result2 = new SearchResult();
    result2.setTitle("title");
    result2.setContent("content");
    
    // Then
    assertEquals(result1, result2); // 两个都是null url
  }

  @Test
  void shouldTestEqualityWithDifferentUrl() {
    // Given
    SearchResult result1 = new SearchResult();
    result1.setTitle("title");
    result1.setContent("content");
    result1.setUrl("https://test1.com");
    
    SearchResult result2 = new SearchResult();
    result2.setTitle("title");
    result2.setContent("content");
    result2.setUrl("https://test2.com");
    
    // Then
    assertNotEquals(result1, result2);
  }

  @Test
  void shouldTestEqualityWithNullScore() {
    // Given
    SearchResult result1 = new SearchResult();
    result1.setTitle("title");
    result1.setContent("content");
    result1.setUrl("https://test.com");
    
    SearchResult result2 = new SearchResult();
    result2.setTitle("title");
    result2.setContent("content");
    result2.setUrl("https://test.com");
    
    // Then
    assertEquals(result1, result2); // 两个都是null score
  }

  @Test
  void shouldTestEqualityWithDifferentScore() {
    // Given
    SearchResult result1 = new SearchResult();
    result1.setTitle("title");
    result1.setContent("content");
    result1.setUrl("https://test.com");
    result1.setScore(0.8);
    
    SearchResult result2 = new SearchResult();
    result2.setTitle("title");
    result2.setContent("content");
    result2.setUrl("https://test.com");
    result2.setScore(0.9);
    
    // Then
    assertNotEquals(result1, result2);
  }

  @Test
  void shouldTestHashCodeConsistency() {
    // Given
    SearchResult result = new SearchResult("title", "content", "url", 0.8, null);
    
    int hashCode1 = result.hashCode();
    int hashCode2 = result.hashCode();
    
    // Then
    assertEquals(hashCode1, hashCode2);
  }

  @Test
  void shouldTestToStringWithNullFields() {
    // Given
    SearchResult result = new SearchResult();
    
    // When
    String toString = result.toString();
    
    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("SearchResult"));
  }

  @Test
  void shouldHandleEmptyStrings() {
    // Given
    SearchResult result = new SearchResult("", "", "", 0.0, null);
    
    // Then
    assertEquals("", result.getTitle());
    assertEquals("", result.getSnippet());
    assertEquals("", result.getUrl());
    assertEquals(0.0, result.getScore());
  }

  @Test
  void shouldHandleNegativeScore() {
    // Given
    SearchResult result = new SearchResult("title", "content", "url", -0.5, null);
    
    // Then
    assertEquals(-0.5, result.getScore());
  }

  @Test
  void shouldHandleScoreGreaterThanOne() {
    // Given
    SearchResult result = new SearchResult("title", "content", "url", 1.5, null);
    
    // Then
    assertEquals(1.5, result.getScore());
  }

  @Test
  void shouldHandleVeryLongStrings() {
    // Given
    String longString = "a".repeat(1000);
    SearchResult result = new SearchResult(longString, longString, longString, 0.5, null);
    
    // Then
    assertEquals(longString, result.getTitle());
    assertEquals(longString, result.getSnippet());
    assertEquals(longString, result.getUrl());
  }

  @Test
  void shouldCreateWithAllNullValues() {
    // When
    SearchResult basicResult = SearchResult.create(null, null, null, null);
    SearchResult completeResult = SearchResult.create(null, null, null, null);
    
    // Then
    assertNull(basicResult.getTitle());
    assertNull(basicResult.getContent());
    assertNull(basicResult.getUrl());
    assertNull(basicResult.getScore());
    
    assertNull(completeResult.getTitle());
    assertNull(completeResult.getContent());
    assertNull(completeResult.getUrl());
    assertNull(completeResult.getScore());
  }

  @Test
  void shouldTestSearchResultEqualsWithSameInstance() {
    SearchResult result = new SearchResult();
    result.setTitle("test");
    assertEquals(result, result); // Same instance reference
  }

  @Test
  void shouldTestSearchResultEqualsWithNull() {
    SearchResult result = new SearchResult();
    assertNotEquals(result, null); // Null comparison
  }

  @Test
  void shouldTestSearchResultEqualsWithDifferentType() {
    SearchResult result = new SearchResult();
    assertNotEquals(result, "not a SearchResult"); // Different type
  }

  @Test
  void shouldTestSearchResultEqualsWithCanEqualFalse() {
    SearchResult result1 = new SearchResult();
    result1.setTitle("test");
    
    // Anonymous subclass that overrides canEqual to return false
    SearchResult result2 = new SearchResult() {
      @Override
      public boolean canEqual(Object other) {
        return false;
      }
    };
    result2.setTitle("test");
    
    assertNotEquals(result1, result2); // canEqual returns false
  }

  @Test
  void shouldTestSearchResultEqualsWithOneNullTitleField() {
    SearchResult result1 = new SearchResult();
    result1.setTitle("test");
    result1.setContent("content");
    
    SearchResult result2 = new SearchResult();
    result2.setTitle(null);
    result2.setContent("content");
    
    assertNotEquals(result1, result2); // One has null title, other doesn't
  }

  @Test
  void shouldTestSearchResultEqualsWithOneNullContentField() {
    SearchResult result1 = new SearchResult();
    result1.setTitle("test");
    result1.setContent("content");
    
    SearchResult result2 = new SearchResult();
    result2.setTitle("test");
    result2.setContent(null);
    
    assertNotEquals(result1, result2); // One has null content, other doesn't
  }

  @Test
  void shouldTestSearchResultEqualsWithOneNullUrlField() {
    SearchResult result1 = new SearchResult();
    result1.setTitle("test");
    result1.setUrl("url");
    
    SearchResult result2 = new SearchResult();
    result2.setTitle("test");
    result2.setUrl(null);
    
    assertNotEquals(result1, result2); // One has null url, other doesn't
  }

  @Test
  void shouldTestSearchResultEqualsWithOneNullScoreField() {
    SearchResult result1 = new SearchResult();
    result1.setTitle("test");
    result1.setScore(0.5);
    
    SearchResult result2 = new SearchResult();
    result2.setTitle("test");
    result2.setScore(null);
    
    assertNotEquals(result1, result2); // One has null score, other doesn't
  }

  @Test
  void shouldTestSearchResultHashCodeWithAllNullFields() {
    SearchResult result1 = new SearchResult();
    SearchResult result2 = new SearchResult();
    
    assertEquals(result1.hashCode(), result2.hashCode()); // Both have all null fields
  }
}