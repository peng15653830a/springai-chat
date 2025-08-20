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

  @Test
  void shouldCreateSearchEventsWithNonEmptyResults() {
    // Given
    String userMessage = "测试查询有结果";
    List<SearchResult> mockResults = Arrays.asList(
        new SearchResult("结果1", "内容1", "https://example1.com", 0.95),
        new SearchResult("结果2", "内容2", "https://example2.com", 0.85)
    );
    
    when(searchService.searchMetaso(userMessage)).thenReturn(mockResults);
    when(searchService.formatSearchResults(mockResults)).thenReturn("格式化的搜索结果");

    // When & Then
    StepVerifier.create(searchIntegrationService.performSearchIfEnabled(userMessage, true))
        .expectNextMatches(result -> {
            // 验证结果和事件流
            return result.getSearchResults() != null && 
                   result.getSearchResults().size() == 2 &&
                   result.getSearchEvents() != null;
        })
        .verifyComplete();
        
    // 验证搜索事件流包含正确的事件
    StepVerifier.create(searchIntegrationService.performSearchIfEnabled(userMessage, true)
        .flatMapMany(result -> result.getSearchEvents()))
        .expectNextMatches(event -> 
            "search".equals(event.getType()) && 
            event.getData().toString().contains("start"))
        .expectNextMatches(event -> 
            "search_results".equals(event.getType()))
        .expectNextMatches(event -> 
            "search".equals(event.getType()) && 
            event.getData().toString().contains("complete"))
        .verifyComplete();
  }

  @Test
  void shouldCreateSearchEventsWithEmptyResults() {
    // Given
    String userMessage = "无结果查询";
    List<SearchResult> emptyResults = Collections.emptyList();
    
    when(searchService.searchMetaso(userMessage)).thenReturn(emptyResults);
    when(searchService.formatSearchResults(emptyResults)).thenReturn("");

    // When & Then - 验证搜索事件流的结构
    StepVerifier.create(searchIntegrationService.performSearchIfEnabled(userMessage, true)
        .flatMapMany(result -> result.getSearchEvents()))
        .expectNextMatches(event -> 
            "search".equals(event.getType()) && 
            event.getData().toString().contains("start"))
        // 空结果不应该产生search_results事件
        .expectNextMatches(event -> 
            "search".equals(event.getType()) && 
            event.getData().toString().contains("complete"))
        .verifyComplete();
  }

  @Test
  void shouldHandleLongUserMessage() {
    // Given - 创建一个超过50个字符的消息
    String longMessage = "这是一个非常长的用户消息，用来测试字符串截取功能，它超过50个字符长度限制";
    List<SearchResult> mockResults = Arrays.asList(
        new SearchResult("测试结果", "测试内容", "https://test.com", 0.8)
    );
    
    when(searchService.searchMetaso(longMessage)).thenReturn(mockResults);
    when(searchService.formatSearchResults(mockResults)).thenReturn("格式化结果");

    // When & Then
    StepVerifier.create(searchIntegrationService.performSearchIfEnabled(longMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext() != null && 
            result.getSearchResults() != null &&
            result.getSearchResults().size() == 1)
        .verifyComplete();
        
    verify(searchService).searchMetaso(longMessage);
    verify(searchService).formatSearchResults(mockResults);
  }

  @Test
  void shouldHandleShortUserMessage() {
    // Given - 短消息测试
    String shortMessage = "短消息";
    List<SearchResult> mockResults = Arrays.asList(
        new SearchResult("简单结果", "简单内容", "https://simple.com", 0.7)
    );
    
    when(searchService.searchMetaso(shortMessage)).thenReturn(mockResults);
    when(searchService.formatSearchResults(mockResults)).thenReturn("简单结果");

    // When & Then
    StepVerifier.create(searchIntegrationService.performSearchIfEnabled(shortMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext().contains("简单结果") && 
            result.getSearchResults() != null &&
            result.getSearchResults().size() == 1)
        .verifyComplete();
        
    verify(searchService).searchMetaso(shortMessage);
    verify(searchService).formatSearchResults(mockResults);
  }

  @Test
  void shouldHandleNullSearchResults() {
    // Given
    String userMessage = "null结果测试";
    when(searchService.searchMetaso(userMessage)).thenReturn(null);
    when(searchService.formatSearchResults(null)).thenReturn("");

    // When & Then
    StepVerifier.create(searchIntegrationService.performSearchIfEnabled(userMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext().isEmpty() &&
            result.getSearchResults() == null)
        .verifyComplete();
  }

  @Test
  void shouldHandleFormatSearchResultsError() {
    // Given
    String userMessage = "格式化错误测试";
    List<SearchResult> mockResults = Arrays.asList(
        new SearchResult("测试", "测试", "https://test.com", 0.5)
    );
    
    when(searchService.searchMetaso(userMessage)).thenReturn(mockResults);
    when(searchService.formatSearchResults(mockResults))
        .thenThrow(new RuntimeException("格式化失败"));

    // When & Then - 应该返回错误结果
    StepVerifier.create(searchIntegrationService.performSearchIfEnabled(userMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext().isEmpty() &&
            result.getSearchResults() == null)
        .verifyComplete();
  }

  @Test
  void shouldTestSearchContextResultMethods() {
    // Given - 直接测试SearchContextResult类
    List<SearchResult> testResults = Arrays.asList(
        new SearchResult("测试", "测试", "https://test.com", 0.9)
    );
    
    SearchIntegrationService.SearchContextResult result = 
        new SearchIntegrationService.SearchContextResult(
            "测试上下文", 
            testResults, 
            reactor.core.publisher.Flux.empty()
        );

    // When & Then - 验证getter方法
    assert result.getSearchContext().equals("测试上下文");
    assert result.getSearchResults() == testResults;
    assert result.getSearchEvents() != null;
  }

  @Test
  void shouldHandleComplexErrorScenario() {
    // Given - 复杂错误场景
    String userMessage = "复杂错误测试";
    
    when(searchService.searchMetaso(userMessage))
        .thenThrow(new IllegalArgumentException("无效参数"));

    // When & Then
    StepVerifier.create(searchIntegrationService.performSearchIfEnabled(userMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext().isEmpty() &&
            result.getSearchResults() == null &&
            result.getSearchEvents() != null) // 错误事件流
        .verifyComplete();
        
    // 验证错误事件流内容
    StepVerifier.create(searchIntegrationService.performSearchIfEnabled(userMessage, true)
        .flatMapMany(result -> result.getSearchEvents()))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("搜索服务暂时不可用"))
        .verifyComplete();
  }
}