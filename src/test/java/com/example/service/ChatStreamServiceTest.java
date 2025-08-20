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

  @Test
  void shouldHandleDeepThinkingMode() {
    // Given
    String prompt = "复杂问题需要深度思考";
    Long conversationId = 1L;
    
    // Mock ModelScopeDirectService行为  
    Flux<SseEventResponse> mockResponse = Flux.just(
        SseEventResponse.start("AI正在深度思考...")
    );
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, true))
        .thenReturn(mockResponse);

    // When & Then
    StepVerifier.create(chatStreamService.executeStreamingChat(prompt, conversationId, true))
        .expectNextMatches(event -> 
            "start".equals(event.getType()) && 
            "AI正在深度思考...".equals(event.getData()))
        .verifyComplete();
  }

  @Test
  void shouldHandle401Error() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(Flux.error(new RuntimeException("HTTP 401 Unauthorized")));

    // When & Then
    StepVerifier.create(chatStreamService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("API密钥无效"))
        .verifyComplete();
  }

  @Test
  void shouldHandle429Error() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(Flux.error(new RuntimeException("HTTP 429 Too Many Requests")));

    // When & Then
    StepVerifier.create(chatStreamService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("API调用频率超限"))
        .verifyComplete();
  }

  @Test
  void shouldHandleTimeoutError() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(Flux.error(new RuntimeException("Request timeout occurred")));

    // When & Then
    StepVerifier.create(chatStreamService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("请求超时"))
        .verifyComplete();
  }

  @Test
  void shouldHandleConnectionError() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(Flux.error(new RuntimeException("Connection refused")));

    // When & Then
    StepVerifier.create(chatStreamService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("网络连接失败"))
        .verifyComplete();
  }

  @Test
  void shouldHandleNullMessageError() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    // 创建一个没有message的异常
    RuntimeException errorWithoutMessage = new RuntimeException();
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(Flux.error(errorWithoutMessage));

    // When & Then
    StepVerifier.create(chatStreamService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("服务暂时不可用"))
        .verifyComplete();
  }

  @Test
  void shouldHandleGenericError() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(Flux.error(new RuntimeException("未知错误")));

    // When & Then
    StepVerifier.create(chatStreamService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("AI服务暂时不可用"))
        .verifyComplete();
  }

  @Test
  void shouldHandleTimeoutFromConfiguration() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    // 设置短超时时间
    when(streamingProperties.getResponseTimeout()).thenReturn(Duration.ofMillis(1));
    
    // Mock一个慢速响应
    Flux<SseEventResponse> slowResponse = Flux.just(SseEventResponse.chunk("慢速响应"))
        .delayElements(Duration.ofMillis(100));
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(slowResponse);

    // When & Then
    StepVerifier.create(chatStreamService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldLogDebugInformationOnStart() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    // Mock ModelScopeDirectService行为  
    Flux<SseEventResponse> mockResponse = Flux.just(
        SseEventResponse.start("正常响应")
    );
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(mockResponse);

    // When & Then
    StepVerifier.create(chatStreamService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "start".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleComplexErrorMessage() {
    // Given
    String prompt = "测试提示";
    Long conversationId = 1L;
    
    // 复杂的错误消息包含多个关键词
    when(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, false))
        .thenReturn(Flux.error(new RuntimeException("Connection timeout: HTTP 401 error")));

    // When & Then
    StepVerifier.create(chatStreamService.executeStreamingChat(prompt, conversationId, false))
        .expectNextMatches(event -> 
            "error".equals(event.getType()) && 
            event.getData().toString().contains("API密钥无效")) // 应该优先匹配401错误
        .verifyComplete();
  }
}