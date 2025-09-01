package com.example.service.impl;

import com.example.config.SearchProperties;
import com.example.dto.response.SearchResult;
import com.example.dto.request.TavilyRequest;
import com.example.dto.response.TavilyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SearchServiceImpl测试类
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private CloseableHttpClient httpClient;

  @Mock
  private CloseableHttpResponse httpResponse;

  @Mock
  private HttpEntity httpEntity;

  @Mock
  private StatusLine statusLine;

  @InjectMocks
  private SearchServiceImpl searchService;

  @BeforeEach
  void setUp() {
    SearchProperties searchProperties = new SearchProperties();
    SearchProperties.Tavily tavily = new SearchProperties.Tavily();
    tavily.setApiKey("test-api-key");
    tavily.setBaseUrl("https://api.tavily.com/search");
    searchProperties.setTavily(tavily);
    searchProperties.setEnabled(true);
    
    ReflectionTestUtils.setField(searchService, "searchProperties", searchProperties);
  }

  @Test
  void shouldReturnEmptyWhenSearchDisabled() {
    // Given
    SearchProperties searchProperties = new SearchProperties();
    SearchProperties.Tavily tavily = new SearchProperties.Tavily();
    tavily.setApiKey("test-api-key");
    tavily.setBaseUrl("https://api.tavily.com/search");
    searchProperties.setTavily(tavily);
    searchProperties.setEnabled(false);
    
    ReflectionTestUtils.setField(searchService, "searchProperties", searchProperties);

    // When
    List<SearchResult> results = searchService.searchMetaso("test query");

    // Then
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void shouldReturnEmptyWhenApiKeyNotConfigured() {
    // Given
    SearchProperties searchProperties = new SearchProperties();
    SearchProperties.Tavily tavily = new SearchProperties.Tavily();
    tavily.setApiKey("");
    tavily.setBaseUrl("https://api.tavily.com/search");
    searchProperties.setTavily(tavily);
    searchProperties.setEnabled(true);
    
    ReflectionTestUtils.setField(searchService, "searchProperties", searchProperties);

    // When
    List<SearchResult> results = searchService.searchMetaso("test query");

    // Then
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void shouldReturnEmptyWhenApiKeyIsNull() {
    // Given
    SearchProperties searchProperties = new SearchProperties();
    SearchProperties.Tavily tavily = new SearchProperties.Tavily();
    tavily.setApiKey(null);
    tavily.setBaseUrl("https://api.tavily.com/search");
    searchProperties.setTavily(tavily);
    searchProperties.setEnabled(true);
    
    ReflectionTestUtils.setField(searchService, "searchProperties", searchProperties);

    // When
    List<SearchResult> results = searchService.searchMetaso("test query");

    // Then
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void shouldPerformSuccessfulSearch() throws Exception {
    // Given
    String query = "artificial intelligence";
    String responseJson = "{\"answer\":\"AI摘要内容\",\"results\":[{\"title\":\"Test Title\",\"url\":\"https://test.com\",\"content\":\"Test content\",\"score\":0.9}]}";
    
    TavilyResponse.TavilySearchResult searchResult = new TavilyResponse.TavilySearchResult();
    searchResult.setTitle("Test Title");
    searchResult.setUrl("https://test.com");
    searchResult.setContent("Test content");
    searchResult.setScore(0.9);
    
    TavilyResponse tavilyResponse = new TavilyResponse();
    tavilyResponse.setAnswer("AI摘要内容");
    tavilyResponse.setResults(Arrays.asList(searchResult));

    try (MockedStatic<HttpClients> httpClientsMock = mockStatic(HttpClients.class)) {
      httpClientsMock.when(HttpClients::createDefault).thenReturn(httpClient);
      
      when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(httpEntity);
      when(objectMapper.writeValueAsString(any(TavilyRequest.class))).thenReturn("{}");
      when(objectMapper.readValue(eq(responseJson), eq(TavilyResponse.class))).thenReturn(tavilyResponse);

      // 创建org.apache.http.util.EntityUtils的模拟
      try (MockedStatic<org.apache.http.util.EntityUtils> entityUtilsMock = mockStatic(org.apache.http.util.EntityUtils.class)) {
        entityUtilsMock.when(() -> org.apache.http.util.EntityUtils.toString(eq(httpEntity), eq(StandardCharsets.UTF_8))).thenReturn(responseJson);

        // When
        List<SearchResult> results = searchService.searchMetaso(query);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size()); // AI摘要 + 搜索结果
        assertEquals("AI 摘要", results.get(0).getTitle());
        assertEquals("AI摘要内容", results.get(0).getSnippet());
        assertEquals("Test Title", results.get(1).getTitle());
        assertEquals("Test content", results.get(1).getContent());
      }
    }
  }

  @Test
  void shouldHandleApiError() throws Exception {
    // Given
    String query = "test query";

    try (MockedStatic<HttpClients> httpClientsMock = mockStatic(HttpClients.class)) {
      httpClientsMock.when(HttpClients::createDefault).thenReturn(httpClient);
      
      when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(statusLine.getStatusCode()).thenReturn(500);
      when(httpResponse.getEntity()).thenReturn(httpEntity);
      when(objectMapper.writeValueAsString(any(TavilyRequest.class))).thenReturn("{}");

      try (MockedStatic<org.apache.http.util.EntityUtils> entityUtilsMock = mockStatic(org.apache.http.util.EntityUtils.class)) {
        entityUtilsMock.when(() -> org.apache.http.util.EntityUtils.toString(any(HttpEntity.class), any(String.class))).thenReturn("Error response");

        // When
        List<SearchResult> results = searchService.searchMetaso(query);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
      }
    }
  }

  @Test
  void shouldHandleHttpException() throws Exception {
    // Given
    String query = "test query";

    try (MockedStatic<HttpClients> httpClientsMock = mockStatic(HttpClients.class)) {
      httpClientsMock.when(HttpClients::createDefault).thenReturn(httpClient);
      
      when(httpClient.execute(any(HttpPost.class))).thenThrow(new IOException("Connection failed"));
      when(objectMapper.writeValueAsString(any(TavilyRequest.class))).thenReturn("{}");

      // When
      List<SearchResult> results = searchService.searchMetaso(query);

      // Then
      assertNotNull(results);
      assertTrue(results.isEmpty());
    }
  }

  @Test
  void shouldHandleJsonSerializationException() throws Exception {
    // Given
    String query = "test query";

    try (MockedStatic<HttpClients> httpClientsMock = mockStatic(HttpClients.class)) {
      httpClientsMock.when(HttpClients::createDefault).thenReturn(httpClient);
      
      when(objectMapper.writeValueAsString(any(TavilyRequest.class))).thenThrow(new RuntimeException("JSON serialization failed"));

      // When
      List<SearchResult> results = searchService.searchMetaso(query);

      // Then
      assertNotNull(results);
      assertTrue(results.isEmpty());
    }
  }

  @Test
  void shouldHandleJsonParsingException() throws Exception {
    // Given
    String query = "test query";
    String invalidJson = "invalid json";

    try (MockedStatic<HttpClients> httpClientsMock = mockStatic(HttpClients.class)) {
      httpClientsMock.when(HttpClients::createDefault).thenReturn(httpClient);
      
      when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(httpEntity);
      when(objectMapper.writeValueAsString(any(TavilyRequest.class))).thenReturn("{}");
      when(objectMapper.readValue(anyString(), eq(TavilyResponse.class))).thenThrow(new RuntimeException("JSON parsing failed"));

      try (MockedStatic<org.apache.http.util.EntityUtils> entityUtilsMock = mockStatic(org.apache.http.util.EntityUtils.class)) {
        entityUtilsMock.when(() -> org.apache.http.util.EntityUtils.toString(any(HttpEntity.class), any(String.class))).thenReturn(invalidJson);

        // When
        List<SearchResult> results = searchService.searchMetaso(query);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
      }
    }
  }

  @Test
  void shouldFormatSearchResults() {
    // Given
    SearchResult result1 = new SearchResult("Title 1", "Content 1", "https://test1.com", null, "Content 1");
    SearchResult result2 = new SearchResult("Title 2", "Content 2", "https://test2.com", null, "Content 2");
    List<SearchResult> results = Arrays.asList(result1, result2);

    // When
    String formatted = searchService.formatSearchResults(results);

    // Then
    assertNotNull(formatted);
    assertTrue(formatted.contains("搜索结果："));
    assertTrue(formatted.contains("1. Title 1"));
    assertTrue(formatted.contains("Content 1"));
    assertTrue(formatted.contains("https://test1.com"));
    assertTrue(formatted.contains("2. Title 2"));
    assertTrue(formatted.contains("Content 2"));
    assertTrue(formatted.contains("https://test2.com"));
  }

  @Test
  void shouldReturnEmptyStringForNullResults() {
    // When
    String formatted = searchService.formatSearchResults(null);

    // Then
    assertEquals("", formatted);
  }

  @Test
  void shouldReturnEmptyStringForEmptyResults() {
    // Given
    List<SearchResult> emptyResults = Arrays.asList();

    // When
    String formatted = searchService.formatSearchResults(emptyResults);

    // Then
    assertEquals("", formatted);
  }

  @Test
  void shouldSearchWhenMessageIsValid() {
    // When & Then
    assertTrue(searchService.shouldSearch("valid message"));
    assertTrue(searchService.shouldSearch("  valid message  "));
    assertFalse(searchService.shouldSearch(null));
    assertFalse(searchService.shouldSearch(""));
    assertFalse(searchService.shouldSearch("   "));
  }

  @Test
  void shouldHandleResponseWithNullAnswer() throws Exception {
    // Given
    String query = "test query";
    String responseJson = "{\"answer\":null,\"results\":[{\"title\":\"Test Title\",\"url\":\"https://test.com\",\"content\":\"Test content\",\"score\":0.9}]}";
    
    TavilyResponse.TavilySearchResult searchResult = new TavilyResponse.TavilySearchResult();
    searchResult.setTitle("Test Title");
    searchResult.setUrl("https://test.com");
    searchResult.setContent("Test content");
    searchResult.setScore(0.9);
    
    TavilyResponse tavilyResponse = new TavilyResponse();
    tavilyResponse.setAnswer(null); // null answer
    tavilyResponse.setResults(Arrays.asList(searchResult));

    try (MockedStatic<HttpClients> httpClientsMock = mockStatic(HttpClients.class)) {
      httpClientsMock.when(HttpClients::createDefault).thenReturn(httpClient);
      
      when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(httpEntity);
      when(objectMapper.writeValueAsString(any(TavilyRequest.class))).thenReturn("{}");
      when(objectMapper.readValue(eq(responseJson), eq(TavilyResponse.class))).thenReturn(tavilyResponse);

      try (MockedStatic<org.apache.http.util.EntityUtils> entityUtilsMock = mockStatic(org.apache.http.util.EntityUtils.class)) {
        entityUtilsMock.when(() -> org.apache.http.util.EntityUtils.toString(eq(httpEntity), eq(StandardCharsets.UTF_8))).thenReturn(responseJson);

        // When
        List<SearchResult> results = searchService.searchMetaso(query);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size()); // 只有搜索结果，没有AI摘要
        assertEquals("Test Title", results.get(0).getTitle());
      }
    }
  }

  @Test
  void shouldHandleResponseWithEmptyResults() throws Exception {
    // Given
    String query = "test query";
    String responseJson = "{\"answer\":\"AI摘要\",\"results\":[]}";
    
    TavilyResponse tavilyResponse = new TavilyResponse();
    tavilyResponse.setAnswer("AI摘要");
    tavilyResponse.setResults(Arrays.asList()); // 空结果

    try (MockedStatic<HttpClients> httpClientsMock = mockStatic(HttpClients.class)) {
      httpClientsMock.when(HttpClients::createDefault).thenReturn(httpClient);
      
      when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(httpEntity);
      when(objectMapper.writeValueAsString(any(TavilyRequest.class))).thenReturn("{}");
      when(objectMapper.readValue(eq(responseJson), eq(TavilyResponse.class))).thenReturn(tavilyResponse);

      try (MockedStatic<org.apache.http.util.EntityUtils> entityUtilsMock = mockStatic(org.apache.http.util.EntityUtils.class)) {
        entityUtilsMock.when(() -> org.apache.http.util.EntityUtils.toString(eq(httpEntity), eq(StandardCharsets.UTF_8))).thenReturn(responseJson);

        // When
        List<SearchResult> results = searchService.searchMetaso(query);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size()); // 只有AI摘要
        assertEquals("AI 摘要", results.get(0).getTitle());
        assertEquals("AI摘要", results.get(0).getSnippet());
      }
    }
  }

  @Test
  void shouldHandleResponseWithNullResults() throws Exception {
    // Given
    String query = "test query";
    String responseJson = "{\"answer\":\"AI摘要\",\"results\":null}";
    
    TavilyResponse tavilyResponse = new TavilyResponse();
    tavilyResponse.setAnswer("AI摘要");
    tavilyResponse.setResults(null); // null results

    try (MockedStatic<HttpClients> httpClientsMock = mockStatic(HttpClients.class)) {
      httpClientsMock.when(HttpClients::createDefault).thenReturn(httpClient);
      
      when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(httpEntity);
      when(objectMapper.writeValueAsString(any(TavilyRequest.class))).thenReturn("{}");
      when(objectMapper.readValue(eq(responseJson), eq(TavilyResponse.class))).thenReturn(tavilyResponse);

      try (MockedStatic<org.apache.http.util.EntityUtils> entityUtilsMock = mockStatic(org.apache.http.util.EntityUtils.class)) {
        entityUtilsMock.when(() -> org.apache.http.util.EntityUtils.toString(eq(httpEntity), eq(StandardCharsets.UTF_8))).thenReturn(responseJson);

        // When
        List<SearchResult> results = searchService.searchMetaso(query);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size()); // 只有AI摘要
        assertEquals("AI 摘要", results.get(0).getTitle());
      }
    }
  }

  @Test
  void shouldHandleResponseWithEmptyAnswer() throws Exception {
    // Given - 测试空字符串answer，覆盖L104的!answer.isEmpty()分支
    String query = "test query";
    String responseJson = "{\"answer\":\"\",\"results\":[{\"title\":\"Test Title\",\"url\":\"https://test.com\",\"content\":\"Test content\",\"score\":0.9}]}";
    
    TavilyResponse.TavilySearchResult searchResult = new TavilyResponse.TavilySearchResult();
    searchResult.setTitle("Test Title");
    searchResult.setUrl("https://test.com");
    searchResult.setContent("Test content");
    searchResult.setScore(0.9);
    
    TavilyResponse tavilyResponse = new TavilyResponse();
    tavilyResponse.setAnswer(""); // 空字符串answer
    tavilyResponse.setResults(Arrays.asList(searchResult));

    try (MockedStatic<HttpClients> httpClientsMock = mockStatic(HttpClients.class)) {
      httpClientsMock.when(HttpClients::createDefault).thenReturn(httpClient);
      
      when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(httpEntity);
      when(objectMapper.writeValueAsString(any(TavilyRequest.class))).thenReturn("{}");
      when(objectMapper.readValue(eq(responseJson), eq(TavilyResponse.class))).thenReturn(tavilyResponse);

      try (MockedStatic<org.apache.http.util.EntityUtils> entityUtilsMock = mockStatic(org.apache.http.util.EntityUtils.class)) {
        entityUtilsMock.when(() -> org.apache.http.util.EntityUtils.toString(eq(httpEntity), eq(StandardCharsets.UTF_8))).thenReturn(responseJson);

        // When
        List<SearchResult> results = searchService.searchMetaso(query);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size()); // 只有搜索结果，没有AI摘要（因为answer为空字符串）
        assertEquals("Test Title", results.get(0).getTitle());
      }
    }
  }

  @Test
  void shouldHandleResponseWithBothNullAnswerAndNullResults() throws Exception {
    // Given - 测试answer和results都为null的情况
    String query = "test query";
    String responseJson = "{\"answer\":null,\"results\":null}";
    
    TavilyResponse tavilyResponse = new TavilyResponse();
    tavilyResponse.setAnswer(null); // null answer
    tavilyResponse.setResults(null); // null results

    try (MockedStatic<HttpClients> httpClientsMock = mockStatic(HttpClients.class)) {
      httpClientsMock.when(HttpClients::createDefault).thenReturn(httpClient);
      
      when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(httpEntity);
      when(objectMapper.writeValueAsString(any(TavilyRequest.class))).thenReturn("{}");
      when(objectMapper.readValue(eq(responseJson), eq(TavilyResponse.class))).thenReturn(tavilyResponse);

      try (MockedStatic<org.apache.http.util.EntityUtils> entityUtilsMock = mockStatic(org.apache.http.util.EntityUtils.class)) {
        entityUtilsMock.when(() -> org.apache.http.util.EntityUtils.toString(eq(httpEntity), eq(StandardCharsets.UTF_8))).thenReturn(responseJson);

        // When
        List<SearchResult> results = searchService.searchMetaso(query);

        // Then
        assertNotNull(results);
        assertEquals(0, results.size()); // 没有AI摘要也没有搜索结果
      }
    }
  }

}