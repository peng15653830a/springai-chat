package com.example.service;

import com.example.service.dto.SearchResult;
import com.example.service.dto.SseEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * SearchIntegrationService测试
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
class SearchIntegrationServiceTest {

  @Mock
  private SearchService searchService;

  private SearchIntegrationService searchIntegrationService;

  @BeforeEach
  void setUp() {
    searchIntegrationService = new SearchIntegrationService();
    ReflectionTestUtils.setField(searchIntegrationService, "searchService", searchService);
  }

  @Test
  void shouldPerformSearchWhenEnabled() {
    // Given
    String userMessage = "今天天气如何";
    List<SearchResult> mockResults = Arrays.asList(
        new SearchResult("天气预报", "今天晴天", "https://weather.com", 0.9),
        new SearchResult("气象信息", "气温25度", "https://weather2.com", 0.8)
    );
    
    when(searchService.searchMetaso(userMessage)).thenReturn(mockResults);
    when(searchService.formatSearchResults(mockResults)).thenReturn("天气相关信息...");

    // When & Then
    StepVerifier.create(searchIntegrationService.performSearchIfEnabled(userMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext().contains("天气相关信息") &&
            result.getSearchResults() != null &&
            result.getSearchResults().size() == 2)
        .verifyComplete();
        
    verify(searchService).searchMetaso(userMessage);
    verify(searchService).formatSearchResults(mockResults);
  }

  @Test
  void shouldSkipSearchWhenDisabled() {
    // Given
    String userMessage = "test message";

    // When & Then
    StepVerifier.create(searchIntegrationService.performSearchIfEnabled(userMessage, false))
        .expectNextMatches(result -> 
            result.getSearchContext().isEmpty() &&
            result.getSearchResults() == null)
        .verifyComplete();
        
    verify(searchService, never()).searchMetaso(anyString());
  }

  @Test
  void shouldHandleSearchError() {
    // Given
    String userMessage = "test query";
    when(searchService.searchMetaso(userMessage)).thenThrow(new RuntimeException("搜索服务不可用"));

    // When & Then
    StepVerifier.create(searchIntegrationService.performSearchIfEnabled(userMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext().isEmpty() &&
            result.getSearchResults() == null)
        .verifyComplete();
  }

  @Test
  void shouldHandleEmptySearchResults() {
    // Given
    String userMessage = "无结果查询";
    when(searchService.searchMetaso(userMessage)).thenReturn(Collections.emptyList());
    when(searchService.formatSearchResults(Collections.emptyList())).thenReturn("");

    // When & Then
    StepVerifier.create(searchIntegrationService.performSearchIfEnabled(userMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext().isEmpty() &&
            result.getSearchResults() != null &&
            result.getSearchResults().isEmpty())
        .verifyComplete();
  }

  @Test
  void shouldCreateSearchEvents() {
    // Given
    String userMessage = "搜索测试";
    List<SearchResult> mockResults = Arrays.asList(
        new SearchResult("标题1", "内容1", "https://test1.com", 0.9)
    );
    
    when(searchService.searchMetaso(userMessage)).thenReturn(mockResults);
    when(searchService.formatSearchResults(mockResults)).thenReturn("搜索结果格式化");

    // When & Then
    StepVerifier.create(searchIntegrationService.performSearchIfEnabled(userMessage, true))
        .expectNextMatches(result -> {
            // 验证搜索事件流不为空
            return result.getSearchEvents() != null;
        })
        .verifyComplete();
  }
}