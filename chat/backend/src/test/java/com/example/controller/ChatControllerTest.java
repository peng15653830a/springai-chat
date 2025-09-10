package com.example.controller;

import com.example.dto.request.StreamChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.service.AiChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private AiChatService aiChatService;

    @InjectMocks
    private ChatController chatController;

    private StreamChatRequest streamChatRequest;

    @BeforeEach
    void setUp() {
        streamChatRequest = new StreamChatRequest();
        streamChatRequest.setConversationId(1L);
        streamChatRequest.setUserId(1L);
        streamChatRequest.setMessage("Hello, AI!");
    }

    @Test
    void testStreamChat_WithMessage() {
        // Given
        Long conversationId = 1L;
        SseEventResponse eventResponse = new SseEventResponse("message", "Hello");
        Flux<SseEventResponse> responseFlux = Flux.just(eventResponse);
        
        when(aiChatService.streamChat(streamChatRequest)).thenReturn(responseFlux);

        // When
        Flux<SseEventResponse> result = chatController.streamChat(conversationId, streamChatRequest);

        // Then
        StepVerifier.create(result)
                .expectNext(eventResponse)
                .verifyComplete();
        
        verify(aiChatService).streamChat(streamChatRequest);
    }

    @Test
    void testStreamChat_WithoutMessage() {
        // Given
        Long conversationId = 1L;
        streamChatRequest.setMessage(null);

        // When
        Flux<SseEventResponse> result = chatController.streamChat(conversationId, streamChatRequest);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testStreamChat_WithEmptyMessage() {
        // Given
        Long conversationId = 1L;
        streamChatRequest.setMessage("   ");

        // When
        Flux<SseEventResponse> result = chatController.streamChat(conversationId, streamChatRequest);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testStreamChat_ServiceError() {
        // Given
        Long conversationId = 1L;
        RuntimeException exception = new RuntimeException("Service error");
        
        when(aiChatService.streamChat(streamChatRequest)).thenThrow(exception);

        // When & Then
        // 验证抛出的异常
        try {
            chatController.streamChat(conversationId, streamChatRequest);
        } catch (RuntimeException e) {
            // 预期的异常
            assertEquals("Service error", e.getMessage());
        }
        
        verify(aiChatService).streamChat(streamChatRequest);
    }

    @Test
    void testStreamChat_WithSearchEnabled() {
        // Given
        Long conversationId = 1L;
        streamChatRequest.setSearchEnabled(true);
        
        SseEventResponse eventResponse = new SseEventResponse("message", "Hello with search");
        Flux<SseEventResponse> responseFlux = Flux.just(eventResponse);
        
        when(aiChatService.streamChat(streamChatRequest)).thenReturn(responseFlux);

        // When
        Flux<SseEventResponse> result = chatController.streamChat(conversationId, streamChatRequest);

        // Then
        StepVerifier.create(result)
                .expectNext(eventResponse)
                .verifyComplete();
        
        verify(aiChatService).streamChat(streamChatRequest);
    }

    @Test
    void testStreamChat_WithDeepThinking() {
        // Given
        Long conversationId = 1L;
        streamChatRequest.setDeepThinking(true);
        
        SseEventResponse eventResponse = new SseEventResponse("message", "Hello with deep thinking");
        Flux<SseEventResponse> responseFlux = Flux.just(eventResponse);
        
        when(aiChatService.streamChat(streamChatRequest)).thenReturn(responseFlux);

        // When
        Flux<SseEventResponse> result = chatController.streamChat(conversationId, streamChatRequest);

        // Then
        StepVerifier.create(result)
                .expectNext(eventResponse)
                .verifyComplete();
        
        verify(aiChatService).streamChat(streamChatRequest);
    }

    @Test
    void testStreamChat_WithProviderAndModel() {
        // Given
        Long conversationId = 1L;
        streamChatRequest.setProvider("qwen");
        streamChatRequest.setModel("qwen-plus");
        
        SseEventResponse eventResponse = new SseEventResponse("message", "Hello with provider and model");
        Flux<SseEventResponse> responseFlux = Flux.just(eventResponse);
        
        when(aiChatService.streamChat(streamChatRequest)).thenReturn(responseFlux);

        // When
        Flux<SseEventResponse> result = chatController.streamChat(conversationId, streamChatRequest);

        // Then
        StepVerifier.create(result)
                .expectNext(eventResponse)
                .verifyComplete();
        
        verify(aiChatService).streamChat(streamChatRequest);
    }

    @Test
    void testStreamChat_WithAllParameters() {
        // Given
        Long conversationId = 1L;
        streamChatRequest.setUserId(123L);
        streamChatRequest.setSearchEnabled(true);
        streamChatRequest.setDeepThinking(true);
        streamChatRequest.setProvider("deepseek");
        streamChatRequest.setModel("deepseek-chat");
        
        SseEventResponse eventResponse = new SseEventResponse("message", "Hello with all parameters");
        Flux<SseEventResponse> responseFlux = Flux.just(eventResponse);
        
        when(aiChatService.streamChat(streamChatRequest)).thenReturn(responseFlux);

        // When
        Flux<SseEventResponse> result = chatController.streamChat(conversationId, streamChatRequest);

        // Then
        StepVerifier.create(result)
                .expectNext(eventResponse)
                .verifyComplete();
        
        verify(aiChatService).streamChat(streamChatRequest);
    }
}