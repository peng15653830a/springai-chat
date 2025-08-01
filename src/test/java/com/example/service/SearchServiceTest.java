package com.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.example.springai.SpringaiApplication.class)
@TestPropertySource(locations = "classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SearchServiceTest {

    @Autowired
    private SearchService searchService;
    
    @Value("${search.metaso.api-key:}")
    private String metasoApiKey;
    
    @Value("${search.metaso.enabled:true}")
    private boolean searchEnabled;
    
    @BeforeEach
    void setUp() {
        // 确保测试开始时使用正确的配置
        ReflectionTestUtils.setField(searchService, "metasoApiKey", metasoApiKey);
        ReflectionTestUtils.setField(searchService, "searchEnabled", searchEnabled);
    }

    // ========== 搜索触发条件测试 ==========
    
    @Test
    void testShouldSearch_ValidMessages() {
        // Given - 各种有效消息
        String[] validMessages = {
            "你好",
            "今天天气怎么样",
            "搜索相关信息",
            "这是什么？",
            "帮我查询一下",
            "最新新闻",
            "股票行情",
            "a", // 单个字符
            "123", // 数字
            "hello world", // 英文
            "测试消息" // 中文
        };
        
        // When & Then
        for (String message : validMessages) {
            assertTrue(searchService.shouldSearch(message),
                      "有效消息 '" + message + "' 应该触发搜索");
        }
    }
    
    @Test
    void testShouldSearch_InvalidMessages() {
        // When & Then
        assertFalse(searchService.shouldSearch(null), "null消息不应该触发搜索");
        assertFalse(searchService.shouldSearch(""), "空消息不应该触发搜索");
        assertFalse(searchService.shouldSearch("   "), "空白消息不应该触发搜索");
        assertFalse(searchService.shouldSearch("\t\n"), "只包含空白字符的消息不应该触发搜索");
    }

    // ========== 搜索功能测试 ==========
    
    @Test
    void testSearchMetaso_WithValidConfig() {
        // When
        List<Map<String, String>> results = searchService.searchMetaso("测试查询");
        
        // Then
        assertNotNull(results);
        // 由于是测试环境，API调用会失败，返回空结果
        assertTrue(results.isEmpty());
    }
    
    @Test
    void testSearchMetaso_WithDisabledSearch() {
        // Given - 临时禁用搜索
        ReflectionTestUtils.setField(searchService, "searchEnabled", false);
        
        // When
        List<Map<String, String>> results = searchService.searchMetaso("测试查询");
        
        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty()); // 禁用搜索时应该返回空结果
        
        // 恢复原始配置
        ReflectionTestUtils.setField(searchService, "searchEnabled", searchEnabled);
    }
    
    @Test
    void testSearchMetaso_WithEmptyApiKey() {
        // Given - 临时设置空的API密钥
        String originalApiKey = (String) ReflectionTestUtils.getField(searchService, "metasoApiKey");
        ReflectionTestUtils.setField(searchService, "metasoApiKey", "");
        
        // When
        List<Map<String, String>> results = searchService.searchMetaso("测试查询");
        
        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty()); // 空API密钥时应该返回空结果
        
        // 恢复原始配置
        ReflectionTestUtils.setField(searchService, "metasoApiKey", originalApiKey);
    }
    
    @Test
    void testSearchMetaso_WithNullApiKey() {
        // Given - 临时设置null的API密钥
        String originalApiKey = (String) ReflectionTestUtils.getField(searchService, "metasoApiKey");
        ReflectionTestUtils.setField(searchService, "metasoApiKey", null);
        
        // When
        List<Map<String, String>> results = searchService.searchMetaso("测试查询");
        
        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty()); // null API密钥时应该返回空结果
        
        // 恢复原始配置
        ReflectionTestUtils.setField(searchService, "metasoApiKey", originalApiKey);
    }

    // ========== 搜索结果格式化测试 ==========
    
    @Test
    void testFormatSearchResults_ValidResults() {
        // Given
        List<Map<String, String>> searchResults = createTestSearchResults();
        
        // When
        String formatted = searchService.formatSearchResults(searchResults);
        
        // Then
        assertNotNull(formatted);
        assertTrue(formatted.startsWith("搜索结果：\n"));
        
        // 验证每个结果都包含编号、标题、摘要和链接
        for (int i = 0; i < searchResults.size(); i++) {
            Map<String, String> result = searchResults.get(i);
            assertTrue(formatted.contains((i + 1) + ". " + result.get("title")));
            assertTrue(formatted.contains("   " + result.get("snippet")));
            assertTrue(formatted.contains("   链接: " + result.get("link")));
        }
    }
    
    @Test
    void testFormatSearchResults_EmptyResults() {
        // Given
        List<Map<String, String>> emptyResults = new ArrayList<>();
        
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
        List<Map<String, String>> singleResult = new ArrayList<>();
        Map<String, String> result = new HashMap<>();
        result.put("title", "单个测试结果");
        result.put("snippet", "这是一个单个测试结果的摘要");
        result.put("link", "https://example.com");
        singleResult.add(result);
        
        // When
        String formatted = searchService.formatSearchResults(singleResult);
        
        // Then
        assertNotNull(formatted);
        assertTrue(formatted.contains("1. 单个测试结果"));
        assertTrue(formatted.contains("这是一个单个测试结果的摘要"));
        assertTrue(formatted.contains("https://example.com"));
    }

    @Test
    void testFormatSearchResults_WithNullValues() {
        // Given
        List<Map<String, String>> results = new ArrayList<>();
        Map<String, String> result1 = new HashMap<>();
        result1.put("title", null);
        result1.put("snippet", "测试片段");
        result1.put("link", null);
        results.add(result1);

        Map<String, String> result2 = new HashMap<>();
        result2.put("title", "测试标题");
        result2.put("snippet", null);
        result2.put("link", "http://test.com");
        results.add(result2);

        // When
        String formatted = searchService.formatSearchResults(results);

        // Then
        assertNotNull(formatted);
        assertTrue(formatted.contains("搜索结果："));
        assertTrue(formatted.contains("1. null"));
        assertTrue(formatted.contains("2. 测试标题"));
    }

    @Test
    void testFormatSearchResults_WithEmptyValues() {
        // Given
        List<Map<String, String>> results = new ArrayList<>();
        Map<String, String> result = new HashMap<>();
        result.put("title", "");
        result.put("snippet", "");
        result.put("link", "");
        results.add(result);

        // When
        String formatted = searchService.formatSearchResults(results);

        // Then
        assertNotNull(formatted);
        assertTrue(formatted.contains("搜索结果："));
        assertTrue(formatted.contains("1. "));
    }

    // ========== 辅助方法 ==========
    
    private List<Map<String, String>> createTestSearchResults() {
        List<Map<String, String>> results = new ArrayList<>();
        
        Map<String, String> result1 = new HashMap<>();
        result1.put("title", "测试标题1");
        result1.put("snippet", "这是第一个测试搜索结果的摘要信息");
        result1.put("link", "https://example1.com");
        results.add(result1);
        
        Map<String, String> result2 = new HashMap<>();
        result2.put("title", "测试标题2");
        result2.put("snippet", "这是第二个测试搜索结果的摘要信息");
        result2.put("link", "https://example2.com");
        results.add(result2);
        
        Map<String, String> result3 = new HashMap<>();
        result3.put("title", "测试标题3");
        result3.put("snippet", "这是第三个测试搜索结果的摘要信息");
        result3.put("link", "https://example3.com");
        results.add(result3);
        
        return results;
    }
}