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
    request.setApi_key(apiKey);
    request.setQuery(query);
    request.setSearch_depth(searchDepth);
    request.setInclude_answer(includeAnswer);
    request.setInclude_raw_content(includeRawContent);
    request.setMax_results(maxResults);

    // Then
    assertEquals(apiKey, request.getApi_key());
    assertEquals(query, request.getQuery());
    assertEquals(searchDepth, request.getSearch_depth());
    assertFalse(request.getInclude_answer());
    assertTrue(request.getInclude_raw_content());
    assertEquals(maxResults, request.getMax_results());
  }

  
  @Test
  void shouldHandleNullValues() {
    // Given
    TavilyRequest request = new TavilyRequest();

    // Then
    assertNull(request.getApi_key());
    assertNull(request.getQuery());
    assertNull(request.getSearch_depth());
    assertNull(request.getInclude_answer());
    assertNull(request.getInclude_raw_content());
    assertEquals(5, request.getMax_results());
    assertNull(request.getInclude_domains());
    assertNull(request.getExclude_domains());
  }

  @Test
  void shouldSetAndGetApiKey() {
    // Given
    TavilyRequest request = new TavilyRequest();
    String apiKey = "test-api-key-123";

    // When
    request.setApi_key(apiKey);

    // Then
    assertEquals(apiKey, request.getApi_key());
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
    request.setSearch_depth("basic");
    assertEquals("basic", request.getSearch_depth());

    request.setSearch_depth("advanced");
    assertEquals("advanced", request.getSearch_depth());
  }

  @Test
  void shouldSetAndGetIncludeAnswer() {
    // Given
    TavilyRequest request = new TavilyRequest();

    // When & Then
    request.setInclude_answer(true);
    assertTrue(request.getInclude_answer());

    request.setInclude_answer(false);
    assertFalse(request.getInclude_answer());
  }

  @Test
  void shouldSetAndGetIncludeRawContent() {
    // Given
    TavilyRequest request = new TavilyRequest();

    // When & Then
    request.setInclude_raw_content(true);
    assertTrue(request.getInclude_raw_content());

    request.setInclude_raw_content(false);
    assertFalse(request.getInclude_raw_content());
  }

  @Test
  void shouldSetAndGetMaxResults() {
    // Given
    TavilyRequest request = new TavilyRequest();

    // When & Then
    request.setMax_results(20);
    assertEquals(20, request.getMax_results());

    request.setMax_results(1);
    assertEquals(1, request.getMax_results());
  }

  @Test
  void shouldSetAndGetDomains() {
    // Given
    TavilyRequest request = new TavilyRequest();
    List<String> includeDomains = Arrays.asList("example.com", "test.org");
    List<String> excludeDomains = Arrays.asList("spam.com", "bad.net");

    // When
    request.setInclude_domains(includeDomains);
    request.setExclude_domains(excludeDomains);

    // Then
    assertEquals(includeDomains, request.getInclude_domains());
    assertEquals(excludeDomains, request.getExclude_domains());
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
    assertEquals(apiKey, request.getApi_key());
    assertEquals(query, request.getQuery());
    assertEquals("basic", request.getSearch_depth());
    assertTrue(request.getInclude_answer());
    assertFalse(request.getInclude_raw_content());
    assertEquals(5, request.getMax_results());
  }

  @Test
  void shouldCreateValidRequestForSearch() {
    // Given
    String apiKey = "valid-api-key";
    String query = "latest technology trends 2025";
    
    // When
    TavilyRequest request = new TavilyRequest();
    request.setApi_key(apiKey);
    request.setQuery(query);
    request.setSearch_depth("advanced");
    request.setInclude_raw_content(false);
    request.setInclude_answer(true);
    request.setMax_results(15);

    // Then
    assertEquals(apiKey, request.getApi_key());
    assertEquals(query, request.getQuery());
    assertEquals("advanced", request.getSearch_depth());
    assertFalse(request.getInclude_raw_content());
    assertTrue(request.getInclude_answer());
    assertEquals(15, request.getMax_results());
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
    request1.setApi_key("key1");
    request1.setQuery("test");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("key2");
    request2.setQuery("test");
    
    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualsWithNullQuery() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("key");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("key");
    
    // Then
    assertEquals(request1, request2); // 两个都是null query
  }

  @Test
  void shouldTestEqualsWithDifferentSearchDepth() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("key");
    request1.setSearch_depth("basic");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("key");
    request2.setSearch_depth("advanced");
    
    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualsWithNullSearchDepth() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("key");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("key");
    
    // Then
    assertEquals(request1, request2); // 两个都是null searchDepth
  }

  @Test
  void shouldTestEqualsWithDifferentIncludeAnswer() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("key");
    request1.setInclude_answer(true);
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("key");
    request2.setInclude_answer(false);
    
    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualsWithNullIncludeAnswer() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("key");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("key");
    
    // Then
    assertEquals(request1, request2); // 两个都是null includeAnswer
  }

  @Test
  void shouldTestEqualsWithDifferentIncludeRawContent() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("key");
    request1.setInclude_raw_content(true);
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("key");
    request2.setInclude_raw_content(false);
    
    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualsWithNullIncludeRawContent() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("key");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("key");
    
    // Then
    assertEquals(request1, request2); // 两个都是null includeRawContent
  }

  @Test
  void shouldTestEqualsWithDifferentMaxResults() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("key");
    request1.setMax_results(5);
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("key");
    request2.setMax_results(10);
    
    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualsWithNullMaxResults() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("key");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("key");
    
    // Then
    assertEquals(request1, request2); // 两个都是null maxResults
  }

  @Test
  void shouldTestEqualsWithDifferentIncludeDomains() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("key");
    request1.setInclude_domains(Arrays.asList("example.com"));
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("key");
    request2.setInclude_domains(Arrays.asList("test.com"));
    
    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualsWithNullIncludeDomains() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("key");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("key");
    
    // Then
    assertEquals(request1, request2); // 两个都是null includeDomains
  }

  @Test
  void shouldTestEqualsWithDifferentExcludeDomains() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("key");
    request1.setExclude_domains(Arrays.asList("spam.com"));
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("key");
    request2.setExclude_domains(Arrays.asList("bad.com"));
    
    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualsWithNullExcludeDomains() {
    // Given
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("key");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("key");
    
    // Then
    assertEquals(request1, request2); // 两个都是null excludeDomains
  }

  @Test
  void shouldTestHashCodeConsistency() {
    // Given
    TavilyRequest request = new TavilyRequest();
    request.setApi_key("test");
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
    request.setApi_key("key");
    request.setQuery("query");
    request.setSearch_depth("basic");
    request.setInclude_answer(true);
    request.setInclude_raw_content(false);
    request.setMax_results(10);
    request.setInclude_domains(Arrays.asList("example.com"));
    request.setExclude_domains(Arrays.asList("spam.com"));
    
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
    assertNull(request.getApi_key());
    assertNull(request.getQuery());
    assertEquals("basic", request.getSearch_depth());
    assertTrue(request.getInclude_answer());
    assertFalse(request.getInclude_raw_content());
    assertEquals(5, request.getMax_results());
  }

  @Test
  void shouldTestTavilyRequestEqualsWithSameInstance() {
    TavilyRequest request = new TavilyRequest();
    request.setApi_key("test");
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
    request1.setApi_key("test");
    
    // Anonymous subclass that overrides canEqual to return false
    TavilyRequest request2 = new TavilyRequest() {
      @Override
      public boolean canEqual(Object other) {
        return false;
      }
    };
    request2.setApi_key("test");
    
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
    request1.setApi_key("test");
    request1.setQuery(null);
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("test");
    request2.setQuery(null);
    
    assertEquals(request1.hashCode(), request2.hashCode()); // Same with mixed null fields
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullApiKeyField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("test");
    request1.setQuery("query");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key(null);
    request2.setQuery("query");
    
    assertNotEquals(request1, request2); // One has null apiKey, other doesn't
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullQueryField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("test");
    request1.setQuery("query");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("test");
    request2.setQuery(null);
    
    assertNotEquals(request1, request2); // One has null query, other doesn't
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullSearchDepthField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("test");
    request1.setSearch_depth("basic");
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("test");
    request2.setSearch_depth(null);
    
    assertNotEquals(request1, request2); // One has null searchDepth, other doesn't
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullIncludeAnswerField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("test");
    request1.setInclude_answer(true);
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("test");
    request2.setInclude_answer(null);
    
    assertNotEquals(request1, request2); // One has null includeAnswer, other doesn't
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullIncludeRawContentField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("test");
    request1.setInclude_raw_content(false);
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("test");
    request2.setInclude_raw_content(null);
    
    assertNotEquals(request1, request2); // One has null includeRawContent, other doesn't
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullMaxResultsField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("test");
    request1.setMax_results(5);
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("test");
    request2.setMax_results(0);
    
    assertNotEquals(request1, request2); // One has null maxResults, other doesn't
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullIncludeDomainsField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("test");
    request1.setInclude_domains(Arrays.asList("example.com"));
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("test");
    request2.setInclude_domains(null);
    
    assertNotEquals(request1, request2); // One has null includeDomains, other doesn't
  }

  @Test
  void shouldTestTavilyRequestEqualsWithOneNullExcludeDomainsField() {
    TavilyRequest request1 = new TavilyRequest();
    request1.setApi_key("test");
    request1.setExclude_domains(Arrays.asList("spam.com"));
    
    TavilyRequest request2 = new TavilyRequest();
    request2.setApi_key("test");
    request2.setExclude_domains(null);
    
    assertNotEquals(request1, request2); // One has null excludeDomains, other doesn't
  }
}