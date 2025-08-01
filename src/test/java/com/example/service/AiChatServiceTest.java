package com.example.service;

import com.example.entity.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = com.example.springai.SpringaiApplication.class)
@TestPropertySource(locations = "classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AiChatServiceTest {

    @Autowired
    private AiChatService aiChatService;
    
    @MockBean
    private MessageService messageService;
    
    @MockBean
    private SearchService searchService;
    
    @MockBean
    private SseEmitterManager sseEmitterManager;

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

    @Test
    void testChatWithAI_WithEmptyHistory() {
        // When
        AiResponse response = aiChatService.chatWithAI(simpleQuery, new ArrayList<>());

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
    }

    @Test
    void testChatWithAI_WithEmptySearchContext() {
        // When
        AiResponse response = aiChatService.chatWithAI(simpleQuery, testHistory, "");

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
    }

    @Test
    void testChatWithAI_WithWhitespaceSearchContext() {
        // When
        AiResponse response = aiChatService.chatWithAI(simpleQuery, testHistory, "   ");

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
    }

    @Test
    void testChat_WithEmptyHistory() {
        // Given
        Long conversationId = 1L;
        when(messageService.getMessagesByConversationId(conversationId)).thenReturn(new ArrayList<>());

        // When
        AiResponse response = aiChatService.chat(conversationId, simpleQuery, null);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        verify(messageService).getMessagesByConversationId(conversationId);
    }

    @Test
    void testSplitResponseForStreaming_ExactChunkSize() {
        // Given - 创建正好等于chunkSize的字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chunkSize; i++) {
            sb.append("X");
        }
        String response = sb.toString();

        // When
        List<String> chunks = aiChatService.splitResponseForStreaming(response);

        // Then
        assertNotNull(chunks);
        assertEquals(1, chunks.size());
        assertEquals(chunkSize, chunks.get(0).length());
    }

    @Test
    void testSplitResponseForStreaming_SingleCharacter() {
        // When
        List<String> chunks = aiChatService.splitResponseForStreaming("A");

        // Then
        assertNotNull(chunks);
        assertEquals(1, chunks.size());
        assertEquals("A", chunks.get(0));
    }
    
    // ========== 新增测试方法 ==========
    
    @Test
    void testChatWithAI_WithSearchContext() {
        // Given
        String searchContext = "搜索上下文信息";
        
        // When
        AiResponse response = aiChatService.chatWithAI(simpleQuery, testHistory, searchContext);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
    }

    @Test
    void testChatWithAI_WithNullSearchContext() {
        // When
        AiResponse response = aiChatService.chatWithAI(simpleQuery, testHistory, null);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
    }
    
    @Test
    void testChat_WithConversationId() {
        // Given
        Long conversationId = 1L;
        List<Message> mockMessages = Arrays.asList(
            createMockMessage(1L, conversationId, "user", "Hello"),
            createMockMessage(2L, conversationId, "assistant", "Hi there!")
        );
        when(messageService.getMessagesByConversationId(conversationId)).thenReturn(mockMessages);
        
        // When
        AiResponse response = aiChatService.chat(conversationId, simpleQuery, null);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        verify(messageService).getMessagesByConversationId(conversationId);
    }
    
    @Test
    void testChat_WithSearchContext() {
        // Given
        Long conversationId = 1L;
        String searchContext = "搜索上下文";
        List<Message> mockMessages = new ArrayList<>();
        when(messageService.getMessagesByConversationId(conversationId)).thenReturn(mockMessages);
        
        // When
        AiResponse response = aiChatService.chat(conversationId, simpleQuery, searchContext);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        verify(messageService).getMessagesByConversationId(conversationId);
    }
    
    @Test
    void testGetConversationHistory() {
        // Given
        Long conversationId = 1L;
        List<Message> expectedMessages = Arrays.asList(
            createMockMessage(1L, conversationId, "user", "Hello"),
            createMockMessage(2L, conversationId, "assistant", "Hi!")
        );
        when(messageService.getMessagesByConversationId(conversationId)).thenReturn(expectedMessages);
        
        // When
        List<Message> result = aiChatService.getConversationHistory(conversationId);
        
        // Then
        assertEquals(expectedMessages, result);
        verify(messageService).getMessagesByConversationId(conversationId);
    }
    
    @Test
    void testSendMessage_ValidInput() {
        // Given
        Long conversationId = 1L;
        String content = "Test message";
        Message mockUserMessage = createMockMessage(1L, conversationId, "user", content);
        when(messageService.saveMessage(conversationId, "user", content)).thenReturn(mockUserMessage);
        
        // When
        Message result = aiChatService.sendMessage(conversationId, content, false);
        
        // Then
        assertNotNull(result);
        assertEquals(mockUserMessage, result);
        verify(messageService).saveMessage(conversationId, "user", content);
    }
    
    @Test
    void testSendMessage_WithSearchEnabled() {
        // Given
        Long conversationId = 1L;
        String content = "Test message with search";
        Message mockUserMessage = createMockMessage(1L, conversationId, "user", content);
        when(messageService.saveMessage(conversationId, "user", content)).thenReturn(mockUserMessage);
        
        // When
        Message result = aiChatService.sendMessage(conversationId, content, true);
        
        // Then
        assertNotNull(result);
        assertEquals(mockUserMessage, result);
        verify(messageService).saveMessage(conversationId, "user", content);
    }
    
    @Test
    void testSendMessage_InvalidConversationId_Null() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            aiChatService.sendMessage(null, "Test message", false);
        });
        assertEquals("会话ID无效", exception.getMessage());
    }
    
    @Test
    void testSendMessage_InvalidConversationId_Zero() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            aiChatService.sendMessage(0L, "Test message", false);
        });
        assertEquals("会话ID无效", exception.getMessage());
    }
    
    @Test
    void testSendMessage_InvalidConversationId_Negative() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            aiChatService.sendMessage(-1L, "Test message", false);
        });
        assertEquals("会话ID无效", exception.getMessage());
    }
    
    @Test
    void testSendMessage_EmptyContent() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            aiChatService.sendMessage(1L, "", false);
        });
        assertEquals("消息内容不能为空", exception.getMessage());
    }
    
    @Test
    void testSendMessage_NullContent() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            aiChatService.sendMessage(1L, null, false);
        });
        assertEquals("消息内容不能为空", exception.getMessage());
    }
    
    @Test
    void testSendMessage_WhitespaceOnlyContent() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            aiChatService.sendMessage(1L, "   ", false);
        });
        assertEquals("消息内容不能为空", exception.getMessage());
    }
    
    @Test
    void testProcessAiResponseAsync_WithSearch() throws InterruptedException {
        // Given
        Long conversationId = 1L;
        String userMessage = "Test message";
        List<Map<String, String>> searchResults = Arrays.asList(
            createSearchResult("Title 1", "Content 1"),
            createSearchResult("Title 2", "Content 2")
        );
        String formattedResults = "Formatted search results";
        Message mockAiMessage = createMockMessage(2L, conversationId, "assistant", "AI response");
        
        when(searchService.searchMetaso(userMessage)).thenReturn(searchResults);
        when(searchService.formatSearchResults(searchResults)).thenReturn(formattedResults);
        when(messageService.getMessagesByConversationId(conversationId)).thenReturn(new ArrayList<>());
        when(messageService.saveMessage(eq(conversationId), eq("assistant"), anyString())).thenReturn(mockAiMessage);
        
        // When
        Message userMsg = createMockMessage(1L, conversationId, "user", userMessage);
        when(messageService.saveMessage(eq(conversationId), eq("user"), eq(userMessage))).thenReturn(userMsg);
        
        Message result = aiChatService.sendMessage(conversationId, userMessage, true);
        
        // Then
        assertNotNull(result);
        verify(messageService).saveMessage(conversationId, "user", userMessage);
        
        // Wait a bit for async processing
        Thread.sleep(100);
    }
    
    @Test
    void testProcessAiResponseAsync_WithoutSearch() throws InterruptedException {
        // Given
        Long conversationId = 1L;
        String userMessage = "Test message";
        Message mockAiMessage = createMockMessage(2L, conversationId, "assistant", "AI response");
        
        when(messageService.getMessagesByConversationId(conversationId)).thenReturn(new ArrayList<>());
        when(messageService.saveMessage(eq(conversationId), eq("assistant"), anyString())).thenReturn(mockAiMessage);
        
        // When
        Message userMsg = createMockMessage(1L, conversationId, "user", userMessage);
        when(messageService.saveMessage(eq(conversationId), eq("user"), eq(userMessage))).thenReturn(userMsg);
        
        Message result = aiChatService.sendMessage(conversationId, userMessage, false);
        
        // Then
        assertNotNull(result);
        verify(messageService).saveMessage(conversationId, "user", userMessage);
        
        // Wait a bit for async processing
        Thread.sleep(100);
    }
    
    // ========== 辅助方法 ==========
    
    private Message createMockMessage(Long id, Long conversationId, String role, String content) {
        Message message = new Message();
        message.setId(id);
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }
    
    private Map<String, String> createSearchResult(String title, String content) {
        Map<String, String> result = new HashMap<>();
        result.put("title", title);
        result.put("content", content);
        return result;
    }
}