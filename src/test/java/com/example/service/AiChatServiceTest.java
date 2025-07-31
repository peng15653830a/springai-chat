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
public class AiChatServiceTest {

    @Autowired
    private AiChatService aiChatService;

    /**
     * 从配置文件读取AI配置，使用默认值
     */
    @Value("${ai.moonshot.api-key:sk-yBoOvdiixdvpCG06mwCSGnxt3lxj3ekZ5t6bav3Ii9cS0Ln4}")
    private String apiKey;
    
    @Value("${ai.moonshot.base-url:https://api.moonshot.cn/v1}")
    private String baseUrl;
    
    @Value("${ai.moonshot.model:kimi-k2-0711-preview}")
    private String model;
    
    @Value("${ai.moonshot.temperature:0.7}")
    private double temperature;
    
    @Value("${ai.moonshot.max-tokens:1000}")
    private int maxTokens;
    
    /**
     * 从配置文件读取流式配置，使用默认值
     */
    @Value("${app.chat.streaming.chunk-size:50}")
    private int chunkSize;
    
    @Value("${app.chat.streaming.delay-ms:100}")
    private int delayMs;
    
    /**
     * 从配置文件读取测试数据，使用默认值
     */
    @Value("${test.ai.sample-messages.user:Hello}")
    private String sampleUserMessage;
    
    @Value("${test.ai.sample-messages.assistant:Hi there!}")
    private String sampleAssistantMessage;
    
    @Value("${test.ai.test-queries.simple:Test message}")
    private String simpleQuery;
    
    @Value("${test.ai.test-queries.long:This is a very long response that should be split into multiple chunks to demonstrate the streaming functionality of the AI chat service implementation.}")
    private String longQuery;
    
    @Value("${test.ai.test-queries.with-newlines:Line 1\nLine 2\n\nLine 3 with more content}")
    private String newlinesQuery;
    
    @Value("${test.ai.expected-responses.error-keywords:抱歉,网络连接错误,AI服务}")
    private String errorKeywords;

    private List<Map<String, String>> testHistory;

    @BeforeEach
    void setUp() {
        // 设置测试用的配置值，从配置文件读取
        ReflectionTestUtils.setField(aiChatService, "apiKey", apiKey);
        ReflectionTestUtils.setField(aiChatService, "baseUrl", baseUrl);
        ReflectionTestUtils.setField(aiChatService, "model", model);
        ReflectionTestUtils.setField(aiChatService, "temperature", temperature);
        ReflectionTestUtils.setField(aiChatService, "maxTokens", maxTokens);

        // 创建测试历史记录，使用配置文件中的数据
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", sampleUserMessage);

        Map<String, String> assistantMessage = new HashMap<>();
        assistantMessage.put("role", "assistant");
        assistantMessage.put("content", sampleAssistantMessage);

        testHistory = Arrays.asList(userMessage, assistantMessage);
    }
    
    // ========== 配置测试 ==========
    
    @Test
    void testAiConfiguration_LoadedFromProperties() {
        // 验证配置是否正确加载
        assertNotNull(apiKey);
        assertEquals("sk-yBoOvdiixdvpCG06mwCSGnxt3lxj3ekZ5t6bav3Ii9cS0Ln4", apiKey);
        assertEquals("https://api.moonshot.cn/v1", baseUrl);
        assertEquals("kimi-k2-0711-preview", model);
        assertEquals(0.7, temperature, 0.001);
        assertEquals(1000, maxTokens);
        assertEquals(50, chunkSize);
        assertEquals(100, delayMs);
        
        // 验证AiChatService中的配置值
        String serviceApiKey = (String) ReflectionTestUtils.getField(aiChatService, "apiKey");
        String serviceBaseUrl = (String) ReflectionTestUtils.getField(aiChatService, "baseUrl");
        String serviceModel = (String) ReflectionTestUtils.getField(aiChatService, "model");
        
        assertEquals(apiKey, serviceApiKey);
        assertEquals(baseUrl, serviceBaseUrl);
        assertEquals(model, serviceModel);
    }

    @Test
    void testChatWithAI_Success() {
        // When
        AiResponse response = aiChatService.chatWithAI(simpleQuery, testHistory);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
        // 由于没有真实的API调用，会返回错误消息，使用配置文件中的关键词
        String[] keywords = errorKeywords.split(",");
        boolean containsErrorKeyword = false;
        for (String keyword : keywords) {
            if (response.getContent().contains(keyword.trim())) {
                containsErrorKeyword = true;
                break;
            }
        }
        assertTrue(containsErrorKeyword, "响应应该包含错误关键词之一: " + errorKeywords);
    }

    @Test
    void testChatWithAI_EmptyMessage() {
        // When
        AiResponse response = aiChatService.chatWithAI("", testHistory);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        String[] keywords = errorKeywords.split(",");
        boolean containsErrorKeyword = false;
        for (String keyword : keywords) {
            if (response.getContent().contains(keyword.trim())) {
                containsErrorKeyword = true;
                break;
            }
        }
        assertTrue(containsErrorKeyword, "空消息响应应该包含错误关键词之一: " + errorKeywords);
    }

    @Test
    void testChatWithAI_NullMessage() {
        // When
        AiResponse response = aiChatService.chatWithAI(null, testHistory);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        String[] keywords = errorKeywords.split(",");
        boolean containsErrorKeyword = false;
        for (String keyword : keywords) {
            if (response.getContent().contains(keyword.trim())) {
                containsErrorKeyword = true;
                break;
            }
        }
        assertTrue(containsErrorKeyword, "null消息响应应该包含错误关键词之一: " + errorKeywords);
    }

    @Test
    void testChatWithAI_NullHistory() {
        // When
        AiResponse response = aiChatService.chatWithAI(simpleQuery, null);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    void testChatWithAI_EmptyHistory() {
        // When
        AiResponse response = aiChatService.chatWithAI(simpleQuery, new ArrayList<>());

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    void testSplitResponseForStreaming_ShortText() {
        // Given
        // 使用配置文件中的短消息
        String shortResponse = sampleAssistantMessage;

        // When
        List<String> chunks = aiChatService.splitResponseForStreaming(shortResponse);

        // Then
        assertNotNull(chunks);
        assertEquals(1, chunks.size());
        assertEquals(sampleAssistantMessage, chunks.get(0));
    }

    @Test
    void testSplitResponseForStreaming_LongText() {
        // Given
        // 使用配置文件中的长文本
        String longResponse = longQuery;

        // When
        List<String> chunks = aiChatService.splitResponseForStreaming(longResponse);

        // Then
        assertNotNull(chunks);
        assertTrue(chunks.size() > 1);
        
        // Verify all chunks combined equal original text
        StringBuilder combined = new StringBuilder();
        for (String chunk : chunks) {
            combined.append(chunk);
        }
        assertEquals(longResponse, combined.toString());
    }

    @Test
    void testSplitResponseForStreaming_EmptyText() {
        // When
        List<String> chunks = aiChatService.splitResponseForStreaming("");

        // Then
        assertNotNull(chunks);
        assertTrue(chunks.isEmpty());
    }

    @Test
    void testSplitResponseForStreaming_NullText() {
        // When
        List<String> chunks = aiChatService.splitResponseForStreaming(null);

        // Then
        assertNotNull(chunks);
        assertTrue(chunks.isEmpty());
    }

    @Test
    void testSplitResponseForStreaming_WithNewlines() {
        // Given
        // 使用配置文件中的换行文本
        String textWithNewlines = newlinesQuery;

        // When
        List<String> chunks = aiChatService.splitResponseForStreaming(textWithNewlines);

        // Then
        assertNotNull(chunks);
        assertTrue(chunks.size() > 0);
        
        // Verify newlines are preserved
        StringBuilder combined = new StringBuilder();
        for (String chunk : chunks) {
            combined.append(chunk);
        }
        assertEquals(textWithNewlines, combined.toString());
        assertTrue(combined.toString().contains("\n"));
    }

    @Test
    void testSplitResponseForStreaming_ChunkSizeLimit() {
        // Given
        // 使用StringBuilder创建重复字符串，兼容JDK 1.8
        // 创建长度为 chunkSize * 4 的字符串来测试分块
        StringBuilder sb = new StringBuilder();
        int totalLength = chunkSize * 4;
        for (int i = 0; i < totalLength; i++) {
            sb.append("A");
        }
        String response = sb.toString();

        // When
        List<String> chunks = aiChatService.splitResponseForStreaming(response);

        // Then
        assertNotNull(chunks);
        assertTrue(chunks.size() >= 4);
        
        // Verify each chunk (except possibly the last) is around chunkSize characters
        for (int i = 0; i < chunks.size() - 1; i++) {
            assertTrue(chunks.get(i).length() <= chunkSize, 
                      "Chunk " + i + " length should be <= " + chunkSize + " but was " + chunks.get(i).length());
        }
    }
}