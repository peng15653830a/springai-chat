package com.example.service.dto;

import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

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

  @Test
  void shouldConvertToSearchResult() {
    // Given
    TavilyResponse.TavilySearchResult tavilyResult = new TavilyResponse.TavilySearchResult();
    tavilyResult.setTitle("AI Research");
    tavilyResult.setContent("Latest AI research findings");
    tavilyResult.setUrl("https://ai-research.com");
    tavilyResult.setScore(0.93);
    
    // When
    SearchResult searchResult = tavilyResult.toSearchResult();
    
    // Then
    assertNotNull(searchResult);
    assertEquals("AI Research", searchResult.getTitle());
    assertEquals("Latest AI research findings", searchResult.getContent());
    assertEquals("https://ai-research.com", searchResult.getUrl());
    assertEquals(0.93, searchResult.getScore());
  }

  @Test
  void shouldConvertToSearchResultWithNullValues() {
    // Given
    TavilyResponse.TavilySearchResult tavilyResult = new TavilyResponse.TavilySearchResult();
    
    // When
    SearchResult searchResult = tavilyResult.toSearchResult();
    
    // Then
    assertNotNull(searchResult);
    assertNull(searchResult.getTitle());
    assertNull(searchResult.getContent());
    assertNull(searchResult.getUrl());
    assertNull(searchResult.getScore());
  }

  @Test
  void shouldTestTavilyResponseEqualityWithNullQuery() {
    // Given
    TavilyResponse response1 = new TavilyResponse();
    response1.setAnswer("answer");
    
    TavilyResponse response2 = new TavilyResponse();
    response2.setAnswer("answer");
    
    // Then
    assertEquals(response1, response2);
  }

  @Test
  void shouldTestTavilyResponseEqualityWithDifferentAnswer() {
    // Given
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    response1.setAnswer("answer1");
    
    TavilyResponse response2 = new TavilyResponse();
    response2.setQuery("test");
    response2.setAnswer("answer2");
    
    // Then
    assertNotEquals(response1, response2);
  }

  @Test
  void shouldTestTavilyResponseEqualityWithNullAnswer() {
    // Given
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    
    TavilyResponse response2 = new TavilyResponse();
    response2.setQuery("test");
    
    // Then
    assertEquals(response1, response2);
  }

  @Test
  void shouldTestTavilyResponseEqualityWithDifferentResults() {
    // Given
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    response1.setResults(Arrays.asList(new TavilyResponse.TavilySearchResult()));
    
    TavilyResponse response2 = new TavilyResponse();
    response2.setQuery("test");
    response2.setResults(Arrays.asList());
    
    // Then
    assertNotEquals(response1, response2);
  }

  @Test
  void shouldTestTavilyResponseEqualityWithNullResults() {
    // Given
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    
    TavilyResponse response2 = new TavilyResponse();
    response2.setQuery("test");
    
    // Then
    assertEquals(response1, response2);
  }

  @Test
  void shouldTestTavilyResponseEqualityWithDifferentFollowUpQuestions() {
    // Given
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    response1.setFollowUpQuestions(Arrays.asList("question1"));
    
    TavilyResponse response2 = new TavilyResponse();
    response2.setQuery("test");
    response2.setFollowUpQuestions(Arrays.asList("question2"));
    
    // Then
    assertNotEquals(response1, response2);
  }

  @Test
  void shouldTestTavilyResponseEqualityWithNullFollowUpQuestions() {
    // Given
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    
    TavilyResponse response2 = new TavilyResponse();
    response2.setQuery("test");
    
    // Then
    assertEquals(response1, response2);
  }

  @Test
  void shouldTestTavilyResponseEqualityWithDifferentImages() {
    // Given
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    response1.setImages(Arrays.asList("image1.jpg"));
    
    TavilyResponse response2 = new TavilyResponse();
    response2.setQuery("test");
    response2.setImages(Arrays.asList("image2.jpg"));
    
    // Then
    assertNotEquals(response1, response2);
  }

  @Test
  void shouldTestTavilyResponseEqualityWithNullImages() {
    // Given
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    
    TavilyResponse response2 = new TavilyResponse();
    response2.setQuery("test");
    
    // Then
    assertEquals(response1, response2);
  }

  @Test
  void shouldTestTavilySearchResultEqualityWithNullTitle() {
    // Given
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setUrl("https://test.com");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setUrl("https://test.com");
    
    // Then
    assertEquals(result1, result2);
  }

  @Test
  void shouldTestTavilySearchResultEqualityWithDifferentUrl() {
    // Given
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("title");
    result1.setUrl("https://test1.com");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("title");
    result2.setUrl("https://test2.com");
    
    // Then
    assertNotEquals(result1, result2);
  }

  @Test
  void shouldTestTavilySearchResultEqualityWithNullUrl() {
    // Given
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("title");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("title");
    
    // Then
    assertEquals(result1, result2);
  }

  @Test
  void shouldTestTavilySearchResultEqualityWithDifferentContent() {
    // Given
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("title");
    result1.setContent("content1");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("title");
    result2.setContent("content2");
    
    // Then
    assertNotEquals(result1, result2);
  }

  @Test
  void shouldTestTavilySearchResultEqualityWithNullContent() {
    // Given
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("title");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("title");
    
    // Then
    assertEquals(result1, result2);
  }

  @Test
  void shouldTestTavilySearchResultEqualityWithDifferentRawContent() {
    // Given
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("title");
    result1.setRawContent("raw1");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("title");
    result2.setRawContent("raw2");
    
    // Then
    assertNotEquals(result1, result2);
  }

  @Test
  void shouldTestTavilySearchResultEqualityWithNullRawContent() {
    // Given
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("title");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("title");
    
    // Then
    assertEquals(result1, result2);
  }

  @Test
  void shouldTestTavilySearchResultEqualityWithDifferentPublishedDate() {
    // Given
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("title");
    result1.setPublishedDate("2023-01-01");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("title");
    result2.setPublishedDate("2023-01-02");
    
    // Then
    assertNotEquals(result1, result2);
  }

  @Test
  void shouldTestTavilySearchResultEqualityWithNullPublishedDate() {
    // Given
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("title");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("title");
    
    // Then
    assertEquals(result1, result2);
  }

  @Test
  void shouldTestTavilySearchResultEqualityWithDifferentScore() {
    // Given
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("title");
    result1.setScore(0.8);
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("title");
    result2.setScore(0.9);
    
    // Then
    assertNotEquals(result1, result2);
  }

  @Test
  void shouldTestTavilySearchResultEqualityWithNullScore() {
    // Given
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("title");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("title");
    
    // Then
    assertEquals(result1, result2);
  }

  @Test
  void shouldTestHashCodeConsistency() {
    // Given
    TavilyResponse response = new TavilyResponse();
    response.setQuery("test");
    response.setAnswer("answer");
    
    int hashCode1 = response.hashCode();
    int hashCode2 = response.hashCode();
    
    // Then
    assertEquals(hashCode1, hashCode2);
  }

  @Test
  void shouldTestTavilySearchResultHashCodeConsistency() {
    // Given
    TavilyResponse.TavilySearchResult result = new TavilyResponse.TavilySearchResult();
    result.setTitle("title");
    result.setScore(0.8);
    
    int hashCode1 = result.hashCode();
    int hashCode2 = result.hashCode();
    
    // Then
    assertEquals(hashCode1, hashCode2);
  }

  @Test
  void shouldTestToStringWithNullFields() {
    // Given
    TavilyResponse response = new TavilyResponse();
    TavilyResponse.TavilySearchResult result = new TavilyResponse.TavilySearchResult();
    
    // When
    String responseString = response.toString();
    String resultString = result.toString();
    
    // Then
    assertNotNull(responseString);
    assertNotNull(resultString);
    assertTrue(responseString.contains("TavilyResponse"));
    assertTrue(resultString.contains("TavilySearchResult"));
  }

  @Test
  void shouldSetAndGetAllFieldsForTavilyResponse() {
    // Given
    TavilyResponse response = new TavilyResponse();
    
    // When
    response.setQuery("query");
    response.setAnswer("answer");
    response.setFollowUpQuestions(Arrays.asList("question1", "question2"));
    response.setImages(Arrays.asList("image1.jpg", "image2.jpg"));
    
    // Then
    assertEquals("query", response.getQuery());
    assertEquals("answer", response.getAnswer());
    assertEquals(2, response.getFollowUpQuestions().size());
    assertEquals("question1", response.getFollowUpQuestions().get(0));
    assertEquals("question2", response.getFollowUpQuestions().get(1));
    assertEquals(2, response.getImages().size());
    assertEquals("image1.jpg", response.getImages().get(0));
    assertEquals("image2.jpg", response.getImages().get(1));
  }

  @Test
  void shouldSetAndGetAllFieldsForTavilySearchResult() {
    // Given
    TavilyResponse.TavilySearchResult result = new TavilyResponse.TavilySearchResult();
    
    // When
    result.setTitle("title");
    result.setUrl("https://test.com");
    result.setContent("content");
    result.setRawContent("raw content");
    result.setPublishedDate("2023-01-01");
    result.setScore(0.95);
    
    // Then
    assertEquals("title", result.getTitle());
    assertEquals("https://test.com", result.getUrl());
    assertEquals("content", result.getContent());
    assertEquals("raw content", result.getRawContent());
    assertEquals("2023-01-01", result.getPublishedDate());
    assertEquals(0.95, result.getScore());
  }

  @Test
  void shouldTestTavilyResponseEqualsWithSameInstance() {
    TavilyResponse response = new TavilyResponse();
    response.setQuery("test");
    assertEquals(response, response); // Same instance reference
  }

  @Test
  void shouldTestTavilyResponseEqualsWithNull() {
    TavilyResponse response = new TavilyResponse();
    assertNotEquals(response, null); // Null comparison
  }

  @Test
  void shouldTestTavilyResponseEqualsWithDifferentType() {
    TavilyResponse response = new TavilyResponse();
    assertNotEquals(response, "not a TavilyResponse"); // Different type
  }

  @Test
  void shouldTestTavilyResponseEqualsWithCanEqualFalse() {
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    
    // Anonymous subclass that overrides canEqual to return false
    TavilyResponse response2 = new TavilyResponse() {
      @Override
      public boolean canEqual(Object other) {
        return false;
      }
    };
    response2.setQuery("test");
    
    assertNotEquals(response1, response2); // canEqual returns false
  }

  @Test
  void shouldTestTavilySearchResultEqualsWithSameInstance() {
    TavilyResponse.TavilySearchResult result = new TavilyResponse.TavilySearchResult();
    result.setTitle("test");
    assertEquals(result, result); // Same instance reference
  }

  @Test
  void shouldTestTavilySearchResultEqualsWithNull() {
    TavilyResponse.TavilySearchResult result = new TavilyResponse.TavilySearchResult();
    assertNotEquals(result, null); // Null comparison
  }

  @Test
  void shouldTestTavilySearchResultEqualsWithDifferentType() {
    TavilyResponse.TavilySearchResult result = new TavilyResponse.TavilySearchResult();
    assertNotEquals(result, "not a TavilySearchResult"); // Different type
  }

  @Test
  void shouldTestTavilySearchResultEqualsWithCanEqualFalse() {
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("test");
    
    // Anonymous subclass that overrides canEqual to return false
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult() {
      @Override
      public boolean canEqual(Object other) {
        return false;
      }
    };
    result2.setTitle("test");
    
    assertNotEquals(result1, result2); // canEqual returns false
  }

  @Test
  void shouldTestTavilyResponseEqualsWithOneNullQueryField() {
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    response1.setAnswer("answer");
    
    TavilyResponse response2 = new TavilyResponse();
    response2.setQuery(null);
    response2.setAnswer("answer");
    
    assertNotEquals(response1, response2); // One has null query, other doesn't
  }

  @Test
  void shouldTestTavilyResponseEqualsWithOneNullAnswerField() {
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    response1.setAnswer("answer");
    
    TavilyResponse response2 = new TavilyResponse();
    response2.setQuery("test");
    response2.setAnswer(null);
    
    assertNotEquals(response1, response2); // One has null answer, other doesn't
  }

  @Test
  void shouldTestTavilyResponseEqualsWithOneNullFollowUpQuestionsField() {
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    response1.setFollowUpQuestions(Arrays.asList("question"));
    
    TavilyResponse response2 = new TavilyResponse();
    response2.setQuery("test");
    response2.setFollowUpQuestions(null);
    
    assertNotEquals(response1, response2); // One has null followUpQuestions, other doesn't
  }

  @Test
  void shouldTestTavilyResponseEqualsWithOneNullImagesField() {
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    response1.setImages(Arrays.asList("image.jpg"));
    
    TavilyResponse response2 = new TavilyResponse();
    response2.setQuery("test");
    response2.setImages(null);
    
    assertNotEquals(response1, response2); // One has null images, other doesn't
  }

  @Test
  void shouldTestTavilyResponseEqualsWithOneNullResultsField() {
    TavilyResponse response1 = new TavilyResponse();
    response1.setQuery("test");
    response1.setResults(Arrays.asList(new TavilyResponse.TavilySearchResult()));
    
    TavilyResponse response2 = new TavilyResponse();
    response2.setQuery("test");
    response2.setResults(null);
    
    assertNotEquals(response1, response2); // One has null results, other doesn't
  }

  @Test
  void shouldTestTavilySearchResultEqualsWithOneNullTitleField() {
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("test");
    result1.setUrl("url");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle(null);
    result2.setUrl("url");
    
    assertNotEquals(result1, result2); // One has null title, other doesn't
  }

  @Test
  void shouldTestTavilySearchResultEqualsWithOneNullUrlField() {
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("test");
    result1.setUrl("url");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("test");
    result2.setUrl(null);
    
    assertNotEquals(result1, result2); // One has null url, other doesn't
  }

  @Test
  void shouldTestTavilySearchResultEqualsWithOneNullContentField() {
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("test");
    result1.setContent("content");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("test");
    result2.setContent(null);
    
    assertNotEquals(result1, result2); // One has null content, other doesn't
  }

  @Test
  void shouldTestTavilySearchResultEqualsWithOneNullRawContentField() {
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("test");
    result1.setRawContent("rawContent");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("test");
    result2.setRawContent(null);
    
    assertNotEquals(result1, result2); // One has null rawContent, other doesn't
  }

  @Test
  void shouldTestTavilySearchResultEqualsWithOneNullPublishedDateField() {
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("test");
    result1.setPublishedDate("2023-01-01");
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("test");
    result2.setPublishedDate(null);
    
    assertNotEquals(result1, result2); // One has null publishedDate, other doesn't
  }

  @Test
  void shouldTestTavilySearchResultEqualsWithOneNullScoreField() {
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    result1.setTitle("test");
    result1.setScore(0.5);
    
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    result2.setTitle("test");
    result2.setScore(null);
    
    assertNotEquals(result1, result2); // One has null score, other doesn't
  }

  @Test
  void shouldTestTavilyResponseHashCodeWithAllNullFields() {
    TavilyResponse response1 = new TavilyResponse();
    TavilyResponse response2 = new TavilyResponse();
    
    assertEquals(response1.hashCode(), response2.hashCode()); // Both have all null fields
  }

  @Test
  void shouldTestTavilySearchResultHashCodeWithAllNullFields() {
    TavilyResponse.TavilySearchResult result1 = new TavilyResponse.TavilySearchResult();
    TavilyResponse.TavilySearchResult result2 = new TavilyResponse.TavilySearchResult();
    
    assertEquals(result1.hashCode(), result2.hashCode()); // Both have all null fields
  }
}