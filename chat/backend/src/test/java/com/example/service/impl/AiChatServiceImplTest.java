package com.example.service.impl;

import com.example.config.ChatStreamingProperties;
import com.example.dto.request.ChatExecutionParams;
import com.example.dto.request.StreamChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.entity.Message;
import com.example.service.ChatModelService;
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
    private ChatModelService chatModelService;

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
        lenient().when(modelSelector.getActualModelName(any(), any())).thenReturn("default-model");
        lenient().when(chatModelService.streamChat(any())).thenReturn(Flux.just(SseEventResponse.chunk("Test response")));
        lenient().when(conversationService.generateTitleIfNeededAsync(any(), any())).thenReturn(Mono.empty());
        lenient().when(streamingProperties.getResponseTimeout()).thenReturn(Duration.ofSeconds(30));
    }

    @Test
    void shouldStreamChatWithModelSuccessfully() {
        // Given
        when(searchService.performSearchWithEvents(userMessage, false))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("", null, Flux.empty())));
        when(messageService.saveUserMessageAsync(conversationId, userMessage))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));
        when(promptBuilder.buildPrompt(anyLong(), anyString(), anyBoolean()))
            .thenReturn(Mono.just("构建的提示词"));

        // When & Then
        StepVerifier.create(aiChatService.streamChatWithModel(ChatExecutionParams.builder()
            .conversationId(conversationId)
            .userMessage(userMessage)
            .searchEnabled(false)
            .deepThinking(false)
            .build()))
            .expectNext(SseEventResponse.chunk("Test response"))
            .verifyComplete();

        verify(messageService).saveUserMessageAsync(conversationId, userMessage);
    }

    @Test
    void shouldStreamChatWithModelUsingChatExecutionParams() {
        // Given
        String providerName = "test-provider";
        String modelName = "test-model";

        ChatExecutionParams params = ChatExecutionParams.builder()
            .conversationId(conversationId)
            .userMessage(userMessage)
            .searchEnabled(false)
            .deepThinking(false)
            .userId(null)
            .providerName(providerName)
            .modelName(modelName)
            .build();

        when(searchService.performSearchWithEvents(userMessage, false))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("", null, Flux.empty())));
        when(messageService.saveUserMessageAsync(anyLong(), anyString()))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));
        when(promptBuilder.buildPrompt(anyLong(), anyString(), anyBoolean()))
            .thenReturn(Mono.just("构建的提示词"));

        // When & Then
        StepVerifier.create(aiChatService.streamChatWithModel(params))
            .expectNext(SseEventResponse.chunk("Test response"))
            .verifyComplete();
    }

    @Test
    void shouldStreamChatWithStreamChatRequestAndAllParameters() {
        // Given
        StreamChatRequest request = new StreamChatRequest();
        request.setConversationId(conversationId);
        request.setMessage(userMessage);
        request.setSearchEnabled(true);
        request.setDeepThinking(true);
        request.setUserId(123L);
        request.setProvider("test-provider");
        request.setModel("test-model");

        SearchService.SearchContextResult searchResult =
            new SearchService.SearchContextResult("", null, Flux.just(SseEventResponse.search("Searching for AI...")));
        when(searchService.performSearchWithEvents(userMessage, true))
            .thenReturn(Mono.just(searchResult));

        when(messageService.saveUserMessageAsync(anyLong(), anyString()))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));
        when(promptBuilder.buildPrompt(anyLong(), anyString(), anyBoolean()))
            .thenReturn(Mono.just("构建的提示词"));

        // When & Then
        StepVerifier.create(aiChatService.streamChat(request))
            .expectNext(SseEventResponse.search("Searching for AI..."))
            .expectNext(SseEventResponse.chunk("Test response"))
            .verifyComplete();
    }

    @Test
    void shouldSaveUserMessageAndGenerateTitle() {
        // Given
        Message mockMessage = new Message();
        mockMessage.setId(1L);
        mockMessage.setConversationId(conversationId);
        mockMessage.setRole(ROLE_USER);
        mockMessage.setContent(userMessage);
        mockMessage.setCreatedAt(LocalDateTime.now());
        
        when(searchService.performSearchWithEvents(userMessage, false))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("", null, Flux.empty())));
        when(messageService.saveUserMessageAsync(conversationId, userMessage))
            .thenReturn(Mono.just(mockMessage));
        when(promptBuilder.buildPrompt(anyLong(), anyString(), anyBoolean()))
            .thenReturn(Mono.just("构建的提示词"));

        // When & Then
        StepVerifier.create(aiChatService.streamChatWithModel(ChatExecutionParams.builder()
            .conversationId(conversationId)
            .userMessage(userMessage)
            .searchEnabled(false)
            .deepThinking(false)
            .build()))
            .expectNext(SseEventResponse.chunk("Test response"))
            .verifyComplete();

        verify(messageService).saveUserMessageAsync(conversationId, userMessage);
    }

    @Test
    void shouldHandleErrorInStreamChat() {
        // Given
        when(searchService.performSearchWithEvents(userMessage, false))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("", null, Flux.empty())));
        when(messageService.saveUserMessageAsync(anyLong(), anyString()))
            .thenReturn(Mono.error(new RuntimeException("Database error")));
        when(promptBuilder.buildPrompt(anyLong(), anyString(), anyBoolean()))
            .thenReturn(Mono.just("构建的提示词"));
        
        // Mock error handler
        when(errorHandler.handleChatError(any())).thenReturn(Flux.just(SseEventResponse.error("AI服务暂时不可用，请稍后重试")));

        // When & Then
        StepVerifier.create(aiChatService.streamChatWithModel(ChatExecutionParams.builder()
            .conversationId(conversationId)
            .userMessage(userMessage)
            .searchEnabled(false)
            .deepThinking(false)
            .build()))
            .expectNext(SseEventResponse.error("AI服务暂时不可用，请稍后重试"))
            .verifyComplete();
    }

    @Test
    void shouldStreamChatSuccessfully() {
        // Given
        when(searchService.performSearchWithEvents(userMessage, false))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("", null, Flux.empty())));
        when(messageService.saveUserMessageAsync(anyLong(), anyString()))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));
        when(promptBuilder.buildPrompt(anyLong(), anyString(), anyBoolean()))
            .thenReturn(Mono.just("构建的提示词"));

        // When & Then
        StepVerifier.create(aiChatService.streamChatWithModel(ChatExecutionParams.builder()
            .conversationId(conversationId)
            .userMessage(userMessage)
            .searchEnabled(false)
            .deepThinking(false)
            .build()))
            .expectNext(SseEventResponse.chunk("Test response"))
            .verifyComplete();

        verify(messageService).saveUserMessageAsync(conversationId, userMessage);
        verify(searchService, times(1)).performSearchWithEvents(userMessage, false);
    }

    @Test
    void shouldStreamChatWithSearchEnabled() {
        // Given
        when(searchService.performSearchWithEvents(userMessage, true))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("Search results", null, Flux.just(
                SseEventResponse.search("Searching..."),
                SseEventResponse.search("Found results")
            ))));
        
        when(messageService.saveUserMessageAsync(anyLong(), anyString()))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));
        when(promptBuilder.buildPrompt(anyLong(), anyString(), anyBoolean()))
            .thenReturn(Mono.just("构建的提示词"));

        // When & Then
        StepVerifier.create(aiChatService.streamChatWithModel(ChatExecutionParams.builder()
            .conversationId(conversationId)
            .userMessage(userMessage)
            .searchEnabled(true)
            .deepThinking(false)
            .build()))
            .expectNext(SseEventResponse.search("Searching..."))
            .expectNext(SseEventResponse.search("Found results"))
            .expectNext(SseEventResponse.chunk("Test response"))
            .verifyComplete();
    }

    @Test
    void shouldStreamChatWithDeepThinking() {
        // Given
        when(searchService.performSearchWithEvents(userMessage, false))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("", null, Flux.empty())));
        when(messageService.saveUserMessageAsync(anyLong(), anyString()))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));
        when(promptBuilder.buildPrompt(anyLong(), anyString(), anyBoolean()))
            .thenReturn(Mono.just("构建的提示词"));

        // When & Then
        StepVerifier.create(aiChatService.streamChatWithModel(ChatExecutionParams.builder()
            .conversationId(conversationId)
            .userMessage(userMessage)
            .searchEnabled(false)
            .deepThinking(true)
            .build()))
            .expectNext(SseEventResponse.chunk("Test response"))
            .verifyComplete();
    }

    @Test
    void shouldIncludeConversationHistoryInPrompt() {
        // Given
        when(searchService.performSearchWithEvents(userMessage, false))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("", null, Flux.empty())));
        when(messageService.saveUserMessageAsync(anyLong(), anyString()))
            .thenReturn(Mono.just(createMessage(3L, conversationId, ROLE_USER, userMessage)));
        when(promptBuilder.buildPrompt(anyLong(), anyString(), anyBoolean()))
            .thenReturn(Mono.just("构建的提示词"));

        // When & Then
        StepVerifier.create(aiChatService.streamChatWithModel(ChatExecutionParams.builder()
            .conversationId(conversationId)
            .userMessage(userMessage)
            .searchEnabled(false)
            .deepThinking(false)
            .build()))
            .expectNext(SseEventResponse.chunk("Test response"))
            .verifyComplete();

        verify(promptBuilder).buildPrompt(conversationId, userMessage, false);
    }

    @Test
    void shouldLimitHistoryToLast10Messages() {
        // Given
        when(searchService.performSearchWithEvents(userMessage, false))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("", null, Flux.empty())));
        when(messageService.saveUserMessageAsync(anyLong(), anyString()))
            .thenReturn(Mono.just(createMessage(12L, conversationId, ROLE_USER, userMessage)));
        when(promptBuilder.buildPrompt(anyLong(), anyString(), anyBoolean()))
            .thenReturn(Mono.just("构建的提示词"));

        // When & Then
        StepVerifier.create(aiChatService.streamChatWithModel(ChatExecutionParams.builder()
            .conversationId(conversationId)
            .userMessage(userMessage)
            .searchEnabled(false)
            .deepThinking(false)
            .build()))
            .expectNext(SseEventResponse.chunk("Test response"))
            .verifyComplete();

        verify(promptBuilder).buildPrompt(conversationId, userMessage, false);
    }

    @Test
    void shouldHandleErrorInSearchStep() {
        // Given
        when(searchService.performSearchWithEvents(userMessage, true))
            .thenReturn(Mono.error(new RuntimeException("Search service error")));
        when(messageService.saveUserMessageAsync(anyLong(), anyString()))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));
        when(promptBuilder.buildPrompt(anyLong(), anyString(), anyBoolean()))
            .thenReturn(Mono.just("构建的提示词"));
        when(errorHandler.handleChatError(any())).thenReturn(Flux.just(SseEventResponse.error("搜索服务错误")));

        // When & Then
        StepVerifier.create(aiChatService.streamChatWithModel(ChatExecutionParams.builder()
            .conversationId(conversationId)
            .userMessage(userMessage)
            .searchEnabled(true)
            .deepThinking(false)
            .build()))
            .expectNext(SseEventResponse.error("搜索服务错误"))
            .verifyComplete();
    }

    @Test
    void shouldHandleErrorInChatStreamService() {
        // Given
        when(searchService.performSearchWithEvents(userMessage, false))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("", null, Flux.empty())));
        when(messageService.saveUserMessageAsync(anyLong(), anyString()))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));
        when(promptBuilder.buildPrompt(anyLong(), anyString(), anyBoolean()))
            .thenReturn(Mono.error(new RuntimeException("Prompt building error")));
        when(errorHandler.handleChatError(any())).thenReturn(Flux.just(SseEventResponse.error("提示构建错误")));

        // When & Then
        StepVerifier.create(aiChatService.streamChatWithModel(ChatExecutionParams.builder()
            .conversationId(conversationId)
            .userMessage(userMessage)
            .searchEnabled(false)
            .deepThinking(false)
            .build()))
            .expectNext(SseEventResponse.error("提示构建错误"))
            .verifyComplete();
    }

    @Test
    void shouldGenerateTitleAsynchronously() {
        // Given
        when(searchService.performSearchWithEvents(userMessage, false))
            .thenReturn(Mono.just(new SearchService.SearchContextResult("", null, Flux.empty())));
        when(messageService.saveUserMessageAsync(anyLong(), anyString()))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));
        when(promptBuilder.buildPrompt(anyLong(), anyString(), anyBoolean()))
            .thenReturn(Mono.just("构建的提示词"));

        // When & Then
        StepVerifier.create(aiChatService.streamChatWithModel(ChatExecutionParams.builder()
            .conversationId(conversationId)
            .userMessage(userMessage)
            .searchEnabled(false)
            .deepThinking(false)
            .build()))
            .expectNext(SseEventResponse.chunk("Test response"))
            .verifyComplete();

        verify(conversationService).generateTitleIfNeededAsync(conversationId, userMessage);
    }
}