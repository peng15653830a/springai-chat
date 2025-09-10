package com.example.service.impl;

import com.example.config.ChatStreamingProperties;
import com.example.dto.request.StreamChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.entity.Message;
import com.example.service.ChatClientManager;
import com.example.service.ConversationService;
import com.example.service.MessageService;
import com.example.service.SearchService;
import com.example.service.chat.ChatErrorHandler;
import com.example.service.chat.ModelSelector;
import com.example.service.chat.PromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequest.CallPromptRequest;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequest.CallPromptRequestSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.example.service.constants.AiChatConstants.ROLE_USER;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AiChatServiceImpl测试类
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
class AiChatServiceImplTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private SearchService searchService;

    @Mock
    private MessageService messageService;

    @Mock
    private ChatClientManager chatClientManager;

    @Mock
    private ChatClient chatClient;

    @Mock
    private CallPromptRequestSpec promptRequestSpec;

    @Mock
    private ModelSelector modelSelector;

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private ChatErrorHandler errorHandler;

    @Mock
    private ChatStreamingProperties streamingProperties;

    @InjectMocks
    private AiChatServiceImpl aiChatService;

    private final Long conversationId = 1L;
    private final String userMessage = "Hello, AI!";
    private final Long userId = 123L;

    // Helper method to create Message objects
    private Message createMessage(Long id, Long conversationId, String role, String content) {
        Message message = new Message();
        message.setId(id);
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

    @BeforeEach
    void setUp() {
        lenient().when(streamingProperties.getResponseTimeout()).thenReturn(Duration.ofSeconds(30));
        lenient().when(conversationService.generateTitleIfNeededAsync(any(), any())).thenReturn(Mono.empty());
        
        // Mock ModelSelector
        lenient().when(modelSelector.getActualProviderName(anyString())).thenReturn("test-provider");
        lenient().when(modelSelector.getActualModelName(anyString(), anyString())).thenReturn("test-model");
        lenient().when(modelSelector.selectModelForUser(any(), any(), any()))
            .thenReturn(new ModelSelector.ModelSelection("test-provider", "test-model"));
        
        // Mock ChatClient chain
        lenient().when(chatClientManager.getChatClient(anyString())).thenReturn(chatClient);
        lenient().when(chatClient.prompt()).thenReturn(promptRequestSpec);
        lenient().when(promptRequestSpec.user(anyString())).thenReturn(promptRequestSpec);
        lenient().when(promptRequestSpec.stream()).thenReturn(promptRequestSpec);
        lenient().when(promptRequestSpec.content()).thenReturn(Flux.just("AI response chunk"));
    }

    @Test
    void shouldStreamChatSuccessfully() {
        // Given
        StreamChatRequest request = new StreamChatRequest();
        request.setConversationId(conversationId);
        request.setMessage(userMessage);
        request.setSearchEnabled(false);
        request.setDeepThinking(false);
        request.setUserId(userId);
        request.setProvider("test-provider");
        request.setModel("test-model");

        when(messageService.saveUserMessageAsync(conversationId, userMessage))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));
        
        when(searchService.performSearchWithEvents(userMessage, false))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("", null, Flux.empty())));
        
        when(promptBuilder.buildPrompt(conversationId, userMessage, false))
            .thenReturn(Mono.just("构建的完整提示词"));
        
        when(messageService.saveAiMessageAsync(eq(conversationId), anyString(), isNull()))
            .thenReturn(Mono.just(SseEventResponse.end(null)));

        // When & Then
        StepVerifier.create(aiChatService.streamChat(request))
            .expectNext(SseEventResponse.start("AI正在思考中..."))
            .expectNext(SseEventResponse.chunk("AI response chunk"))
            .expectNext(SseEventResponse.end(null))
            .verifyComplete();

        verify(messageService).saveUserMessageAsync(conversationId, userMessage);
        verify(searchService).performSearchWithEvents(userMessage, false);
        verify(promptBuilder).buildPrompt(conversationId, userMessage, false);
        verify(chatClientManager).getChatClient("test-provider");
    }

    @Test
    void shouldStreamChatWithSearchEnabled() {
        // Given
        StreamChatRequest request = new StreamChatRequest();
        request.setConversationId(conversationId);
        request.setMessage(userMessage);
        request.setSearchEnabled(true);
        request.setDeepThinking(false);
        request.setProvider("test-provider");
        request.setModel("test-model");

        when(messageService.saveUserMessageAsync(conversationId, userMessage))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));
        
        SearchService.SearchContextResult searchResult = new SearchService.SearchContextResult(
            "search context", null, 
            Flux.just(
                SseEventResponse.search("正在搜索..."),
                SseEventResponse.search("找到相关信息")
            )
        );
        
        when(searchService.performSearchWithEvents(userMessage, true))
            .thenReturn(Mono.just(searchResult));
        
        when(promptBuilder.buildPrompt(conversationId, userMessage, true))
            .thenReturn(Mono.just("包含搜索结果的提示词"));
        
        when(messageService.saveAiMessageAsync(eq(conversationId), anyString(), isNull()))
            .thenReturn(Mono.just(SseEventResponse.end(null)));

        // When & Then
        StepVerifier.create(aiChatService.streamChat(request))
            .expectNext(SseEventResponse.search("正在搜索..."))
            .expectNext(SseEventResponse.search("找到相关信息"))
            .expectNext(SseEventResponse.start("AI正在思考中..."))
            .expectNext(SseEventResponse.chunk("AI response chunk"))
            .expectNext(SseEventResponse.end(null))
            .verifyComplete();

        verify(searchService).performSearchWithEvents(userMessage, true);
    }

    @Test
    void shouldStreamChatWithUserPreference() {
        // Given
        StreamChatRequest request = new StreamChatRequest();
        request.setConversationId(conversationId);
        request.setMessage(userMessage);
        request.setSearchEnabled(false);
        request.setDeepThinking(false);
        request.setUserId(userId);
        request.setProvider("openai");
        request.setModel("gpt-4");

        when(messageService.saveUserMessageAsync(conversationId, userMessage))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));
        
        when(searchService.performSearchWithEvents(userMessage, false))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("", null, Flux.empty())));
        
        when(promptBuilder.buildPrompt(conversationId, userMessage, false))
            .thenReturn(Mono.just("用户偏好模型的提示词"));
        
        when(modelSelector.selectModelForUser(userId, "openai", "gpt-4"))
            .thenReturn(new ModelSelector.ModelSelection("openai", "gpt-4-turbo"));
        
        when(messageService.saveAiMessageAsync(eq(conversationId), anyString(), isNull()))
            .thenReturn(Mono.just(SseEventResponse.end(null)));

        // When & Then
        StepVerifier.create(aiChatService.streamChat(request))
            .expectNext(SseEventResponse.start("AI正在思考中..."))
            .expectNext(SseEventResponse.chunk("AI response chunk"))
            .expectNext(SseEventResponse.end(null))
            .verifyComplete();

        verify(modelSelector).selectModelForUser(userId, "openai", "gpt-4");
        verify(chatClientManager).getChatClient("openai");
    }

    @Test
    void shouldHandleErrorInMessageSaving() {
        // Given
        StreamChatRequest request = new StreamChatRequest();
        request.setConversationId(conversationId);
        request.setMessage(userMessage);
        request.setSearchEnabled(false);
        request.setDeepThinking(false);

        when(messageService.saveUserMessageAsync(conversationId, userMessage))
            .thenReturn(Mono.error(new RuntimeException("Database error")));
        
        when(errorHandler.handleChatError(any()))
            .thenReturn(Flux.just(SseEventResponse.error("AI服务暂时不可用，请稍后重试")));

        // When & Then
        StepVerifier.create(aiChatService.streamChat(request))
            .expectNext(SseEventResponse.error("AI服务暂时不可用，请稍后重试"))
            .verifyComplete();

        verify(errorHandler).handleChatError(any());
    }

    @Test
    void shouldHandleErrorInChatClientCall() {
        // Given
        StreamChatRequest request = new StreamChatRequest();
        request.setConversationId(conversationId);
        request.setMessage(userMessage);
        request.setSearchEnabled(false);
        request.setDeepThinking(false);

        when(messageService.saveUserMessageAsync(conversationId, userMessage))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));
        
        when(searchService.performSearchWithEvents(userMessage, false))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("", null, Flux.empty())));
        
        when(promptBuilder.buildPrompt(conversationId, userMessage, false))
            .thenReturn(Mono.just("提示词"));

        when(chatClientManager.getChatClient(anyString()))
            .thenThrow(new RuntimeException("ChatClient creation failed"));
        
        when(errorHandler.handleChatError(any()))
            .thenReturn(Flux.just(SseEventResponse.error("AI服务暂时不可用，请稍后重试")));

        // When & Then
        StepVerifier.create(aiChatService.streamChat(request))
            .expectNext(SseEventResponse.error("AI服务暂时不可用，请稍后重试"))
            .verifyComplete();
    }

    @Test
    void shouldHandleEmptyAiResponse() {
        // Given
        StreamChatRequest request = new StreamChatRequest();
        request.setConversationId(conversationId);
        request.setMessage(userMessage);
        request.setSearchEnabled(false);
        request.setDeepThinking(false);

        when(messageService.saveUserMessageAsync(conversationId, userMessage))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));
        
        when(searchService.performSearchWithEvents(userMessage, false))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("", null, Flux.empty())));
        
        when(promptBuilder.buildPrompt(conversationId, userMessage, false))
            .thenReturn(Mono.just("提示词"));
        
        // Mock empty response from AI
        when(promptRequestSpec.content()).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(aiChatService.streamChat(request))
            .expectNext(SseEventResponse.start("AI正在思考中..."))
            .expectNext(SseEventResponse.end(null))
            .verifyComplete();
    }
}