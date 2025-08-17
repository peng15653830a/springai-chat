package com.example.controller;

import com.example.service.AiChatService;
import com.example.service.dto.SseEventResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ChatController响应式集成测试
 *
 * @author xupeng
 */
@WebFluxTest(ChatController.class)
class ChatControllerIntegrationTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private AiChatService aiChatService;

  @Test
  void shouldStreamChatEvents() {
    // Given
    Long conversationId = 1L;
    String message = "Hello AI";
    
    Flux<SseEventResponse> mockEvents = Flux.just(
        SseEventResponse.start("AI正在思考中..."),
        SseEventResponse.chunk("Hello"),
        SseEventResponse.chunk(" World"),
        SseEventResponse.end(123L)
    );
    
    when(aiChatService.streamChat(anyLong(), anyString(), anyBoolean(), anyBoolean()))
        .thenReturn(mockEvents);

    // When & Then
    webTestClient.get()
        .uri("/api/chat/stream/{conversationId}?message={message}&searchEnabled=false", 
             conversationId, message)
        .accept(MediaType.TEXT_EVENT_STREAM)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
        .expectBody()
        .consumeWith(response -> {
          String body = new String(response.getResponseBody());
          // 验证SSE格式和内容
          assert body.contains("data:");
          assert body.contains("AI正在思考中");
          assert body.contains("Hello");
        });
  }

  @Test
  void shouldHandleStreamingTimeout() {
    // Given
    Long conversationId = 1L;
    String message = "Hello AI";
    
    // Mock一个慢的流，用于测试超时
    Flux<SseEventResponse> slowEvents = Flux.just(SseEventResponse.start("开始"))
        .delayElements(Duration.ofSeconds(35)); // 超过30秒超时
    
    when(aiChatService.streamChat(anyLong(), anyString(), anyBoolean(), anyBoolean()))
        .thenReturn(slowEvents);

    // When & Then
    webTestClient.get()
        .uri("/api/chat/stream/{conversationId}?message={message}&deepThinking=false", conversationId, message)
        .accept(MediaType.TEXT_EVENT_STREAM)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);
  }

  @Test
  void shouldHandleStreamingError() {
    // Given
    Long conversationId = 1L;
    String message = "Hello AI";
    
    when(aiChatService.streamChat(anyLong(), anyString(), anyBoolean(), anyBoolean()))
        .thenReturn(Flux.error(new RuntimeException("AI服务错误")));

    // When & Then
    webTestClient.get()
        .uri("/api/chat/stream/{conversationId}?message={message}&deepThinking=false", conversationId, message)
        .accept(MediaType.TEXT_EVENT_STREAM)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);
  }
}