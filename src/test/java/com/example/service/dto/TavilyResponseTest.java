package com.example.service.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TavilyResponse测试
 *
 * @author xupeng
 */
class TavilyResponseTest {

  @Test
  void shouldCreateTavilyResponse() {
    // Given
    String query = "AI technology";
    String answer = "AI is advancing rapidly";
    
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("AI News");
    result1.setUrl("https://ai-news.com");
    result1.setContent("Latest AI developments");
    result1.setScore(0.95);
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("Tech Update");
    result2.setUrl("https://tech-update.com");
    result2.setContent("Technology trends");
    result2.setScore(0.87);
    
    List<TavilyResponse.TavilySearchResult> results = Arrays.asList(result1, result2);

    // When
    TavilyResponse response = new TavilyResponse();
    response.setQuery(query);
    response.setAnswer(answer);
    response.setResults(results);

    // Then
    assertEquals(query, response.getQuery());
    assertEquals(answer, response.getAnswer());
    assertEquals(2, response.getResults().size());
    assertEquals("AI News", response.getResults().get(0).getTitle());
    assertEquals("Tech Update", response.getResults().get(1).getTitle());
  }

  @Test
  void shouldCreateTavilySearchResult() {
    // Given
    String title = "Machine Learning Guide";
    String url = "https://ml-guide.com";
    String content = "Comprehensive guide to ML";
    Double score = 0.92;

    // When
    TavilyResponse.TavilySearchResult result = new TavilyResponse.TavilySearchResult();
    result.setTitle(title);
    result.setUrl(url);
    result.setContent(content);
    result.setScore(score);

    // Then
    assertEquals(title, result.getTitle());
    assertEquals(url, result.getUrl());
    assertEquals(content, result.getContent());
    assertEquals(score, result.getScore());
  }

  @Test
  void shouldCreateEmptyTavilyResponse() {
    // When
    TavilyResponse response = new TavilyResponse();

    // Then
    assertNull(response.getQuery());
    assertNull(response.getAnswer());
    assertNull(response.getResults());
  }

  @Test
  void shouldCreateEmptyTavilySearchResult() {
    // When
    TavilyResponse.TavilySearchResult result = new TavilyResponse.TavilySearchResult();

    // Then
    assertNull(result.getTitle());
    assertNull(result.getUrl());
    assertNull(result.getContent());
    assertNull(result.getScore());
  }

  @Test
  void shouldTestTavilyResponseEquality() {
    // Given
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    response1.setAnswer("answer");

    TavilyResponse response2 = new TavilyResponse();
    response2.setQuery("test");
    response2.setAnswer("answer");

    TavilyResponse response3 = new TavilyResponse();
    response3.setQuery("different");
    response3.setAnswer("answer");

    // Then
    assertEquals(response1, response2);
    assertNotEquals(response1, response3);
    assertEquals(response1.hashCode(), response2.hashCode());
  }

  @Test
  void shouldTestTavilySearchResultEquality() {
    // Given
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("title");
    result1.setScore(0.9);

    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("title");
    result2.setScore(0.9);

    TavilyResponse.TavilySearchResult result3 = new TavilyResponse.TavilySearchResult();
    result3.setTitle("different");
    result3.setScore(0.9);

    // Then
    assertEquals(result1, result2);
    assertNotEquals(result1, result3);
    assertEquals(result1.hashCode(), result2.hashCode());
  }

  @Test
  void shouldTestTavilyResponseToString() {
    // Given
    TavilyResponse response = new TavilyResponse();
    response.setQuery("AI research");
    response.setAnswer("AI is evolving");

    // When
    String toString = response.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("AI research"));
    assertTrue(toString.contains("AI is evolving"));
  }

  @Test
  void shouldTestTavilySearchResultToString() {
    // Given
    TavilyResponse.TavilySearchResult result = new TavilyResponse.TavilySearchResult();
    result.setTitle("AI Article");
    result.setUrl("https://ai.com");
    result.setScore(0.85);

    // When
    String toString = result.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("AI Article"));
    assertTrue(toString.contains("https://ai.com"));
    assertTrue(toString.contains("0.85"));
  }

  @Test
  void shouldHandleNullResults() {
    // Given
    TavilyResponse response = new TavilyResponse();

    // When
    response.setResults(null);

    // Then
    assertNull(response.getResults());
  }

  @Test
  void shouldHandleEmptyResults() {
    // Given
    TavilyResponse response = new TavilyResponse();

    // When
    response.setResults(Arrays.asList());

    // Then
    assertNotNull(response.getResults());
    assertTrue(response.getResults().isEmpty());
  }

  @Test
  void shouldCreateTavilySearchResultWithAllArgsConstructor() {
    // Given
    String title = "Deep Learning";
    String url = "https://deeplearning.ai";
    String content = "Neural networks explained";
    Double score = 0.98;

    // When
    TavilyResponse.TavilySearchResult result = 
        new TavilyResponse.TavilySearchResult(title, url, content, null, null, score);

    // Then
    assertEquals(title, result.getTitle());
    assertEquals(url, result.getUrl());
    assertEquals(content, result.getContent());
    assertEquals(score, result.getScore());
  }

  @Test
  void shouldCreateTavilyResponseWithAllArgsConstructor() {
    // Given
    String query = "quantum computing";
    String answer = "Quantum computing uses quantum mechanics";
    List<TavilyResponse.TavilySearchResult> results = Arrays.asList(
        new TavilyResponse.TavilySearchResult("Quantum Guide", "https://quantum.com", "Guide content", null, null, 0.9)
    );

    // When
    TavilyResponse response = new TavilyResponse(query, answer, null, null, results);

    // Then
    assertEquals(query, response.getQuery());
    assertEquals(answer, response.getAnswer());
    assertEquals(1, response.getResults().size());
    assertEquals("Quantum Guide", response.getResults().get(0).getTitle());
  }

  @Test
  void shouldCompareSearchResultScores() {
    // Given
    TavilyResponse.TavilySearchResult highScore = new TavilyResponse.TavilySearchResult();
    highScore.setScore(0.95);
    
    TavilyResponse.TavilySearchResult lowScore = new TavilyResponse.TavilySearchResult();
    lowScore.setScore(0.65);

    // Then
    assertTrue(highScore.getScore() > lowScore.getScore());
  }

  @Test
  void shouldHandleSpecialCharactersInContent() {
    // Given
    String specialContent = "Content with special chars: @#$%^&*()";
    TavilyResponse.TavilySearchResult result = new TavilyResponse.TavilySearchResult();

    // When
    result.setContent(specialContent);

    // Then
    assertEquals(specialContent, result.getContent());
  }
}