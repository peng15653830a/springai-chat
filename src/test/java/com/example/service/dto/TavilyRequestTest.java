package com.example.service.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TavilyRequest测试
 *
 * @author xupeng
 */
class TavilyRequestTest {

  @Test
  void shouldCreateTavilyRequest() {
    // Given
    String apiKey = "test-api-key";
    String query = "artificial intelligence news";
    String searchDepth = "advanced";
    Boolean includeAnswer = false;
    Boolean includeRawContent = true;
    Integer maxResults = 10;

    // When
    TavilyRequest request = new TavilyRequest();
    request.setApiKey(apiKey);
    request.setQuery(query);
    request.setSearchDepth(searchDepth);
    request.setIncludeAnswer(includeAnswer);
    request.setIncludeRawContent(includeRawContent);
    request.setMaxResults(maxResults);

    // Then
    assertEquals(apiKey, request.getApiKey());
    assertEquals(query, request.getQuery());
    assertEquals(searchDepth, request.getSearchDepth());
    assertFalse(request.getIncludeAnswer());
    assertTrue(request.getIncludeRawContent());
    assertEquals(maxResults, request.getMaxResults());
  }

  @Test
  void shouldCreateWithAllArgsConstructor() {
    // Given
    String apiKey = "test-key";
    String query = "machine learning";
    String searchDepth = "basic";
    Boolean includeAnswer = true;
    Boolean includeRawContent = false;
    Integer maxResults = 5;
    List<String> includeDomains = Arrays.asList("example.com");
    List<String> excludeDomains = Arrays.asList("spam.com");

    // When
    TavilyRequest request = new TavilyRequest(apiKey, query, searchDepth, includeAnswer, includeRawContent, maxResults, includeDomains, excludeDomains);

    // Then
    assertEquals(apiKey, request.getApiKey());
    assertEquals(query, request.getQuery());
    assertEquals(searchDepth, request.getSearchDepth());
    assertTrue(request.getIncludeAnswer());
    assertFalse(request.getIncludeRawContent());
    assertEquals(maxResults, request.getMaxResults());
    assertEquals(includeDomains, request.getIncludeDomains());
    assertEquals(excludeDomains, request.getExcludeDomains());
  }

  @Test
  void shouldCreateEmptyRequest() {
    // When
    TavilyRequest request = new TavilyRequest();

    // Then
    assertNull(request.getApiKey());
    assertNull(request.getQuery());
    assertNull(request.getSearchDepth());
    assertNull(request.getIncludeAnswer());
    assertNull(request.getIncludeRawContent());
    assertNull(request.getMaxResults());
    assertNull(request.getIncludeDomains());
    assertNull(request.getExcludeDomains());
  }

  @Test
  void shouldTestEquality() {
    // Given
    TavilyRequest request1 = new TavilyRequest("key", "AI", "advanced", true, false, 10, null, null);
    TavilyRequest request2 = new TavilyRequest("key", "AI", "advanced", true, false, 10, null, null);
    TavilyRequest request3 = new TavilyRequest("key", "ML", "basic", false, true, 5, null, null);

    // Then
    assertEquals(request1, request2);
    assertNotEquals(request1, request3);
    assertEquals(request1.hashCode(), request2.hashCode());
  }

  @Test
  void shouldTestToString() {
    // Given
    TavilyRequest request = new TavilyRequest("test-key", "test query", "basic", true, false, 3, null, null);

    // When
    String toString = request.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("test query"));
    assertTrue(toString.contains("basic"));
    assertTrue(toString.contains("true"));
    assertTrue(toString.contains("false"));
    assertTrue(toString.contains("3"));
  }

  @Test
  void shouldHandleNullValues() {
    // Given
    TavilyRequest request = new TavilyRequest(null, null, null, null, null, null, null, null);

    // Then
    assertNull(request.getApiKey());
    assertNull(request.getQuery());
    assertNull(request.getSearchDepth());
    assertNull(request.getIncludeAnswer());
    assertNull(request.getIncludeRawContent());
    assertNull(request.getMaxResults());
    assertNull(request.getIncludeDomains());
    assertNull(request.getExcludeDomains());
  }

  @Test
  void shouldSetAndGetApiKey() {
    // Given
    TavilyRequest request = new TavilyRequest();
    String apiKey = "test-api-key-123";

    // When
    request.setApiKey(apiKey);

    // Then
    assertEquals(apiKey, request.getApiKey());
  }

  @Test
  void shouldSetAndGetQuery() {
    // Given
    TavilyRequest request = new TavilyRequest();
    String query = "deep learning research";

    // When
    request.setQuery(query);

    // Then
    assertEquals(query, request.getQuery());
  }

  @Test
  void shouldSetAndGetSearchDepth() {
    // Given
    TavilyRequest request = new TavilyRequest();

    // When & Then
    request.setSearchDepth("basic");
    assertEquals("basic", request.getSearchDepth());

    request.setSearchDepth("advanced");
    assertEquals("advanced", request.getSearchDepth());
  }

  @Test
  void shouldSetAndGetIncludeAnswer() {
    // Given
    TavilyRequest request = new TavilyRequest();

    // When & Then
    request.setIncludeAnswer(true);
    assertTrue(request.getIncludeAnswer());

    request.setIncludeAnswer(false);
    assertFalse(request.getIncludeAnswer());
  }

  @Test
  void shouldSetAndGetIncludeRawContent() {
    // Given
    TavilyRequest request = new TavilyRequest();

    // When & Then
    request.setIncludeRawContent(true);
    assertTrue(request.getIncludeRawContent());

    request.setIncludeRawContent(false);
    assertFalse(request.getIncludeRawContent());
  }

  @Test
  void shouldSetAndGetMaxResults() {
    // Given
    TavilyRequest request = new TavilyRequest();

    // When & Then
    request.setMaxResults(20);
    assertEquals(20, request.getMaxResults());

    request.setMaxResults(1);
    assertEquals(1, request.getMaxResults());
  }

  @Test
  void shouldSetAndGetDomains() {
    // Given
    TavilyRequest request = new TavilyRequest();
    List<String> includeDomains = Arrays.asList("example.com", "test.org");
    List<String> excludeDomains = Arrays.asList("spam.com", "bad.net");

    // When
    request.setIncludeDomains(includeDomains);
    request.setExcludeDomains(excludeDomains);

    // Then
    assertEquals(includeDomains, request.getIncludeDomains());
    assertEquals(excludeDomains, request.getExcludeDomains());
  }

  @Test
  void shouldHandleEmptyQuery() {
    // Given
    TavilyRequest request = new TavilyRequest();

    // When
    request.setQuery("");

    // Then
    assertEquals("", request.getQuery());
  }

  @Test
  void shouldCreateBasicRequest() {
    // Given
    String apiKey = "test-key";
    String query = "AI research";

    // When
    TavilyRequest request = TavilyRequest.createBasic(apiKey, query);

    // Then
    assertEquals(apiKey, request.getApiKey());
    assertEquals(query, request.getQuery());
    assertEquals("basic", request.getSearchDepth());
    assertTrue(request.getIncludeAnswer());
    assertFalse(request.getIncludeRawContent());
    assertEquals(5, request.getMaxResults());
  }

  @Test
  void shouldCreateValidRequestForSearch() {
    // Given
    String apiKey = "valid-api-key";
    String query = "latest technology trends 2025";
    
    // When
    TavilyRequest request = new TavilyRequest();
    request.setApiKey(apiKey);
    request.setQuery(query);
    request.setSearchDepth("advanced");
    request.setIncludeRawContent(false);
    request.setIncludeAnswer(true);
    request.setMaxResults(15);

    // Then
    assertEquals(apiKey, request.getApiKey());
    assertEquals(query, request.getQuery());
    assertEquals("advanced", request.getSearchDepth());
    assertFalse(request.getIncludeRawContent());
    assertTrue(request.getIncludeAnswer());
    assertEquals(15, request.getMaxResults());
  }
}