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
    
    @Value("${search.metaso.api-key:mk-2A24E872A9A815A08EAFFB34AD42381D}")
    private String metasoApiKey;
    
    @Value("${search.metaso.enabled:true}")
    private boolean searchEnabled;
    
    /**
     * 从配置文件读取搜索关键词，使用默认值
     */
    @Value("${search.keywords.time:最新,今天,现在,当前,实时,近期,目前,这几天,本周,最近}")
    private String timeKeywords;
    
    @Value("${search.keywords.info:新闻,资讯,消息,报道,动态,头条}")
    private String infoKeywords;
    
    @Value("${search.keywords.finance:天气,股价,汇率,股票,基金,投资,行情,价格}")
    private String financeKeywords;
    
    @Value("${search.keywords.query:什么是,如何,怎么,哪里,什么时候,为什么}")
    private String queryKeywords;
    
    @Value("${search.keywords.search:搜索,查询,找,查找,了解,知道}")
    private String searchKeywords;
    
    /**
     * 从配置文件读取测试数据，使用默认值
     */
    @Value("${search.test.no-trigger-messages:你好,谢谢,再见,我很好,没问题,聊天,对话}")
    private String noTriggerMessages;
    
    @Value("${search.test.sample-queries.weather:今天天气}")
    private String weatherQuery;
    
    @Value("${search.test.sample-queries.news:最新新闻}")
    private String newsQuery;
    
    @Value("${search.test.sample-queries.stock:股票行情}")
    private String stockQuery;
    
    @BeforeEach
    void setUp() {
        // 确保测试开始时使用正确的配置
        ReflectionTestUtils.setField(searchService, "metasoApiKey", metasoApiKey);
        ReflectionTestUtils.setField(searchService, "searchEnabled", searchEnabled);
    }

    // ========== 配置测试 ==========
    
    @Test
    void testSearchConfiguration_LoadedFromProperties() {
        // Then - 验证配置是否正确加载
        assertNotNull(metasoApiKey);
        assertEquals("mk-2A24E872A9A815A08EAFFB34AD42381D", metasoApiKey);
        assertTrue(searchEnabled);
        
        // 验证SearchService中的配置值
        String serviceApiKey = (String) ReflectionTestUtils.getField(searchService, "metasoApiKey");
        Boolean serviceEnabled = (Boolean) ReflectionTestUtils.getField(searchService, "searchEnabled");
        
        assertEquals(metasoApiKey, serviceApiKey);
        assertEquals(searchEnabled, serviceEnabled);
    }

    // ========== 搜索触发条件测试 ==========
    
    @Test
    void testShouldSearch_TimeKeywords() {
        // Given - 从配置文件读取时间相关关键词
        String[] timeKeywordArray = timeKeywords.split(",");
        
        // When & Then
        for (String keyword : timeKeywordArray) {
            assertTrue(searchService.shouldSearch("请告诉我" + keyword.trim() + "的信息"),
                      "时间关键词 '" + keyword.trim() + "' 应该触发搜索");
        }
    }
    
    @Test
    void testShouldSearch_InfoKeywords() {
        // Given - 信息类关键词
        String[] infoKeywords = {"新闻", "资讯", "消息", "报道", "动态", "头条"};
        
        // When & Then
        for (String keyword : infoKeywords) {
            assertTrue(searchService.shouldSearch("我想看" + keyword),
                      "信息关键词 '" + keyword + "' 应该触发搜索");
        }
    }
    
    @Test
    void testShouldSearch_FinanceKeywords() {
        // Given - 金融相关关键词
        String[] financeKeywords = {"天气", "股价", "汇率", "股票", "基金", "投资", "行情", "价格"};
        
        // When & Then
        for (String keyword : financeKeywords) {
            assertTrue(searchService.shouldSearch("查看" + keyword + "情况"),
                      "金融关键词 '" + keyword + "' 应该触发搜索");
        }
    }
    
    @Test
    void testShouldSearch_QueryKeywords() {
        // Given - 疑问词汇
        String[] queryKeywords = {"什么是", "如何", "怎么", "哪里", "什么时候", "为什么"};
        
        // When & Then
        for (String keyword : queryKeywords) {
            assertTrue(searchService.shouldSearch(keyword + "人工智能"),
                      "疑问词汇 '" + keyword + "' 应该触发搜索");
        }
    }
    
    @Test
    void testShouldSearch_SearchKeywords() {
        // Given - 搜索指示词
        String[] searchKeywords = {"搜索", "查询", "找", "查找", "了解", "知道"};
        
        // When & Then
        for (String keyword : searchKeywords) {
            assertTrue(searchService.shouldSearch(keyword + "相关信息"),
                      "搜索指示词 '" + keyword + "' 应该触发搜索");
        }
    }
    
    @Test
    void testShouldSearch_QuestionMarks() {
        // When & Then
        assertTrue(searchService.shouldSearch("这是什么？"));
        assertTrue(searchService.shouldSearch("天气如何?"));
        assertTrue(searchService.shouldSearch("股价怎么样？"));
    }
    
    @Test
    void testShouldSearch_NoTrigger() {
        // Given - 不应该触发搜索的消息
        String[] noTriggerMessages = {
            "你好",
            "谢谢",
            "再见",
            "我很好",
            "没问题",
            "聊天",
            "对话"
        };
        
        // When & Then
        for (String message : noTriggerMessages) {
            assertFalse(searchService.shouldSearch(message),
                       "消息 '" + message + "' 不应该触发搜索");
        }
    }
    
    @Test
    void testShouldSearch_EdgeCases() {
        // When & Then
        assertFalse(searchService.shouldSearch(null), "null消息不应该触发搜索");
        assertFalse(searchService.shouldSearch(""), "空消息不应该触发搜索");
        assertFalse(searchService.shouldSearch("   "), "空白消息不应该触发搜索");
    }

    // ========== 搜索功能测试 ==========
    
    @Test
    void testSearchMetaso_WithValidConfig() {
        // When
        List<Map<String, String>> results = searchService.searchMetaso("测试查询");
        
        // Then
        assertNotNull(results);
        // 由于是测试环境，API调用会失败，但应该有本地降级结果
        assertFalse(results.isEmpty());
    }
    
    @Test
    void testSearchMetaso_WithDisabledSearch() {
        // Given - 临时禁用搜索
        ReflectionTestUtils.setField(searchService, "searchEnabled", false);
        
        // When
        List<Map<String, String>> results = searchService.searchMetaso("测试查询");
        
        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty()); // 应该有本地搜索结果
        
        // 验证返回的是本地搜索结果
        boolean hasLocalResult = results.stream()
            .anyMatch(result -> result.get("title").contains("测试查询") ||
                               result.get("snippet").contains("相关信息"));
        assertTrue(hasLocalResult, "禁用搜索时应该返回本地搜索结果");
        
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
        assertFalse(results.isEmpty()); // 应该有本地降级结果
        
        // 恢复原始配置
        ReflectionTestUtils.setField(searchService, "metasoApiKey", originalApiKey);
    }
    
    @Test
    void testSearchMetaso_WeatherQuery() {
        // When
        List<Map<String, String>> results = searchService.searchMetaso("今天天气");
        
        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        
        // 验证天气相关的本地搜索结果
        boolean hasWeatherInfo = results.stream()
            .anyMatch(result -> result.get("title").contains("天气") || 
                               result.get("snippet").contains("天气"));
        assertTrue(hasWeatherInfo, "天气查询应该返回天气相关信息");
    }
    
    @Test
    void testSearchMetaso_NewsQuery() {
        // When
        List<Map<String, String>> results = searchService.searchMetaso("最新新闻");
        
        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        
        // 验证新闻相关的本地搜索结果
        boolean hasNewsInfo = results.stream()
            .anyMatch(result -> result.get("title").contains("新闻") || 
                               result.get("snippet").contains("新闻"));
        assertTrue(hasNewsInfo, "新闻查询应该返回新闻相关信息");
    }
    
    @Test
    void testSearchMetaso_StockQuery() {
        // When
        List<Map<String, String>> results = searchService.searchMetaso("股票行情");
        
        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        
        // 验证股票相关的本地搜索结果
        boolean hasStockInfo = results.stream()
            .anyMatch(result -> result.get("title").contains("股") || 
                               result.get("snippet").contains("股"));
        assertTrue(hasStockInfo, "股票查询应该返回股票相关信息");
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