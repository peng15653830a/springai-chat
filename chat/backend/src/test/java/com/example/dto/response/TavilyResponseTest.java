package com.example.dto.response;

import static org.junit.jupiter.api.Assertions.*;

import com.example.dto.response.TavilyResponse.TavilySearchResult;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * TavilyResponse测试
 *
 * @author xupeng
 */
class TavilyResponseTest {

  @Test
  void shouldCreateTavilyResponse() {
    // Given
    String answer = "测试答案";
    String query = "测试查询";
    double responseTime = 0.5;
    List<TavilySearchResult> results =
        Arrays.asList(
            new TavilySearchResult("标题1", "url1", "内容1", "原始内容1", "2024-01-01", 0.9),
            new TavilySearchResult("标题2", "url2", "内容2", "原始内容2", "2024-01-02", 0.8));

    // When
    TavilyResponse response = new TavilyResponse();
    response.setAnswer(answer);
    response.setQuery(query);
    response.setResponseTime(responseTime);
    response.setResults(results);

    // Then
    assertEquals(answer, response.getAnswer());
    assertEquals(query, response.getQuery());
    assertEquals(responseTime, response.getResponseTime(), 0.001);
    assertEquals(results, response.getResults());
    assertEquals(2, response.getResults().size());
  }

  @Test
  void shouldCreateTavilySearchResult() {
    // Given
    String title = "测试标题";
    String url = "https://test.com";
    String content = "测试内容";
    String rawContent = "原始内容";
    String publishedDate = "2024-01-01";
    Double score = 0.95;

    // When
    TavilySearchResult result =
        new TavilySearchResult(title, url, content, rawContent, publishedDate, score);

    // Then
    assertEquals(title, result.getTitle());
    assertEquals(url, result.getUrl());
    assertEquals(content, result.getContent());
    assertEquals(rawContent, result.getRawContent());
    assertEquals(publishedDate, result.getPublishedDate());
    assertEquals(score, result.getScore());
  }

  @Test
  void shouldConvertToSearchResult() {
    // Given
    TavilySearchResult tavilyResult =
        new TavilySearchResult("标题", "https://test.com", "内容", "原始内容", "2024-01-01", 0.9);

    // When
    SearchResult searchResult = tavilyResult.toSearchResult();

    // Then
    assertNotNull(searchResult);
    assertEquals("标题", searchResult.getTitle());
    assertEquals("https://test.com", searchResult.getUrl());
    assertEquals("内容", searchResult.getSnippet());
    assertEquals("内容", searchResult.getContent());
  }

  @Test
  void shouldHandleEmptyResults() {
    // Given
    TavilyResponse response = new TavilyResponse();
    response.setAnswer("无结果");
    response.setQuery("测试查询");
    response.setResponseTime(0.1);
    response.setResults(Arrays.asList());

    // Then
    assertEquals("无结果", response.getAnswer());
    assertEquals("测试查询", response.getQuery());
    assertEquals(0.1, response.getResponseTime(), 0.001);
    assertTrue(response.getResults().isEmpty());
  }

  @Test
  void shouldHandleNullValues() {
    // Given
    TavilySearchResult result = new TavilySearchResult();

    // When & Then
    assertNull(result.getTitle());
    assertNull(result.getUrl());
    assertNull(result.getContent());
    assertNull(result.getRawContent());
    assertNull(result.getPublishedDate());
    assertNull(result.getScore());
  }

  @Test
  void shouldCreateWithNoArgsConstructor() {
    // When
    TavilyResponse response = new TavilyResponse();
    TavilySearchResult result = new TavilySearchResult();

    // Then
    assertNotNull(response);
    assertNotNull(result);
    assertNull(response.getAnswer());
    assertNull(result.getTitle());
  }

  @Test
  void shouldCreateWithAllArgsConstructor() {
    // When
    TavilySearchResult result = new TavilySearchResult("标题", "url", "内容", "原始", "日期", 0.8);

    // Then
    assertEquals("标题", result.getTitle());
    assertEquals("url", result.getUrl());
    assertEquals("内容", result.getContent());
    assertEquals("原始", result.getRawContent());
    assertEquals("日期", result.getPublishedDate());
    assertEquals(0.8, result.getScore());
  }
}
