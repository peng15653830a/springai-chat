package com.example.service;

import com.example.config.ChatStreamingProperties;
import com.example.service.dto.SseEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * ChatStreamService响应式流测试
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
class ChatStreamServiceTest {

  @Mock
  private ChatStreamingProperties streamingProperties;

  @Mock
  private ModelScopeDirectService modelScopeDirectService;

  private ChatStreamService chatStreamService;

  @BeforeEach
  void setUp() {
    chatStreamService = new ChatStreamService();
    ReflectionTestUtils.setField(chatStreamService, "streamingProperties", streamingProperties);
    ReflectionTestUtils.setField(chatStreamService, "modelScopeDirectService", modelScopeDirectService);
    
    // 设置默认配置
    when(streamingProperties.getResponseTimeout()).thenReturn(Duration.ofSeconds(30));
  }

  @Test
  void shouldGenerateStartEventForStreamingChat() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    // Mock ModelScopeDirectService行为  
    Flux<SseEventResponse> mockResponse = Flux.just(
        SseEventResponse.start("AI正在思考中..."),
        SseEventResponse.chunk("测试响应"),
        SseEventResponse.end(1L)
    );
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(mockResponse);

    // When & Then
    StepVerifier.create(chatStreamService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "start".equals(event.getType()) && 
            "AI正在思考中...".equals(event.getData()))
        .expectNextMatches(event -> 
            "chunk".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleErrorGracefully() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    // Mock ModelScopeDirectService抛出异常
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(Flux.error(new RuntimeException("AI服务不可用")));

    // When & Then
    StepVerifier.create(chatStreamService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("AI服务暂时不可用"))
        .verifyComplete();
  }
}