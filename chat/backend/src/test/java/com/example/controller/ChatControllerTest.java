package com.example.controller;

import com.example.dto.request.StreamChatRequest;
import com.example.dto.stream.ChatEvent;
import com.example.service.AiChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.ServerSentEvent;
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
        ChatEvent eventResponse = ChatEvent.chunk("Hello");
        Flux<ChatEvent> responseFlux = Flux.just(eventResponse);
        
        when(aiChatService.streamChat(streamChatRequest)).thenReturn(responseFlux);

        // When
        Flux<ServerSentEvent<Object>> result = chatController.streamChat(conversationId, streamChatRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(ev -> {
                    return "chunk".equals(ev.event()) && ev.data() instanceof ChatEvent.ChunkPayload cp && "Hello".equals(cp.getContent());
                })
                .verifyComplete();
        
        verify(aiChatService).streamChat(streamChatRequest);
    }

    @Test
    void testStreamChat_WithoutMessage() {
        // Given
        Long conversationId = 1L;
        streamChatRequest.setMessage(null);

        // When
        Flux<ServerSentEvent<Object>> result = chatController.streamChat(conversationId, streamChatRequest);

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
        Flux<ServerSentEvent<Object>> result = chatController.streamChat(conversationId, streamChatRequest);

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
        
        ChatEvent eventResponse = ChatEvent.chunk("Hello with search");
        Flux<ChatEvent> responseFlux = Flux.just(eventResponse);
        
        when(aiChatService.streamChat(streamChatRequest)).thenReturn(responseFlux);

        // When
        Flux<ServerSentEvent<Object>> result = chatController.streamChat(conversationId, streamChatRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(ev -> "chunk".equals(ev.event()))
                .verifyComplete();
        
        verify(aiChatService).streamChat(streamChatRequest);
    }

    @Test
    void testStreamChat_WithDeepThinking() {
        // Given
        Long conversationId = 1L;
        streamChatRequest.setDeepThinking(true);
        
        ChatEvent eventResponse = ChatEvent.chunk("Hello with deep thinking");
        Flux<ChatEvent> responseFlux = Flux.just(eventResponse);
        
        when(aiChatService.streamChat(streamChatRequest)).thenReturn(responseFlux);

        // When
        Flux<ServerSentEvent<Object>> result = chatController.streamChat(conversationId, streamChatRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(ev -> "chunk".equals(ev.event()))
                .verifyComplete();
        
        verify(aiChatService).streamChat(streamChatRequest);
    }

    @Test
    void testStreamChat_WithProviderAndModel() {
        // Given
        Long conversationId = 1L;
        streamChatRequest.setProvider("qwen");
        streamChatRequest.setModel("qwen-plus");
        
        ChatEvent eventResponse = ChatEvent.chunk("Hello with provider and model");
        Flux<ChatEvent> responseFlux = Flux.just(eventResponse);
        
        when(aiChatService.streamChat(streamChatRequest)).thenReturn(responseFlux);

        // When
        Flux<ServerSentEvent<Object>> result = chatController.streamChat(conversationId, streamChatRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(ev -> "chunk".equals(ev.event()))
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
        
        ChatEvent eventResponse = ChatEvent.chunk("Hello with all parameters");
        Flux<ChatEvent> responseFlux = Flux.just(eventResponse);
        
        when(aiChatService.streamChat(streamChatRequest)).thenReturn(responseFlux);

        // When
        Flux<ServerSentEvent<Object>> result = chatController.streamChat(conversationId, streamChatRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(ev -> "chunk".equals(ev.event()))
                .verifyComplete();
        
        verify(aiChatService).streamChat(streamChatRequest);
    }
}
