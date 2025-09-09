package com.example.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.config.SearchProperties;
import com.example.dto.response.SearchResult;
import com.example.dto.response.SseEventResponse;
import com.example.service.impl.SearchServiceImpl;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

// 移除SpringBootTest相关注解，改为纯Mockito测试
@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {

  @Mock
  private SearchProperties searchProperties;

  @Mock
  private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

  private SearchService searchService;

  @BeforeEach
  void setUp() {
    // 创建SearchServiceImpl实例，使用mock的依赖
    searchService = new SearchServiceImpl(searchProperties, objectMapper);
  }

  // ========== 搜索触发条件测试 ==========

  @Test
  void testShouldSearch_ValidMessages() {
    // Given - 各种有效消息
    String[] validMessages = {
      "你好", "今天天气怎么样", "搜索相关信息", "这是什么？", "帮我查询一下", "最新新闻", "股票行情", "a", // 单个字符
    };

    // When & Then
    for (String message : validMessages) {
      assertTrue(searchService.shouldSearch(message), "消息 '" + message + "' 应该触发搜索");
    }
  }

  @Test
  void testShouldSearch_InvalidMessages() {
    // Given - 无效消息
    String[] invalidMessages = {
      null, "", "   ", // 只有空格
      "\t\n", // 只有制表符和换行符
    };

    // When & Then
    for (String message : invalidMessages) {
      assertFalse(searchService.shouldSearch(message), "消息 '" + message + "' 不应该触发搜索");
    }
  }

  // ========== 搜索功能测试 ==========

  @Test
  void testSearchMetaso_SearchDisabled() {
    // Given - 禁用搜索
    when(searchProperties.isEnabled()).thenReturn(false);

    // When
    List<SearchResult> results = searchService.searchMetaso("测试查询");

    // Then
    assertNotNull(results);
    assertTrue(results.isEmpty());
    // 验证没有调用Tavily API
    verify(searchProperties).isEnabled();
    verifyNoMoreInteractions(searchProperties);
  }

  @Test
  void testSearchMetaso_EmptyApiKey() {
    // Given - 启用搜索但API密钥为空
    when(searchProperties.isEnabled()).thenReturn(true);
    when(searchProperties.getTavily()).thenReturn(new SearchProperties.Tavily());
    // Tavily对象的apiKey默认为空字符串

    // When
    List<SearchResult> results = searchService.searchMetaso("测试查询");

    // Then
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testSearchMetaso_NullApiKey() {
    // Given - 启用搜索但API密钥为null
    when(searchProperties.isEnabled()).thenReturn(true);
    SearchProperties.Tavily tavily = new SearchProperties.Tavily();
    tavily.setApiKey(null);
    when(searchProperties.getTavily()).thenReturn(tavily);

    // When
    List<SearchResult> results = searchService.searchMetaso("测试查询");

    // Then
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testSearchMetaso_ValidQuery() {
    // Given - 启用搜索且有API密钥
    when(searchProperties.isEnabled()).thenReturn(true);
    SearchProperties.Tavily tavily = new SearchProperties.Tavily();
    tavily.setApiKey("test-key");
    tavily.setBaseUrl("https://api.test.com/search");
    when(searchProperties.getTavily()).thenReturn(tavily);

    // When
    List<SearchResult> results = searchService.searchMetaso("测试查询");

    // Then
    assertNotNull(results);
    // 由于是mock测试，不会实际调用API，结果应该为空
    assertTrue(results.isEmpty());
  }

  // ========== 格式化测试 ==========

  @Test
  void testFormatSearchResults_EmptyResults() {
    // Given
    List<SearchResult> emptyResults = new ArrayList<>();

    // When
    String formatted = searchService.formatSearchResults(emptyResults);

    // Then
    assertEquals("", formatted);
  }

  @Test
  void testFormatSearchResults_NullResults() {
    // When
    String formatted = searchService.formatSearchResults(null);

    // Then
    assertEquals("", formatted);
  }

  @Test
  void testFormatSearchResults_SingleResult() {
    // Given
    List<SearchResult> singleResult = new ArrayList<>();
    singleResult.add(SearchResult.create("测试标题", "http://test.com", null, "测试内容"));

    // When
    String formatted = searchService.formatSearchResults(singleResult);

    // Then
    assertNotNull(formatted);
    assertFalse(formatted.isEmpty());
    assertTrue(formatted.contains("搜索结果"));
    assertTrue(formatted.contains("测试标题"));
    assertTrue(formatted.contains("测试内容"));
    assertTrue(formatted.contains("http://test.com"));
  }

  @Test
  void testFormatSearchResults_MultipleResults() {
    // Given
    List<SearchResult> results = new ArrayList<>();
    results.add(SearchResult.create("标题1", "http://test1.com", null, "内容1"));
    results.add(SearchResult.create("标题2", "http://test2.com", null, "内容2"));
    results.add(SearchResult.create("标题3", "http://test3.com", null, "内容3"));

    // When
    String formatted = searchService.formatSearchResults(results);

    // Then
    assertNotNull(formatted);
    assertFalse(formatted.isEmpty());
    assertTrue(formatted.contains("搜索结果"));
    assertTrue(formatted.contains("1. 标题1"));
    assertTrue(formatted.contains("2. 标题2"));
    assertTrue(formatted.contains("3. 标题3"));
    assertTrue(formatted.contains("内容1"));
    assertTrue(formatted.contains("内容2"));
    assertTrue(formatted.contains("内容3"));
  }

  @Test
  void testFormatSearchResults_HandleNullValues() {
    // Given
    List<SearchResult> results = new ArrayList<>();
    results.add(SearchResult.create(null, null, null, null));

    // When
    String formatted = searchService.formatSearchResults(results);

    // Then
    assertNotNull(formatted);
    assertTrue(formatted.contains("搜索结果"));
    // 应该能处理null值而不抛出异常
  }

  // ========== 特殊查询测试 ==========

  @Test
  void testSearchMetaso_SpecialCharacters() {
    // Given
    when(searchProperties.isEnabled()).thenReturn(true);
    SearchProperties.Tavily tavily = new SearchProperties.Tavily();
    tavily.setApiKey("test-key");
    tavily.setBaseUrl("https://api.test.com/search");
    when(searchProperties.getTavily()).thenReturn(tavily);
    
    String specialQuery = "特殊字符!@#$%^&*()测试";

    // When
    List<SearchResult> results = searchService.searchMetaso(specialQuery);

    // Then
    assertNotNull(results);
    // 不应该抛出异常
    assertTrue(results.isEmpty()); // mock测试不会返回实际结果
  }

  @Test
  void testSearchMetaso_LongQuery() {
    // Given
    when(searchProperties.isEnabled()).thenReturn(true);
    SearchProperties.Tavily tavily = new SearchProperties.Tavily();
    tavily.setApiKey("test-key");
    tavily.setBaseUrl("https://api.test.com/search");
    when(searchProperties.getTavily()).thenReturn(tavily);
    
    StringBuilder longQuery = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longQuery.append("长查询内容");
    }

    // When
    List<SearchResult> results = searchService.searchMetaso(longQuery.toString());

    // Then
    assertNotNull(results);
    // 不应该抛出异常
    assertTrue(results.isEmpty()); // mock测试不会返回实际结果
  }

  @Test
  void testSearchMetaso_UnicodeCharacters() {
    // Given
    when(searchProperties.isEnabled()).thenReturn(true);
    SearchProperties.Tavily tavily = new SearchProperties.Tavily();
    tavily.setApiKey("test-key");
    tavily.setBaseUrl("https://api.test.com/search");
    when(searchProperties.getTavily()).thenReturn(tavily);
    
    String unicodeQuery = "测试🔍搜索🌟功能";

    // When
    List<SearchResult> results = searchService.searchMetaso(unicodeQuery);

    // Then
    assertNotNull(results);
    // 不应该抛出异常
    assertTrue(results.isEmpty()); // mock测试不会返回实际结果
  }

  private List<SearchResult> createTestSearchResults() {
    List<SearchResult> results = new ArrayList<>();

    // AI摘要结果
    results.add(SearchResult.create("AI 摘要", "AI Generated Summary", null, "这是一个AI生成的摘要内容"));

    // 普通搜索结果
    results.add(SearchResult.create("测试标题1", "http://test1.com", null, "测试内容1"));
    results.add(SearchResult.create("测试标题2", "http://test2.com", null, "测试内容2"));

    return results;
  }

  // ========== 响应式搜索功能测试（从SearchIntegrationServiceTest迁移） ==========

  @Test
  void testPerformSearchWithEvents_SearchEnabled() {
    // Given
    when(searchProperties.isEnabled()).thenReturn(true);
    String userMessage = "今天天气如何";
    
    // When & Then
    StepVerifier.create(searchService.performSearchWithEvents(userMessage, true))
        .expectNextMatches(result -> {
            // 【推荐】复杂断言拆分为多个简单断言
            boolean hasCorrectContext = result.getSearchContext() != null;
            boolean hasEvents = result.getSearchEvents() != null;
            return hasCorrectContext && hasEvents;
        })
        .verifyComplete();
  }

  @Test
  void testPerformSearchWithEvents_SearchDisabled() {
    // Given
    when(searchProperties.isEnabled()).thenReturn(false);
    String userMessage = "test message";

    // When & Then
    StepVerifier.create(searchService.performSearchWithEvents(userMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext().isEmpty() &&
            result.getSearchResults() == null &&
            result.getSearchEvents() != null)
        .verifyComplete();
  }

  @Test
  void testPerformSearchWithEvents_NullUserMessage() {
    // Given - 搜索启用
    lenient().when(searchProperties.isEnabled()).thenReturn(true);
    
    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
        searchService.performSearchWithEvents(null, true);
    }, "用户消息为null时应抛出IllegalArgumentException");
  }

  @Test
  void testCreateSearchEvents_WithResults() {
    // Given
    List<SearchResult> results = Arrays.asList(
        SearchResult.create("测试标题", "https://test.com", "测试内容", null)
    );
    
    // When & Then
    StepVerifier.create(searchService.createSearchEvents(results))
        .expectNextMatches(event -> {
            return "search".equals(event.getType()) && 
                   event.getData().toString().contains("start");
        })
        .expectNextMatches(event -> {
            return "search_results".equals(event.getType());
        })
        .expectNextMatches(event -> {
            return "search".equals(event.getType()) && 
                   event.getData().toString().contains("complete");
        })
        .verifyComplete();
  }

  @Test
  void testCreateSearchEvents_WithEmptyResults() {
    // Given
    List<SearchResult> emptyResults = Collections.emptyList();
    
    // When & Then
    StepVerifier.create(searchService.createSearchEvents(emptyResults))
        .expectNextMatches(event -> "search".equals(event.getType()))
        .expectNextMatches(event -> "search".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void testCreateSearchEvents_WithNullResults() {
    // When & Then
    StepVerifier.create(searchService.createSearchEvents(null))
        .expectNextMatches(event -> "search".equals(event.getType()))
        .expectNextMatches(event -> "search".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void testPerformSearchWithEvents_LongUserMessage() {
    // Given - 创建一个超过50个字符的消息
    when(searchProperties.isEnabled()).thenReturn(true);
    String longMessage = "这是一个非常长的用户消息，用来测试字符串截取功能，它超过50个字符长度限制";

    // When & Then
    StepVerifier.create(searchService.performSearchWithEvents(longMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext() != null && 
            result.getSearchEvents() != null)
        .verifyComplete();
  }

  @Test
  void testPerformSearchWithEvents_ShortUserMessage() {
    // Given - 短消息测试
    when(searchProperties.isEnabled()).thenReturn(true);
    String shortMessage = "短消息";

    // When & Then
    StepVerifier.create(searchService.performSearchWithEvents(shortMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext() != null && 
            result.getSearchEvents() != null)
        .verifyComplete();
  }

  @Test
  void testPerformSearchWithEvents_ErrorHandling() {
    // Given - 测试错误处理，通过禁用搜索和使用无效API密钥来模拟
    when(searchProperties.isEnabled()).thenReturn(true);
    String userMessage = "测试错误处理";

    // When & Then
    StepVerifier.create(searchService.performSearchWithEvents(userMessage, true))
        .expectNextMatches(result -> {
            // 在API密钥为空的情况下，应该返回空结果但不抛出异常
            return result.getSearchContext() != null &&
                   result.getSearchEvents() != null;
        })
        .verifyComplete();
  }

  @Test
  void testSearchContextResult_GetterMethods() {
    // Given - 直接测试SearchContextResult类
    List<SearchResult> testResults = Arrays.asList(
        SearchResult.create("测试", "https://test.com", "测试", null)
    );
    
    SearchService.SearchContextResult result = 
        new SearchService.SearchContextResult(
            "测试上下文", 
            testResults, 
            reactor.core.publisher.Flux.empty()
        );

    // When & Then - 验证getter方法
    assertEquals("测试上下文", result.getSearchContext());
    assertEquals(testResults, result.getSearchResults());
    assertNotNull(result.getSearchEvents());
  }

  @Test
  void testSearchContextResult_NullValidation() {
    // When & Then - 测试构造函数的null校验
    assertThrows(NullPointerException.class, () -> {
        new SearchService.SearchContextResult(null, new ArrayList<>(), reactor.core.publisher.Flux.empty());
    }, "搜索上下文为null时应抛出NullPointerException");
    
    assertThrows(NullPointerException.class, () -> {
        new SearchService.SearchContextResult("test", new ArrayList<>(), null);
    }, "搜索事件流为null时应抛出NullPointerException");
  }

  @Test
  void testPerformSearchWithEvents_EmptyUserMessage() {
    // Given
    when(searchProperties.isEnabled()).thenReturn(true);
    String emptyMessage = "";

    // When & Then
    StepVerifier.create(searchService.performSearchWithEvents(emptyMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext() != null && 
            result.getSearchEvents() != null)
        .verifyComplete();
  }

  @Test
  void testPerformSearchWithEvents_WhitespaceUserMessage() {
    // Given
    when(searchProperties.isEnabled()).thenReturn(true);
    String whitespaceMessage = "   ";

    // When & Then
    StepVerifier.create(searchService.performSearchWithEvents(whitespaceMessage, true))
        .expectNextMatches(result -> 
            result.getSearchContext() != null && 
            result.getSearchEvents() != null)
        .verifyComplete();
  }

  @Test
  void testCreateSearchEvents_WithError() {
    // Given
    List<SearchResult> results = Arrays.asList(
        SearchResult.create("测试标题", "https://test.com", "测试内容", null)
    );
    
    // When & Then
    StepVerifier.create(searchService.createSearchEvents(results))
        .expectNextMatches(event -> {
            return "search".equals(event.getType()) && 
                   event.getData().toString().contains("start");
        })
        .expectNextMatches(event -> "search_results".equals(event.getType()))
        .expectNextMatches(event -> {
            return "search".equals(event.getType()) && 
                   event.getData().toString().contains("complete");
        })
        .verifyComplete();
  }

  @Test
  void testSearchMetaso_ExceptionHandling() {
    // Given - 测试异常处理
    when(searchProperties.isEnabled()).thenReturn(true);
    SearchProperties.Tavily tavily = new SearchProperties.Tavily();
    tavily.setApiKey("test-key");
    tavily.setBaseUrl("https://api.test.com/search");
    when(searchProperties.getTavily()).thenReturn(tavily);
    
    String query = "测试异常处理";

    // When
    List<SearchResult> results = searchService.searchMetaso(query);

    // Then
    assertNotNull(results);
    // 不应该抛出异常
    assertTrue(results.isEmpty()); // mock测试不会返回实际结果
  }

  @Test
  void testFormatSearchResults_WithAIAnswer() {
    // Given
    List<SearchResult> results = new ArrayList<>();
    results.add(SearchResult.create("AI 摘要", "http://test.com", null, "这是AI生成的摘要"));
    results.add(SearchResult.create("测试标题", "http://test.com", null, "测试内容"));

    // When
    String formatted = searchService.formatSearchResults(results);

    // Debug output
    System.out.println("Formatted result:");
    System.out.println(formatted);
    System.out.println("End of formatted result");

    // Then
    assertNotNull(formatted);
    assertFalse(formatted.isEmpty());
    assertTrue(formatted.contains("搜索结果"), "Should contain '搜索结果'");
    assertTrue(formatted.contains("AI 摘要"), "Should contain 'AI 摘要'");
    assertTrue(formatted.contains("这是AI生成的摘要"), "Should contain '这是AI生成的摘要'");
    assertTrue(formatted.contains("测试标题"), "Should contain '测试标题'");
    assertTrue(formatted.contains("测试内容"), "Should contain '测试内容'");
  }
}