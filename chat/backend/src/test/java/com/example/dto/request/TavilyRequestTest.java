package com.example.dto.request;

import com.example.dto.request.TavilyRequest;
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
  void shouldHandleNullValues() {
    // Given
    TavilyRequest request = new TavilyRequest();

    // Then
    assertNull(request.getApiKey());
    assertNull(request.getQuery());
    assertNull(request.getSearchDepth());
    assertNull(request.getIncludeAnswer());
    assertNull(request.getIncludeRawContent());
    assertEquals(5, request.getMaxResults());
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

  @Test
  void shouldTestEqualsWithNullApiKey() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setQuery("test");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setQuery("test");
    
    // Then
    assertEquals(request1, request2); // 两个都是null apiKey
  }

  @Test
  void shouldTestEqualsWithDifferentApiKey() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("key1");
    request1.setQuery("test");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("key2");
    request2.setQuery("test");
    
    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualsWithNullQuery() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("key");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("key");
    
    // Then
    assertEquals(request1, request2); // 两个都是null query
  }

  @Test
  void shouldTestEqualsWithDifferentSearchDepth() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("key");
    request1.setSearchDepth("basic");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("key");
    request2.setSearchDepth("advanced");
    
    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualsWithNullSearchDepth() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("key");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("key");
    
    // Then
    assertEquals(request1, request2); // 两个都是null searchDepth
  }

  @Test
  void shouldTestEqualsWithDifferentIncludeAnswer() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("key");
    request1.setIncludeAnswer(true);
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("key");
    request2.setIncludeAnswer(false);
    
    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualsWithNullIncludeAnswer() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("key");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("key");
    
    // Then
    assertEquals(request1, request2); // 两个都是null includeAnswer
  }

  @Test
  void shouldTestEqualsWithDifferentIncludeRawContent() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("key");
    request1.setIncludeRawContent(true);
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("key");
    request2.setIncludeRawContent(false);
    
    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualsWithNullIncludeRawContent() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("key");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("key");
    
    // Then
    assertEquals(request1, request2); // 两个都是null includeRawContent
  }

  @Test
  void shouldTestEqualsWithDifferentMaxResults() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("key");
    request1.setMaxResults(5);
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("key");
    request2.setMaxResults(10);
    
    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualsWithNullMaxResults() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("key");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("key");
    
    // Then
    assertEquals(request1, request2); // 两个都是null maxResults
  }

  @Test
  void shouldTestEqualsWithDifferentIncludeDomains() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("key");
    request1.setIncludeDomains(Arrays.asList("example.com"));
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("key");
    request2.setIncludeDomains(Arrays.asList("test.com"));
    
    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualsWithNullIncludeDomains() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("key");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("key");
    
    // Then
    assertEquals(request1, request2); // 两个都是null includeDomains
  }

  @Test
  void shouldTestEqualsWithDifferentExcludeDomains() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("key");
    request1.setExcludeDomains(Arrays.asList("spam.com"));
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("key");
    request2.setExcludeDomains(Arrays.asList("bad.com"));
    
    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualsWithNullExcludeDomains() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("key");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("key");
    
    // Then
    assertEquals(request1, request2); // 两个都是null excludeDomains
  }

  @Test
  void shouldTestHashCodeConsistency() {
    // Given
    TavilyRequest request = new TavilyRequest();
    request.setApiKey("test");
    request.setQuery("test query");
    
    int hashCode1 = request.hashCode();
    int hashCode2 = request.hashCode();
    
    // Then
    assertEquals(hashCode1, hashCode2);
  }

  @Test
  void shouldTestToStringWithNullFields() {
    // Given
    TavilyRequest request = new TavilyRequest();
    
    // When
    String toString = request.toString();
    
    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("TavilyRequest"));
  }

  @Test
  void shouldTestToStringWithAllFields() {
    // Given
    TavilyRequest request = new TavilyRequest();
    request.setApiKey("key");
    request.setQuery("query");
    request.setSearchDepth("basic");
    request.setIncludeAnswer(true);
    request.setIncludeRawContent(false);
    request.setMaxResults(10);
    request.setIncludeDomains(Arrays.asList("example.com"));
    request.setExcludeDomains(Arrays.asList("spam.com"));
    
    // When
    String toString = request.toString();
    
    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("key"));
    assertTrue(toString.contains("query"));
    assertTrue(toString.contains("basic"));
    assertTrue(toString.contains("true"));
    assertTrue(toString.contains("false"));
    assertTrue(toString.contains("10"));
    assertTrue(toString.contains("example.com"));
    assertTrue(toString.contains("spam.com"));
  }

  @Test
  void shouldCreateBasicRequestWithNullValues() {
    // When
    TavilyRequest request = TavilyRequest.createBasic(null, null);
    
    // Then
    assertNull(request.getApiKey());
    assertNull(request.getQuery());
    assertEquals("basic", request.getSearchDepth());
    assertTrue(request.getIncludeAnswer());
    assertFalse(request.getIncludeRawContent());
    assertEquals(5, request.getMaxResults());
  }

  @Test
  void shouldTestTavilyRequestEqualsWithSameInstance() {
    TavilyRequest request = new TavilyRequest();
    request.setApiKey("test");
    assertEquals(request, request); // Same instance reference
  }

  @Test
  void shouldTestTavilyRequestEqualsWithNull() {
    TavilyRequest request = new TavilyRequest();
    assertNotEquals(request, null); // Null comparison
  }

  @Test
  void shouldTestTavilyRequestEqualsWithDifferentType() {
    TavilyRequest request = new TavilyRequest();
    assertNotEquals(request, "not a TavilyRequest"); // Different type
  }

  @Test
  void shouldTestTavilyRequestEqualsWithCanEqualFalse() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("test");
    
    // Anonymous subclass that overrides canEqual to return false
    TavilyRequest request2 = new TavilyRequest() {
      @Override
      public boolean canEqual(Object other) {
        return false;
      }
    };
    request2.setApiKey("test");
    
    assertNotEquals(request1, request2); // canEqual returns false
  }

  @Test
  void shouldTestTavilyRequestHashCodeWithNullFields() {
    TavilyRequest request1 = new TavilyRequest();
    TavilyRequest request2 = new TavilyRequest();
    
    assertEquals(request1.hashCode(), request2.hashCode()); // Both have all null fields
  }

  @Test
  void shouldTestTavilyRequestHashCodeWithMixedNullFields() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("test");
    request1.setQuery(null);
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("test");
    request2.setQuery(null);
    
    assertEquals(request1.hashCode(), request2.hashCode()); // Same with mixed null fields
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullApiKeyField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("test");
    request1.setQuery("query");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey(null);
    request2.setQuery("query");
    
    assertNotEquals(request1, request2); // One has null apiKey, other doesn't
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullQueryField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("test");
    request1.setQuery("query");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("test");
    request2.setQuery(null);
    
    assertNotEquals(request1, request2); // One has null query, other doesn't
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullSearchDepthField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("test");
    request1.setSearchDepth("basic");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("test");
    request2.setSearchDepth(null);
    
    assertNotEquals(request1, request2); // One has null searchDepth, other doesn't
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullIncludeAnswerField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("test");
    request1.setIncludeAnswer(true);
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("test");
    request2.setIncludeAnswer(null);
    
    assertNotEquals(request1, request2); // One has null includeAnswer, other doesn't
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullIncludeRawContentField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("test");
    request1.setIncludeRawContent(false);
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("test");
    request2.setIncludeRawContent(null);
    
    assertNotEquals(request1, request2); // One has null includeRawContent, other doesn't
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullMaxResultsField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("test");
    request1.setMaxResults(5);
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("test");
    request2.setMaxResults(0);
    
    assertNotEquals(request1, request2); // One has null maxResults, other doesn't
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullIncludeDomainsField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("test");
    request1.setIncludeDomains(Arrays.asList("example.com"));
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("test");
    request2.setIncludeDomains(null);
    
    assertNotEquals(request1, request2); // One has null includeDomains, other doesn't
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullExcludeDomainsField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApiKey("test");
    request1.setExcludeDomains(Arrays.asList("spam.com"));
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApiKey("test");
    request2.setExcludeDomains(null);
    
    assertNotEquals(request1, request2); // One has null excludeDomains, other doesn't
  }
}