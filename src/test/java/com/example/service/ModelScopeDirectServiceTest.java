package com.example.service;

import com.example.config.MultiModelProperties;
import com.example.dto.response.SseEventResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.quality.Strictness;

/**
 * ModelScopeDirectService测试
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ModelScopeDirectServiceTest {

  @Mock
  private WebClient.Builder webClientBuilder;

  @Mock
  private WebClient webClient;

  @Mock
  private WebClient.RequestBodyUriSpec requestBodyUriSpec;

  @Mock
  private WebClient.RequestBodySpec requestBodySpec;

  @Mock
  private WebClient.RequestHeadersSpec requestHeadersSpec;

  @Mock
  private WebClient.ResponseSpec responseSpec;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private MessageService messageService;

  @Mock
  private MultiModelProperties multiModelProperties;

  private ModelScopeDirectService modelScopeDirectService;

  @BeforeEach
  void setUp() {
    when(webClientBuilder.build()).thenReturn(webClient);
    
    // 配置MultiModelProperties
    MultiModelProperties.ProviderConfig qwenProvider = new MultiModelProperties.ProviderConfig();
    qwenProvider.setBaseUrl("https://api-inference.modelscope.cn");
    
    MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
    modelConfig.setName("Qwen/Qwen3-235B-A22B-Thinking-2507");
    modelConfig.setTemperature(java.math.BigDecimal.valueOf(0.7));
    modelConfig.setMaxTokens(2000);
    modelConfig.setSupportsThinking(true);
    modelConfig.setThinkingBudget(50000);
    qwenProvider.setModels(java.util.List.of(modelConfig));
    
    java.util.Map<String, MultiModelProperties.ProviderConfig> providers = new java.util.HashMap<>();
    providers.put("qwen", qwenProvider);
    
    when(multiModelProperties.getProviders()).thenReturn(providers);
    when(multiModelProperties.getApiKey("qwen")).thenReturn("test-api-key");
    
    modelScopeDirectService = new ModelScopeDirectService(webClientBuilder, objectMapper, messageService, multiModelProperties);
  }

  @Test
  void shouldExecuteDirectStreamingWithoutThinking() {
    // Given
    String prompt = "Hello AI";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String responseJson = "{\"choices\":[{\"delta\":{\"content\":\"Hello! How can I help you?\"}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(responseJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(responseJson)).thenReturn(createMockJsonNode("Hello! How can I help you?", ""));
    } catch (Exception e) {
      // Handle checked exception
    }
    
    // Mock MessageService
    when(messageService.saveAiMessageAsync(conversationId, "Hello! How can I help you?", null))
        .thenReturn(Mono.just(SseEventResponse.end(null)));

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "chunk".equals(event.getType()) && 
                          event.getData() instanceof SseEventResponse.ChunkData &&
                          "Hello! How can I help you?".equals(((SseEventResponse.ChunkData) event.getData()).getContent()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldExecuteDirectStreamingWithThinking() {
    // Given
    String prompt = "Solve this complex problem";
    Long conversationId = 1L;
    boolean deepThinking = true;
    
    String thinkingJson = "{\"choices\":[{\"delta\":{\"reasoning_content\":\"Let me think about this...\"}}]}";
    String contentJson = "{\"choices\":[{\"delta\":{\"content\":\"Here's the solution.\"}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(thinkingJson, contentJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(thinkingJson)).thenReturn(createMockJsonNode("", "Let me think about this..."));
      when(objectMapper.readTree(contentJson)).thenReturn(createMockJsonNode("Here's the solution.", ""));
    } catch (Exception e) {
      // Handle checked exception
    }
    
    // Mock MessageService  
    when(messageService.saveAiMessageAsync(conversationId, "Here's the solution.", "Let me think about this..."))
        .thenReturn(Mono.just(SseEventResponse.end(null)));

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "thinking".equals(event.getType()) && 
                          event.getData() instanceof SseEventResponse.ChunkData &&
                          "Let me think about this...".equals(((SseEventResponse.ChunkData) event.getData()).getContent()))
        .expectNextMatches(event -> "chunk".equals(event.getType()) && 
                          event.getData() instanceof SseEventResponse.ChunkData &&
                          "Here's the solution.".equals(((SseEventResponse.ChunkData) event.getData()).getContent()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleEmptyContent() {
    // Given
    String prompt = "Empty response test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String emptyJson = "{\"choices\":[{\"delta\":{}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(emptyJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(emptyJson)).thenReturn(createMockJsonNode("", ""));
    } catch (Exception e) {
      // Handle checked exception
    }

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleApiError() {
    // Given
    String prompt = "Error test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    // Mock WebClient chain to throw error
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.error(new RuntimeException("API调用失败")));

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "error".equals(event.getType()) && 
                          event.getData().toString().contains("AI服务暂时不可用"))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleJsonParsingError() {
    // Given
    String prompt = "JSON parsing test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String invalidJson = "invalid json";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(invalidJson, "[DONE]"));
    
    // ObjectMapper会抛出异常，但会被捕获处理

    // When & Then  
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldSkipInvalidLines() {
    // Given
    String prompt = "Skip invalid lines test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String validJson = "{\"choices\":[{\"delta\":{\"content\":\"Valid content\"}}]}";
    
    // Mock WebClient chain - including invalid lines
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(
        "",
        "data: invalid",
        validJson,
        "[DONE]"
    ));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(validJson)).thenReturn(createMockJsonNode("Valid content", ""));
    } catch (Exception e) {
      // Handle checked exception
    }
    
    // Mock MessagePersistenceService
    when(messageService.saveAiMessageAsync(conversationId, "Valid content", null))
        .thenReturn(Mono.just(SseEventResponse.end(null)));

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "chunk".equals(event.getType()) && 
                          event.getData() instanceof SseEventResponse.ChunkData &&
                          "Valid content".equals(((SseEventResponse.ChunkData) event.getData()).getContent()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }


  @Test
  void shouldHandleEmptyContentForSaving() {
    // Given
    String prompt = "Empty content save test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String emptyJson = "{\"choices\":[{\"delta\":{}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(emptyJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(emptyJson)).thenReturn(createMockJsonNode("", ""));
    } catch (Exception e) {
      // Handle checked exception
    }

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
        
    // 验证没有调用保存方法
    verify(messageService, never()).saveAiMessageAsync(any(), any(), any());
  }

  @Test
  void shouldHandleNullContentForSaving() {
    // Given - 测试null content的保存分支
    String prompt = "Null content test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    // Mock WebClient chain返回空结果
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("[DONE]"));

    // When & Then - 验证null content处理
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
        
    // 验证没有调用保存方法
    verify(messageService, never()).saveAiMessageAsync(any(), any(), any());
  }

  @Test
  void shouldHandleDeepThinkingDisabledBranch() {
    // Given - 测试deepThinking=true但supportsThinking=false的分支
    String prompt = "Deep thinking disabled test";
    Long conversationId = 1L;
    boolean deepThinking = true;
    
    // 设置supportsThinking为false来测试分支
    MultiModelProperties.ModelConfig disabledThinkingModel = new MultiModelProperties.ModelConfig();
    disabledThinkingModel.setName("Qwen/Qwen3-235B-A22B-Thinking-2507");
    disabledThinkingModel.setTemperature(java.math.BigDecimal.valueOf(0.7));
    disabledThinkingModel.setMaxTokens(2000);
    disabledThinkingModel.setSupportsThinking(false);
    disabledThinkingModel.setThinkingBudget(50000);
    
    MultiModelProperties.ProviderConfig qwenProvider = new MultiModelProperties.ProviderConfig();
    qwenProvider.setBaseUrl("https://api-inference.modelscope.cn");
    qwenProvider.setModels(java.util.List.of(disabledThinkingModel));
    
    java.util.Map<String, MultiModelProperties.ProviderConfig> providers = new java.util.HashMap<>();
    providers.put("qwen", qwenProvider);
    
    when(multiModelProperties.getProviders()).thenReturn(providers);
    
    String responseJson = "{\"choices\":[{\"delta\":{\"content\":\"Response without thinking\"}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(responseJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(responseJson)).thenReturn(createMockJsonNode("Response without thinking", ""));
    } catch (Exception e) {
      // Handle checked exception
    }
    
    // Mock MessagePersistenceService
    when(messageService.saveAiMessageAsync(conversationId, "Response without thinking", null))
        .thenReturn(Mono.just(SseEventResponse.end(null)));

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "chunk".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleWhitespaceOnlyContent() {
    // Given - 测试只包含空白字符的content
    String prompt = "Whitespace content test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String whitespaceJson = "{\"choices\":[{\"delta\":{\"content\":\"   \\t\\n   \"}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(whitespaceJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(whitespaceJson)).thenReturn(createMockJsonNode("   \t\n   ", ""));
    } catch (Exception e) {
      // Handle checked exception
    }

    // When & Then - 空白内容应该导致直接结束，不保存
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "chunk".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
        
    // 验证没有调用保存方法（因为trim后为空）
    verify(messageService, never()).saveAiMessageAsync(any(), any(), any());
  }

  @Test
  void shouldHandleEmptyAndInvalidLines() {
    // Given - 测试各种无效行的过滤逻辑
    String prompt = "Invalid lines filter test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String validJson = "{\"choices\":[{\"delta\":{\"content\":\"Valid response\"}}]}";
    
    // Mock WebClient chain - 包含各种无效行
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(
        "", // 空行
        "   ", // 只有空格
        "data: invalid format", // 无效格式
        "not json at all", // 不是JSON
        "malformed{", // 格式错误
        validJson, // 有效JSON
        "[DONE]"
    ));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(validJson)).thenReturn(createMockJsonNode("Valid response", ""));
    } catch (Exception e) {
      // Handle checked exception
    }
    
    // Mock MessagePersistenceService
    when(messageService.saveAiMessageAsync(conversationId, "Valid response", null))
        .thenReturn(Mono.just(SseEventResponse.end(null)));

    // When & Then - 只有有效JSON应该被处理
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "chunk".equals(event.getType()) && 
                          "Valid response".equals(((SseEventResponse.ChunkData) event.getData()).getContent()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleJsonParsingExceptionInParseChunk() {
    // Given - 测试parseJsonChunk中的异常处理分支
    String prompt = "JSON parsing exception test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String malformedJson = "{\"choices\":[{\"delta\":{\"content\":\"test\"}}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(malformedJson, "[DONE]"));
    
    // Mock ObjectMapper to throw exception
    try {
      when(objectMapper.readTree(malformedJson)).thenThrow(new RuntimeException("JSON parsing failed"));
    } catch (Exception e) {
      // Handle checked exception
    }

    // When & Then - 解析异常应该被捕获，不产生任何事件
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
        
    // 验证没有调用保存方法
    verify(messageService, never()).saveAiMessageAsync(any(), any(), any());
  }

  @Test
  void shouldHandleEmptyChoicesArray() {
    // Given - 测试choices数组为空的情况
    String prompt = "Empty choices test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String emptyChoicesJson = "{\"choices\":[]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(emptyChoicesJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      com.fasterxml.jackson.databind.node.ObjectNode emptyNode = new ObjectMapper().createObjectNode();
      emptyNode.putArray("choices"); // 空数组
      when(objectMapper.readTree(emptyChoicesJson)).thenReturn(emptyNode);
    } catch (Exception e) {
      // Handle checked exception
    }

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
        
    // 验证没有调用保存方法
    verify(messageService, never()).saveAiMessageAsync(any(), any(), any());
  }

  @Test
  void shouldHandleMissingChoicesField() {
    // Given - 测试缺少choices字段的JSON
    String prompt = "Missing choices test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String noChoicesJson = "{\"some_other_field\":\"value\"}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(noChoicesJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      com.fasterxml.jackson.databind.node.ObjectNode noChoicesNode = new ObjectMapper().createObjectNode();
      noChoicesNode.put("some_other_field", "value");
      when(objectMapper.readTree(noChoicesJson)).thenReturn(noChoicesNode);
    } catch (Exception e) {
      // Handle checked exception
    }

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
        
    // 验证没有调用保存方法
    verify(messageService, never()).saveAiMessageAsync(any(), any(), any());
  }

  @Test
  void shouldHandleOnlyReasoningContentWithoutNormalContent() {
    // Given - 测试只有推理内容，没有普通内容的情况
    String prompt = "Only reasoning test";
    Long conversationId = 1L;
    boolean deepThinking = true;
    
    String onlyReasoningJson = "{\"choices\":[{\"delta\":{\"reasoning_content\":\"Only thinking...\"}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(onlyReasoningJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(onlyReasoningJson)).thenReturn(createMockJsonNode("", "Only thinking..."));
    } catch (Exception e) {
      // Handle checked exception
    }

    // When & Then - 只有推理内容时，应该产生thinking事件但不保存（因为没有普通内容）
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "thinking".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
        
    // 验证没有调用保存方法（因为最终content为空）
    verify(messageService, never()).saveAiMessageAsync(any(), any(), any());
  }

  @Test
  void shouldHandleNullEventData() {
    // Given - 测试event.getData()为null的分支
    String prompt = "Null event data test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    // 创建一个data为null的事件来测试doOnNext中的null检查
    String responseJson = "{\"choices\":[{\"delta\":{\"content\":\"Test content\"}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(responseJson, "[DONE]"));
    
    // Mock ObjectMapper - 返回null content来测试getData() == null的情况
    try {
      when(objectMapper.readTree(responseJson)).thenReturn(createMockJsonNodeWithNullContent());
    } catch (Exception e) {
      // Handle checked exception
    }

    // When & Then - 应该处理null data而不出错
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
        
    // 验证没有调用保存方法（因为内容为空）
    verify(messageService, never()).saveAiMessageAsync(any(), any(), any());
  }

  @Test
  void shouldHandleEmptyReasoningContent() {
    // Given - 测试只有content没有reasoning_content的情况
    String prompt = "Test prompt";
    Long conversationId = 1L;
    boolean deepThinking = true;
    
    String contentJson = "{\"choices\":[{\"delta\":{\"content\":\"Only content\"}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(contentJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(contentJson)).thenReturn(createMockJsonNode("Only content", ""));
    } catch (Exception e) {
      // Handle checked exception
    }
    
    // Mock MessageService
    when(messageService.saveAiMessageAsync(conversationId, "Only content", null))
        .thenReturn(Mono.just(SseEventResponse.end(null)));

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "chunk".equals(event.getType()) && 
                          "Only content".equals(((SseEventResponse.ChunkData) event.getData()).getContent()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleBothContentAndReasoning() {
    // Given - 测试同时有content和reasoning_content的情况
    String prompt = "Complex test";
    Long conversationId = 1L;
    boolean deepThinking = true;
    
    String reasoningJson = "{\"choices\":[{\"delta\":{\"reasoning_content\":\"Thinking process\"}}]}";
    String contentJson = "{\"choices\":[{\"delta\":{\"content\":\"Final response\"}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(reasoningJson, contentJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(reasoningJson)).thenReturn(createMockJsonNode("", "Thinking process"));
      when(objectMapper.readTree(contentJson)).thenReturn(createMockJsonNode("Final response", ""));
    } catch (Exception e) {
      // Handle checked exception
    }
    
    // Mock MessageService
    when(messageService.saveAiMessageAsync(conversationId, "Final response", "Thinking process"))
        .thenReturn(Mono.just(SseEventResponse.end(null)));

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "thinking".equals(event.getType()) && 
                          "Thinking process".equals(((SseEventResponse.ChunkData) event.getData()).getContent()))
        .expectNextMatches(event -> "chunk".equals(event.getType()) && 
                          "Final response".equals(((SseEventResponse.ChunkData) event.getData()).getContent()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleEmptyChoicesArray() {
    // Given - 测试choices数组为空的情况
    String prompt = "Empty choices test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String emptyChoicesJson = "{\"choices\":[]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(emptyChoicesJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      com.fasterxml.jackson.databind.node.ObjectNode emptyNode = new ObjectMapper().createObjectNode();
      emptyNode.putArray("choices"); // 空数组
      when(objectMapper.readTree(emptyChoicesJson)).thenReturn(emptyNode);
    } catch (Exception e) {
      // Handle checked exception
    }

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
        
    // 验证没有调用保存方法
    verify(messageService, never()).saveAiMessageAsync(any(), any(), any());
  }

  @Test
  void shouldHandleMissingChoicesField() {
    // Given - 测试缺少choices字段的JSON
    String prompt = "Missing choices test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String noChoicesJson = "{\"some_other_field\":\"value\"}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(noChoicesJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      com.fasterxml.jackson.databind.node.ObjectNode noChoicesNode = new ObjectMapper().createObjectNode();
      noChoicesNode.put("some_other_field", "value");
      when(objectMapper.readTree(noChoicesJson)).thenReturn(noChoicesNode);
    } catch (Exception e) {
      // Handle checked exception
    }

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
        
    // 验证没有调用保存方法
    verify(messageService, never()).saveAiMessageAsync(any(), any(), any());
  }

  @Test
  void shouldHandleInvalidJsonInParseChunk() {
    // Given - 测试parseJsonChunk中的无效JSON处理
    String prompt = "Invalid JSON test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String invalidJson = "{\"invalid\": json}"; // 无效JSON
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(invalidJson, "[DONE]"));
    
    // Mock ObjectMapper to throw exception
    try {
      when(objectMapper.readTree(invalidJson)).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Invalid JSON") {});
    } catch (Exception e) {
      // Handle checked exception
    }

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
        
    // 验证没有调用保存方法
    verify(messageService, never()).saveAiMessageAsync(any(), any(), any());
  }

  @Test
  void shouldHandleSaveMessageError() {
    // Given - 测试保存消息时的错误处理
    String prompt = "Save error test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String responseJson = "{\"choices\":[{\"delta\":{\"content\":\"Test response\"}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(responseJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(responseJson)).thenReturn(createMockJsonNode("Test response", ""));
    } catch (Exception e) {
      // Handle checked exception
    }
    
    // Mock MessageService to throw error
    when(messageService.saveAiMessageAsync(conversationId, "Test response", null))
        .thenReturn(Mono.error(new RuntimeException("Save failed")));

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "chunk".equals(event.getType()))
        .expectNextMatches(event -> "error".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleEmptyResponse() {
    // Given - 测试空响应的情况
    String prompt = "Empty response test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    // Mock WebClient chain with empty response
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("[DONE]")); // 只有结束标记

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
        
    // 验证没有调用保存方法
    verify(messageService, never()).saveAiMessageAsync(any(), any(), any());
  }

  @Test
  void shouldHandleWhitespaceOnlyResponse() {
    // Given - 测试只有空白字符的响应
    String prompt = "Whitespace only test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String whitespaceJson = "{\"choices\":[{\"delta\":{\"content\":\"   \\t\\n   \"}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(whitespaceJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(whitespaceJson)).thenReturn(createMockJsonNode("   \t\n   ", ""));
    } catch (Exception e) {
      // Handle checked exception
    }

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "chunk".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleSpecialCharactersInResponse() {
    // Given - 测试响应中的特殊字符
    String prompt = "Special characters test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String specialJson = "{\"choices\":[{\"delta\":{\"content\":\"特殊字符🌟🔍🚀\"}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(specialJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(specialJson)).thenReturn(createMockJsonNode("特殊字符🌟🔍🚀", ""));
    } catch (Exception e) {
      // Handle checked exception
    }
    
    // Mock MessageService
    when(messageService.saveAiMessageAsync(conversationId, "特殊字符🌟🔍🚀", null))
        .thenReturn(Mono.just(SseEventResponse.end(null)));

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "chunk".equals(event.getType()) && 
                          "特殊字符🌟🔍🚀".equals(((SseEventResponse.ChunkData) event.getData()).getContent()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleVeryLongResponse() {
    // Given - 测试非常长的响应
    String prompt = "Long response test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    StringBuilder longContent = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longContent.append("这是一个很长的响应内容，用来测试处理长文本的能力。");
    }
    String longResponse = longContent.toString();
    
    String longJson = "{\"choices\":[{\"delta\":{\"content\":\"" + longResponse + "\"}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(longJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(longJson)).thenReturn(createMockJsonNode(longResponse, ""));
    } catch (Exception e) {
      // Handle checked exception
    }
    
    // Mock MessageService
    when(messageService.saveAiMessageAsync(conversationId, longResponse, null))
        .thenReturn(Mono.just(SseEventResponse.end(null)));

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "chunk".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleNullContentInSaveMessage() {
    // Given - 测试保存消息时content为null的情况
    String prompt = "Null content test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String responseJson = "{\"choices\":[{\"delta\":{\"content\":null}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(responseJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      com.fasterxml.jackson.databind.node.ObjectNode rootNode = new ObjectMapper().createObjectNode();
      com.fasterxml.jackson.databind.node.ArrayNode choices = rootNode.putArray("choices");
      com.fasterxml.jackson.databind.node.ObjectNode choice = choices.addObject();
      com.fasterxml.jackson.databind.node.ObjectNode delta = choice.putObject("delta");
      delta.putNull("content"); // null content
      when(objectMapper.readTree(responseJson)).thenReturn(rootNode);
    } catch (Exception e) {
      // Handle checked exception
    }

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
        
    // 验证没有调用保存方法
    verify(messageService, never()).saveAiMessageAsync(any(), any(), any());
  }

  @Test
  void shouldHandleThinkingWithSpecialCharacters() {
    // Given - 测试推理内容中的特殊字符
    String prompt = "Thinking special chars test";
    Long conversationId = 1L;
    boolean deepThinking = true;
    
    String thinkingJson = "{\"choices\":[{\"delta\":{\"reasoning_content\":\"推理过程🌟🔍🚀\"}}]}";
    String contentJson = "{\"choices\":[{\"delta\":{\"content\":\"响应内容\"}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(thinkingJson, contentJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      when(objectMapper.readTree(thinkingJson)).thenReturn(createMockJsonNode("", "推理过程🌟🔍🚀"));
      when(objectMapper.readTree(contentJson)).thenReturn(createMockJsonNode("响应内容", ""));
    } catch (Exception e) {
      // Handle checked exception
    }
    
    // Mock MessageService
    when(messageService.saveAiMessageAsync(conversationId, "响应内容", "推理过程🌟🔍🚀"))
        .thenReturn(Mono.just(SseEventResponse.end(null)));

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "thinking".equals(event.getType()) && 
                          "推理过程🌟🔍🚀".equals(((SseEventResponse.ChunkData) event.getData()).getContent()))
        .expectNextMatches(event -> "chunk".equals(event.getType()) && 
                          "响应内容".equals(((SseEventResponse.ChunkData) event.getData()).getContent()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
  }

  @Test
  void shouldHandleEmptyDeltaNode() {
    // Given - 测试空的delta节点
    String prompt = "Empty delta test";
    Long conversationId = 1L;
    boolean deepThinking = false;
    
    String emptyDeltaJson = "{\"choices\":[{\"delta\":{}}]}";
    
    // Mock WebClient chain
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.accept(any(MediaType.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just(emptyDeltaJson, "[DONE]"));
    
    // Mock ObjectMapper
    try {
      com.fasterxml.jackson.databind.node.ObjectNode rootNode = new ObjectMapper().createObjectNode();
      com.fasterxml.jackson.databind.node.ArrayNode choices = rootNode.putArray("choices");
      com.fasterxml.jackson.databind.node.ObjectNode choice = choices.addObject();
      choice.putObject("delta"); // 空的delta对象
      when(objectMapper.readTree(emptyDeltaJson)).thenReturn(rootNode);
    } catch (Exception e) {
      // Handle checked exception
    }

    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking))
        .expectNextMatches(event -> "start".equals(event.getType()))
        .expectNextMatches(event -> "end".equals(event.getType()))
        .verifyComplete();
        
    // 验证没有调用保存方法
    verify(messageService, never()).saveAiMessageAsync(any(), any(), any());
  }

  /**
   * 创建模拟的JsonNode
   */
  private com.fasterxml.jackson.databind.JsonNode createMockJsonNode(String content, String reasoningContent) {
    com.fasterxml.jackson.databind.node.ObjectNode root = new ObjectMapper().createObjectNode();
    com.fasterxml.jackson.databind.node.ArrayNode choices = root.putArray("choices");
    com.fasterxml.jackson.databind.node.ObjectNode choice = choices.addObject();
    com.fasterxml.jackson.databind.node.ObjectNode delta = choice.putObject("delta");
    
    if (!content.isEmpty()) {
      delta.put("content", content);
    }
    if (!reasoningContent.isEmpty()) {
      delta.put("reasoning_content", reasoningContent);
    }
    
    return root;
  }
  
  /**
   * 创建包含null content的模拟JsonNode来测试null data分支
   */
  private com.fasterxml.jackson.databind.JsonNode createMockJsonNodeWithNullContent() {
    com.fasterxml.jackson.databind.node.ObjectNode root = new ObjectMapper().createObjectNode();
    com.fasterxml.jackson.databind.node.ArrayNode choices = root.putArray("choices");
    com.fasterxml.jackson.databind.node.ObjectNode choice = choices.addObject();
    com.fasterxml.jackson.databind.node.ObjectNode delta = choice.putObject("delta");
    // 不添加任何内容，让content和reasoning_content都为null/missing
    return root;
  }
}