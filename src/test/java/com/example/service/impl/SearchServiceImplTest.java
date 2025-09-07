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
      // 修复Mockito参数匹配问题
      when(objectMapper.readValue(anyString(), eq(TavilyResponse.class))).thenThrow(new RuntimeException("JSON parsing failed"));

      try (MockedStatic<org.apache.http.util.EntityUtils> entityUtilsMock = mockStatic(org.apache.http.util.EntityUtils.class)) {
        // 修复EntityUtils.toString参数问题
        entityUtilsMock.when(() -> org.apache.http.util.EntityUtils.toString(any(HttpEntity.class))).thenReturn(invalidJson);

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

  @Test
  void shouldHandleResponseWithSpecialCharacters() throws Exception {
    // Given
    String query = "特殊字符测试";
    String responseJson = "{\"answer\":\"AI摘要🌟🔍🚀\",\"results\":[{\"title\":\"特殊标题🌟\",\"url\":\"https://test.com\",\"content\":\"特殊内容🔍\",\"score\":0.9}]}";
    
    TavilyResponse.TavilySearchResult searchResult = new TavilyResponse.TavilySearchResult();
    searchResult.setTitle("特殊标题🌟");
    searchResult.setUrl("https://test.com");
    searchResult.setContent("特殊内容🔍");
    searchResult.setScore(0.9);
    
    TavilyResponse tavilyResponse = new TavilyResponse();
    tavilyResponse.setAnswer("AI摘要🌟🔍🚀");
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
        assertEquals(2, results.size());
        assertEquals("AI 摘要", results.get(0).getTitle());
        assertEquals("AI摘要🌟🔍🚀", results.get(0).getSnippet());
        assertEquals("特殊标题🌟", results.get(1).getTitle());
        assertEquals("特殊内容🔍", results.get(1).getContent());
      }
    }
  }

  @Test
  void shouldHandleResponseWithLongContent() throws Exception {
    // Given
    String query = "长内容测试";
    StringBuilder longContent = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longContent.append("这是很长的内容，用来测试处理长文本的能力。");
    }
    String longText = longContent.toString();
    
    String responseJson = "{\"answer\":\"" + longText + "\",\"results\":[{\"title\":\"" + longText + "\",\"url\":\"https://test.com\",\"content\":\"" + longText + "\",\"score\":0.9}]}";
    
    TavilyResponse.TavilySearchResult searchResult = new TavilyResponse.TavilySearchResult();
    searchResult.setTitle(longText);
    searchResult.setUrl("https://test.com");
    searchResult.setContent(longText);
    searchResult.setScore(0.9);
    
    TavilyResponse tavilyResponse = new TavilyResponse();
    tavilyResponse.setAnswer(longText);
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
        assertEquals(2, results.size());
        assertEquals("AI 摘要", results.get(0).getTitle());
        assertEquals(longText, results.get(0).getSnippet());
        assertEquals(longText, results.get(1).getTitle());
        assertEquals(longText, results.get(1).getContent());
      }
    }
  }

  @Test
  void shouldHandleResponseWithUnicodeCharacters() throws Exception {
    // Given
    String query = "Unicode测试";
    String responseJson = "{\"answer\":\"AI摘要：测试中文\",\"results\":[{\"title\":\"标题：测试中文\",\"url\":\"https://test.com\",\"content\":\"内容：测试中文\",\"score\":0.9}]}";
    
    TavilyResponse.TavilySearchResult searchResult = new TavilyResponse.TavilySearchResult();
    searchResult.setTitle("标题：测试中文");
    searchResult.setUrl("https://test.com");
    searchResult.setContent("内容：测试中文");
    searchResult.setScore(0.9);
    
    TavilyResponse tavilyResponse = new TavilyResponse();
    tavilyResponse.setAnswer("AI摘要：测试中文");
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
        assertEquals(2, results.size());
        assertEquals("AI 摘要", results.get(0).getTitle());
        assertEquals("AI摘要：测试中文", results.get(0).getSnippet());
        assertEquals("标题：测试中文", results.get(1).getTitle());
        assertEquals("内容：测试中文", results.get(1).getContent());
      }
    }
  }

  @Test
  void shouldFormatSearchResultsWithSpecialCharacters() {
    // Given
    SearchResult result1 = new SearchResult("特殊标题🌟", "特殊内容🔍", "https://test1.com", null, "特殊内容🔍");
    SearchResult result2 = new SearchResult("另一个标题🚀", "更多内容📖", "https://test2.com", null, "更多内容📖");
    List<SearchResult> results = Arrays.asList(result1, result2);

    // When
    String formatted = searchService.formatSearchResults(results);

    // Then
    assertNotNull(formatted);
    assertTrue(formatted.contains("搜索结果："));
    assertTrue(formatted.contains("1. 特殊标题🌟"));
    assertTrue(formatted.contains("特殊内容🔍"));
    assertTrue(formatted.contains("https://test1.com"));
    assertTrue(formatted.contains("2. 另一个标题🚀"));
    assertTrue(formatted.contains("更多内容📖"));
    assertTrue(formatted.contains("https://test2.com"));
  }

  @Test
  void shouldFormatSearchResultsWithLongContent() {
    // Given
    StringBuilder longContent = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longContent.append("这是很长的内容，用来测试处理长文本的能力。");
    }
    String longText = longContent.toString();
    
    SearchResult result1 = new SearchResult(longText, longText, "https://test1.com", null, longText);
    SearchResult result2 = new SearchResult(longText, longText, "https://test2.com", null, longText);
    List<SearchResult> results = Arrays.asList(result1, result2);

    // When
    String formatted = searchService.formatSearchResults(results);

    // Then
    assertNotNull(formatted);
    assertTrue(formatted.contains("搜索结果："));
    assertTrue(formatted.contains("1. " + longText));
    assertTrue(formatted.contains("2. " + longText));
  }

  @Test
  void shouldFormatSearchResultsWithUnicode() {
    // Given
    SearchResult result1 = new SearchResult("标题：测试中文", "内容：测试中文", "https://test1.com", null, "内容：测试中文");
    SearchResult result2 = new SearchResult("另一个标题：更多中文", "更多内容：中文测试", "https://test2.com", null, "更多内容：中文测试");
    List<SearchResult> results = Arrays.asList(result1, result2);

    // When
    String formatted = searchService.formatSearchResults(results);

    // Then
    assertNotNull(formatted);
    assertTrue(formatted.contains("搜索结果："));
    assertTrue(formatted.contains("1. 标题：测试中文"));
    assertTrue(formatted.contains("内容：测试中文"));
    assertTrue(formatted.contains("https://test1.com"));
    assertTrue(formatted.contains("2. 另一个标题：更多中文"));
    assertTrue(formatted.contains("更多内容：中文测试"));
    assertTrue(formatted.contains("https://test2.com"));
  }

  @Test
  void shouldSearchWhenMessageHasSpecialCharacters() {
    // When & Then
    assertTrue(searchService.shouldSearch("特殊字符🌟🔍🚀"));
    assertTrue(searchService.shouldSearch("  特殊字符  "));
    assertTrue(searchService.shouldSearch("Unicode测试：测试中文"));
  }

  @Test
  void shouldSearchWhenMessageHasLongContent() {
    // Given
    StringBuilder longMessage = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longMessage.append("这是很长的消息内容，用来测试处理长文本的能力。");
    }
    String message = longMessage.toString();

    // When & Then
    assertTrue(searchService.shouldSearch(message));
  }

  @Test
  void shouldNotSearchWhenMessageHasOnlyWhitespace() {
    // When & Then
    assertFalse(searchService.shouldSearch("   "));
    assertFalse(searchService.shouldSearch("\t\n"));
    assertFalse(searchService.shouldSearch("  \t  \n  "));
  }

  @Test
  void shouldHandleResponseWithNullFields() throws Exception {
    // Given
    String query = "null字段测试";
    String responseJson = "{\"answer\":null,\"results\":[{\"title\":null,\"url\":null,\"content\":null,\"score\":0.9}]}";
    
    TavilyResponse.TavilySearchResult searchResult = new TavilyResponse.TavilySearchResult();
    searchResult.setTitle(null);
    searchResult.setUrl(null);
    searchResult.setContent(null);
    searchResult.setScore(0.9);
    
    TavilyResponse tavilyResponse = new TavilyResponse();
    tavilyResponse.setAnswer(null);
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
        assertEquals(1, results.size()); // 只有搜索结果，没有AI摘要（因为answer为null）
        assertNull(results.get(0).getTitle());
        assertNull(results.get(0).getContent());
      }
    }
  }

  @Test
  void shouldHandleResponseWithEmptyFields() throws Exception {
    // Given - 测试空字符串字段
    String query = "空字段测试";
    String responseJson = "{\"answer\":\"\",\"results\":[{\"title\":\"\",\"url\":\"\",\"content\":\"\",\"score\":0.9}]}";
    
    TavilyResponse.TavilySearchResult searchResult = new TavilyResponse.TavilySearchResult();
    searchResult.setTitle("");
    searchResult.setUrl("");
    searchResult.setContent("");
    searchResult.setScore(0.9);
    
    TavilyResponse tavilyResponse = new TavilyResponse();
    tavilyResponse.setAnswer("");
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
        assertEquals("", results.get(0).getTitle());
        assertEquals("", results.get(0).getContent());
      }
    }
  }

  @Test
  void shouldHandleResponseWithWhitespaceFields() throws Exception {
    // Given - 测试只有空白字符的字段
    String query = "空白字段测试";
    String responseJson = "{\"answer\":\"   \",\"results\":[{\"title\":\"   \",\"url\":\"   \",\"content\":\"   \",\"score\":0.9}]}";
    
    TavilyResponse.TavilySearchResult searchResult = new TavilyResponse.TavilySearchResult();
    searchResult.setTitle("   ");
    searchResult.setUrl("   ");
    searchResult.setContent("   ");
    searchResult.setScore(0.9);
    
    TavilyResponse tavilyResponse = new TavilyResponse();
    tavilyResponse.setAnswer("   ");
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
        assertEquals(1, results.size()); // 只有搜索结果
        assertEquals("   ", results.get(0).getTitle());
        assertEquals("   ", results.get(0).getContent());
      }
    }
  }

  @Test
  void shouldHandleVeryLongQuery() {
    // Given
    StringBuilder longQuery = new StringBuilder();
    for (int i = 0; i < 10000; i++) {
      longQuery.append("这是很长的查询内容，用来测试处理长查询的能力。");
    }
    String query = longQuery.toString();

    // When
    List<SearchResult> results = searchService.searchMetaso(query);

    // Then
    assertNotNull(results);
    // 不应该抛出异常
  }

  @Test
  void shouldHandleExceptionInHttpClientCreation() {
    // Given
    String query = "测试查询";
    
    try (MockedStatic<HttpClients> httpClientsMock = mockStatic(HttpClients.class)) {
      httpClientsMock.when(HttpClients::createDefault).thenThrow(new RuntimeException("HTTP client creation failed"));

      // When
      List<SearchResult> results = searchService.searchMetaso(query);

      // Then
      assertNotNull(results);
      assertTrue(results.isEmpty());
    }
  }

  @Test
  void shouldHandleExceptionInObjectMapperWriteValue() throws Exception {
    // Given
    String query = "测试查询";

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
  void shouldHandleResponseWithInvalidJsonStructure() throws Exception {
    // Given
    String query = "无效JSON测试";
    String invalidJson = "{\"invalid\": json structure}"; // 无效的JSON结构
    
    try (MockedStatic<HttpClients> httpClientsMock = mockStatic(HttpClients.class)) {
      httpClientsMock.when(HttpClients::createDefault).thenReturn(httpClient);
      
      when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(httpEntity);
      when(objectMapper.writeValueAsString(any(TavilyRequest.class))).thenReturn("{}");
      // 修复Mockito参数匹配问题
      when(objectMapper.readValue(anyString(), eq(TavilyResponse.class))).thenThrow(new RuntimeException("Invalid JSON structure"));

      try (MockedStatic<org.apache.http.util.EntityUtils> entityUtilsMock = mockStatic(org.apache.http.util.EntityUtils.class)) {
        // 修复EntityUtils.toString参数问题
        entityUtilsMock.when(() -> org.apache.http.util.EntityUtils.toString(any(HttpEntity.class))).thenReturn(invalidJson);

        // When
        List<SearchResult> results = searchService.searchMetaso(query);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
      }
    }
  }

  @Test
  void shouldHandleResponseWithMissingFields() throws Exception {
    // Given
    String query = "缺失字段测试";
    String responseJson = "{}"; // 空的JSON对象，缺少answer和results字段
    
    TavilyResponse tavilyResponse = new TavilyResponse();
    tavilyResponse.setAnswer(null);
    tavilyResponse.setResults(null);

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

  @Test
  void shouldHandleResponseWithNestedException() throws Exception {
    // Given
    String query = "嵌套异常测试";

    try (MockedStatic<HttpClients> httpClientsMock = mockStatic(HttpClients.class)) {
      httpClientsMock.when(HttpClients::createDefault).thenReturn(httpClient);
      
      when(httpClient.execute(any(HttpPost.class))).thenThrow(new RuntimeException("Nested exception", new IOException("IO error")));

      // When
      List<SearchResult> results = searchService.searchMetaso(query);

      // Then
      assertNotNull(results);
      assertTrue(results.isEmpty());
    }
  }

  @Test
  void testShouldSearch_WithSpecialCharacters() {
    // Given
    String[] specialMessages = {
      "特殊字符测试🌟🔍🚀", 
      "Unicode测试：测试中文消息",
      "!@#$%^&*()_+-=[]{}|;':\",./<>?"
    };

    // When & Then
    for (String message : specialMessages) {
      assertTrue(searchService.shouldSearch(message), "消息 '" + message + "' 应该触发搜索");
    }
  }

  @Test
  void testShouldSearch_WithLongMessage() {
    // Given
    StringBuilder longMessage = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longMessage.append("这是很长的消息内容，用来测试处理长文本的能力。");
    }
    String message = longMessage.toString();

    // When & Then
    assertTrue(searchService.shouldSearch(message), "长消息应该触发搜索");
  }

  @Test
  void testShouldSearch_WithOnlyNumbers() {
    // Given
    String[] numericMessages = {
      "123456", 
      "123.456", 
      "123,456", 
      "123-456",
      "2023年最新消息"
    };

    // When & Then
    for (String message : numericMessages) {
      assertTrue(searchService.shouldSearch(message), "数字消息 '" + message + "' 应该触发搜索");
    }
  }

  @Test
  void testShouldSearch_WithMixedContent() {
    // Given
    String[] mixedMessages = {
      "今天天气怎么样2023", 
      "最新消息🌟", 
      "查询123结果",
      "测试🔍功能"
    };

    // When & Then
    for (String message : mixedMessages) {
      assertTrue(searchService.shouldSearch(message), "混合内容消息 '" + message + "' 应该触发搜索");
    }
  }

  @Test
  void testShouldSearch_WithPunctuation() {
    // Given
    String[] punctuatedMessages = {
      "你好，世界！", 
      "这是什么？", 
      "真的吗？！",
      "测试...省略号"
    };

    // When & Then
    for (String message : punctuatedMessages) {
      assertTrue(searchService.shouldSearch(message), "标点符号消息 '" + message + "' 应该触发搜索");
    }
  }

  @Test
  void testShouldSearch_WithWhitespaceVariations() {
    // Given
    String[] whitespaceMessages = {
      "  前后有空格  ", 
      "\t制表符\t", 
      "\n换行符\n",
      "   \t  \n  混合空白字符  \t  \n  "
    };

    // When & Then
    for (String message : whitespaceMessages) {
      assertTrue(searchService.shouldSearch(message), "空白字符消息 '" + message + "' 应该触发搜索");
    }
  }

  @Test
  void testShouldSearch_WithSingleCharacters() {
    // Given
    String[] singleCharMessages = {
      "a", 
      "中", 
      "🌟", 
      "1",
      "?"
    };

    // When & Then
    for (String message : singleCharMessages) {
      assertTrue(searchService.shouldSearch(message), "单字符消息 '" + message + "' 应该触发搜索");
    }
  }

  @Test
  void testShouldSearch_WithRepeatedCharacters() {
    // Given
    String[] repeatedCharMessages = {
      "aaaaaa", 
      "中中中中中", 
      "🌟🌟🌟🌟🌟",
      "111111"
    };

    // When & Then
    for (String message : repeatedCharMessages) {
      assertTrue(searchService.shouldSearch(message), "重复字符消息 '" + message + "' 应该触发搜索");
    }
  }

  @Test
  void testFormatSearchResults_WithSpecialCharacters() {
    // Given
    List<SearchResult> results = new ArrayList<>();
    results.add(SearchResult.create("特殊标题🌟", "http://test1.com", "特殊内容🔍", null));
    results.add(SearchResult.create("Unicode测试🚀", "http://test2.com", "Unicode内容📖", null));

    // When
    String formatted = searchService.formatSearchResults(results);

    // Then
    assertNotNull(formatted);
    assertFalse(formatted.isEmpty());
    assertTrue(formatted.contains("搜索结果"));
    assertTrue(formatted.contains("特殊标题🌟"));
    assertTrue(formatted.contains("特殊内容🔍"));
    assertTrue(formatted.contains("Unicode测试🚀"));
    assertTrue(formatted.contains("Unicode内容📖"));
  }

  @Test
  void testFormatSearchResults_WithLongContent() {
    // Given
    StringBuilder longTitle = new StringBuilder();
    StringBuilder longContent = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      longTitle.append("这是很长的标题内容，用来测试处理长文本的能力。");
      longContent.append("这是很长的内容描述，用来测试处理长文本的能力。");
    }
    
    List<SearchResult> results = new ArrayList<>();
    results.add(SearchResult.create(longTitle.toString(), "http://test.com", longContent.toString(), null));

    // When
    String formatted = searchService.formatSearchResults(results);

    // Then
    assertNotNull(formatted);
    assertFalse(formatted.isEmpty());
    assertTrue(formatted.contains("搜索结果"));
    assertTrue(formatted.contains(longTitle.toString()));
    assertTrue(formatted.contains(longContent.toString()));
  }

  @Test
  void testFormatSearchResults_WithUnicode() {
    // Given
    List<SearchResult> results = new ArrayList<>();
    results.add(SearchResult.create("Unicode标题：测试中文", "http://test1.com", "Unicode内容：更多中文内容", null));
    results.add(SearchResult.create("Another title: English test", "http://test2.com", "Another content: More English", null));

    // When
    String formatted = searchService.formatSearchResults(results);

    // Then
    assertNotNull(formatted);
    assertFalse(formatted.isEmpty());
    assertTrue(formatted.contains("搜索结果"));
    assertTrue(formatted.contains("Unicode标题：测试中文"));
    assertTrue(formatted.contains("Unicode内容：更多中文内容"));
    assertTrue(formatted.contains("Another title: English test"));
    assertTrue(formatted.contains("Another content: More English"));
  }

  @Test
  void testFormatSearchResults_WithEmptyFields() {
    // Given
    List<SearchResult> results = new ArrayList<>();
    results.add(SearchResult.create("", "", "", null));
    results.add(SearchResult.create(null, null, null, null));

    // When
    String formatted = searchService.formatSearchResults(results);

    // Then
    assertNotNull(formatted);
    assertTrue(formatted.contains("搜索结果"));
    // 应该能处理空值和null值而不抛出异常
  }

  @Test
  void testFormatSearchResults_WithHtmlContent() {
    // Given
    List<SearchResult> results = new ArrayList<>();
    results.add(SearchResult.create("HTML标题", "http://test.com", "<p>HTML内容</p><b>粗体</b>", null));

    // When
    String formatted = searchService.formatSearchResults(results);

    // Then
    assertNotNull(formatted);
    assertFalse(formatted.isEmpty());
    assertTrue(formatted.contains("搜索结果"));
    assertTrue(formatted.contains("HTML标题"));
    assertTrue(formatted.contains("<p>HTML内容</p><b>粗体</b>"));
  }

  @Test
  void testFormatSearchResults_WithUrlVariations() {
    // Given
    List<SearchResult> results = new ArrayList<>();
    results.add(SearchResult.create("标题1", "http://test1.com", "内容1", null));
    results.add(SearchResult.create("标题2", "https://test2.com", "内容2", null));
    results.add(SearchResult.create("标题3", "ftp://test3.com", "内容3", null));
    results.add(SearchResult.create("标题4", "www.test4.com", "内容4", null));

    // When
    String formatted = searchService.formatSearchResults(results);

    // Then
    assertNotNull(formatted);
    assertFalse(formatted.isEmpty());
    assertTrue(formatted.contains("搜索结果"));
    assertTrue(formatted.contains("http://test1.com"));
    assertTrue(formatted.contains("https://test2.com"));
    assertTrue(formatted.contains("ftp://test3.com"));
    assertTrue(formatted.contains("www.test4.com"));
  }

  @Test
  void testFormatSearchResults_WithDuplicateResults() {
    // Given
    List<SearchResult> results = new ArrayList<>();
    SearchResult duplicateResult = SearchResult.create("重复标题", "http://test.com", "重复内容", null);
    results.add(duplicateResult);
    results.add(duplicateResult); // 添加相同的对象

    // When
    String formatted = searchService.formatSearchResults(results);

    // Then
    assertNotNull(formatted);
    assertFalse(formatted.isEmpty());
    assertTrue(formatted.contains("搜索结果"));
    assertTrue(formatted.contains("重复标题"));
    assertTrue(formatted.contains("重复内容"));
  }

  @Test
  void testFormatSearchResults_WithVeryLargeList() {
    // Given
    List<SearchResult> results = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      results.add(SearchResult.create("标题" + i, "http://test" + i + ".com", "内容" + i, null));
    }

    // When
    String formatted = searchService.formatSearchResults(results);

    // Then
    assertNotNull(formatted);
    assertFalse(formatted.isEmpty());
    assertTrue(formatted.contains("搜索结果"));
    assertTrue(formatted.contains("标题99"));
    assertTrue(formatted.contains("http://test99.com"));
  }

  @Test
  void testPerformSearchWithEvents_WithSpecialCharacters() {
    // Given
    String userMessage = "特殊字符测试🌟🔍🚀";

    // When & Then
    StepVerifier.create(searchService.performSearchWithEvents(userMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext() != null && 
            result.getSearchEvents() != null)
        .verifyComplete();
  }

  @Test
  void testPerformSearchWithEvents_WithLongMessage() {
    // Given
    StringBuilder longMessage = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longMessage.append("这是很长的消息内容，用来测试处理长文本的能力。");
    }
    String userMessage = longMessage.toString();

    // When & Then
    StepVerifier.create(searchService.performSearchWithEvents(userMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext() != null && 
            result.getSearchEvents() != null)
        .verifyComplete();
  }

  @Test
  void testPerformSearchWithEvents_WithUnicode() {
    // Given
    String userMessage = "Unicode测试：测试中文消息";

    // When & Then
    StepVerifier.create(searchService.performSearchWithEvents(userMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext() != null && 
            result.getSearchEvents() != null)
        .verifyComplete();
  }

  @Test
  void testPerformSearchWithEvents_WithEmptyMessage() {
    // Given
    String userMessage = "";

    // When & Then
    StepVerifier.create(searchService.performSearchWithEvents(userMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext() != null && 
            result.getSearchEvents() != null)
        .verifyComplete();
  }

  @Test
  void testPerformSearchWithEvents_WithWhitespaceMessage() {
    // Given
    String userMessage = "   \t\n   ";

    // When & Then
    StepVerifier.create(searchService.performSearchWithEvents(userMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext() != null && 
            result.getSearchEvents() != null)
        .verifyComplete();
  }

  @Test
  void testPerformSearchWithEvents_WithZeroLengthMessage() {
    // Given
    String userMessage = "";

    // When & Then
    StepVerifier.create(searchService.performSearchWithEvents(userMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext() != null && 
            result.getSearchEvents() != null)
        .verifyComplete();
  }

  @Test
  void testCreateSearchEvents_WithSpecialCharacters() {
    // Given
    List<SearchResult> results = Arrays.asList(
        SearchResult.create("特殊标题🌟", "https://test.com", "特殊内容🔍", null)
    );
    
    // When & Then
    StepVerifier.create(searchService.createSearchEvents(results))
        .expectNextMatches(event -> "search".equals(event.getType()))
        .expectNextMatches(event -> "search_results".equals(event.getType()))
        .expectNextMatches(event -> "search".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void testCreateSearchEvents_WithLongResults() {
    // Given
    List<SearchResult> results = new ArrayList<>();
    for (int i = 0; i < 50; i++) {
      results.add(SearchResult.create("标题" + i, "https://test" + i + ".com", "内容" + i, null));
    }
    
    // When & Then
    StepVerifier.create(searchService.createSearchEvents(results))
        .expectNextMatches(event -> "search".equals(event.getType()))
        .expectNextMatches(event -> "search_results".equals(event.getType()))
        .expectNextMatches(event -> "search".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void testCreateSearchEvents_WithUnicode() {
    // Given
    List<SearchResult> results = Arrays.asList(
        SearchResult.create("Unicode标题：测试中文", "https://test.com", "Unicode内容：更多中文", null)
    );
    
    // When & Then
    StepVerifier.create(searchService.createSearchEvents(results))
        .expectNextMatches(event -> "search".equals(event.getType()))
        .expectNextMatches(event -> "search_results".equals(event.getType()))
        .expectNextMatches(event -> "search".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void testSearchContextResult_ConstructWithNullValues() {
    // When & Then
    assertThrows(NullPointerException.class, () -> {
        new SearchService.SearchContextResult(null, null, Flux.empty());
    });
    
    assertThrows(NullPointerException.class, () -> {
        new SearchService.SearchContextResult("test", null, null);
    });
  }

  @Test
  void testSearchContextResult_ConstructWithEmptyValues() {
    // Given
    SearchService.SearchContextResult result = 
        new SearchService.SearchContextResult("", Collections.emptyList(), Flux.empty());

    // When & Then
    assertEquals("", result.getSearchContext());
    assertNotNull(result.getSearchResults());
    assertTrue(result.getSearchResults().isEmpty());
    assertNotNull(result.getSearchEvents());
  }

  @Test
  void testSearchContextResult_ConstructWithSpecialCharacters() {
    // Given
    List<SearchResult> results = Arrays.asList(
        SearchResult.create("特殊标题🌟", "https://test.com", "特殊内容🔍", null)
    );
    
    SearchService.SearchContextResult result = 
        new SearchService.SearchContextResult("特殊上下文🚀", results, Flux.empty());

    // When & Then
    assertEquals("特殊上下文🚀", result.getSearchContext());
    assertEquals(results, result.getSearchResults());
    assertNotNull(result.getSearchEvents());
  }
}