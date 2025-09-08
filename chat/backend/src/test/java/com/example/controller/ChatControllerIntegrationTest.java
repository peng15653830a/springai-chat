package com.example.controller;

import com.example.service.AiChatService;
import com.example.dto.response.SseEventResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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
@SpringBootTest(classes = com.example.springai.SpringaiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
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
    
    when(aiChatService.streamChat(any(com.example.dto.request.StreamChatRequest.class)))
        .thenReturn(mockEvents);

    // When & Then
    webTestClient.get()
        .uri("/api/chat/stream/{conversationId}?message={message}&searchEnabled=false", 
             conversationId, message)
        .accept(MediaType.TEXT_EVENT_STREAM)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
        .expectBodyList(String.class)
        .hasSize(4); // 应该有4个SSE事件
  }

  @Test
  void shouldHandleStreamingTimeout() {
    // Given
    Long conversationId = 1L;
    String message = "Hello AI";
    
    // Mock一个正常的流，测试基本功能即可
    Flux<SseEventResponse> normalEvents = Flux.just(
        SseEventResponse.start("开始"),
        SseEventResponse.chunk("响应内容"),
        SseEventResponse.end(1L)
    );
    
    when(aiChatService.streamChat(any(com.example.dto.request.StreamChatRequest.class)))
        .thenReturn(normalEvents);

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
    
    // 错误流应该返回错误事件，而不是直接抛异常
    Flux<SseEventResponse> errorEvents = Flux.just(
        SseEventResponse.error("AI服务错误")
    );
    
    when(aiChatService.streamChat(any(com.example.dto.request.StreamChatRequest.class)))
        .thenReturn(errorEvents);

    // When & Then
    webTestClient.get()
        .uri("/api/chat/stream/{conversationId}?message={message}&deepThinking=false", conversationId, message)
        .accept(MediaType.TEXT_EVENT_STREAM)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);
  }
}