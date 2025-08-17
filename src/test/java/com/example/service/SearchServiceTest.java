package com.example.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.service.dto.SearchResult;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(classes = com.example.springai.SpringaiApplication.class)
@TestPropertySource(locations = "classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SearchServiceTest {

  @Autowired private SearchService searchService;

  @Value("${search.tavily.api-key:}")
  private String tavilyApiKey;

  @Value("${search.enabled:true}")
  private boolean searchEnabled;

  @BeforeEach
  void setUp() {
    // 确保测试开始时使用正确的配置
    ReflectionTestUtils.setField(searchService, "tavilyApiKey", tavilyApiKey);
    ReflectionTestUtils.setField(searchService, "searchEnabled", searchEnabled);
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
    ReflectionTestUtils.setField(searchService, "searchEnabled", false);

    // When
    List<SearchResult> results = searchService.searchMetaso("测试查询");

    // Then
    assertNotNull(results);
    assertTrue(results.isEmpty(), "搜索禁用时应返回空结果");
  }

  @Test
  void testSearchMetaso_EmptyApiKey() {
    // Given - 空API密钥
    ReflectionTestUtils.setField(searchService, "tavilyApiKey", "");

    // When
    List<SearchResult> results = searchService.searchMetaso("测试查询");

    // Then
    assertNotNull(results);
    assertTrue(results.isEmpty(), "API密钥为空时应返回空结果");
  }

  @Test
  void testSearchMetaso_NullApiKey() {
    // Given - null API密钥
    ReflectionTestUtils.setField(searchService, "tavilyApiKey", null);

    // When
    List<SearchResult> results = searchService.searchMetaso("测试查询");

    // Then
    assertNotNull(results);
    assertTrue(results.isEmpty(), "API密钥为null时应返回空结果");
  }

  @Test
  void testSearchMetaso_ValidQuery() {
    // Given - 有效查询（注意：由于没有有效的API密钥，实际会返回空结果）
    // When
    List<SearchResult> results = searchService.searchMetaso("测试查询");

    // Then
    assertNotNull(results);
    // 由于测试环境中可能没有配置有效的API密钥，结果可能为空，这是正常的
    // 我们只需要验证不会抛出异常并且返回值不为null
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
    singleResult.add(SearchResult.create("测试标题", "测试内容", "http://test.com", 0.9));

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
    results.add(SearchResult.create("标题1", "内容1", "http://test1.com", 0.9));
    results.add(SearchResult.create("标题2", "内容2", "http://test2.com", 0.8));
    results.add(SearchResult.create("标题3", "内容3", "http://test3.com", 0.7));

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
    String specialQuery = "特殊字符!@#$%^&*()测试";

    // When
    List<SearchResult> results = searchService.searchMetaso(specialQuery);

    // Then
    assertNotNull(results);
    // 不应该抛出异常
  }

  @Test
  void testSearchMetaso_LongQuery() {
    // Given
    StringBuilder longQuery = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longQuery.append("长查询内容");
    }

    // When
    List<SearchResult> results = searchService.searchMetaso(longQuery.toString());

    // Then
    assertNotNull(results);
    // 不应该抛出异常
  }

  @Test
  void testSearchMetaso_UnicodeCharacters() {
    // Given
    String unicodeQuery = "测试🔍搜索🌟功能";

    // When
    List<SearchResult> results = searchService.searchMetaso(unicodeQuery);

    // Then
    assertNotNull(results);
    // 不应该抛出异常
  }

  private List<SearchResult> createTestSearchResults() {
    List<SearchResult> results = new ArrayList<>();

    // AI摘要结果
    results.add(SearchResult.create("AI 摘要", "这是一个AI生成的摘要内容", "AI Generated Summary", null));

    // 普通搜索结果
    results.add(SearchResult.create("测试标题1", "测试内容1", "http://test1.com", 0.9));
    results.add(SearchResult.create("测试标题2", "测试内容2", "http://test2.com", 0.8));

    return results;
  }
}
