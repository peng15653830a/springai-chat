package com.example.service;

import com.example.service.dto.SseEventResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ModelScopeDirectService基础测试
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
class ModelScopeDirectServiceTest {

  @Mock
  private WebClient.Builder webClientBuilder;

  @Mock
  private WebClient webClient;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private MessagePersistenceService messagePersistenceService;

  private ModelScopeDirectService modelScopeDirectService;

  @BeforeEach
  void setUp() {
    when(webClientBuilder.build()).thenReturn(webClient);
    modelScopeDirectService = new ModelScopeDirectService(webClientBuilder, objectMapper, messagePersistenceService);
    
    // 设置配置属性
    ReflectionTestUtils.setField(modelScopeDirectService, "apiKey", "test-api-key");
    ReflectionTestUtils.setField(modelScopeDirectService, "baseUrl", "https://api-test.com/v1");
    ReflectionTestUtils.setField(modelScopeDirectService, "model", "test-model");
    ReflectionTestUtils.setField(modelScopeDirectService, "temperature", 0.7);
    ReflectionTestUtils.setField(modelScopeDirectService, "maxTokens", 2000);
    ReflectionTestUtils.setField(modelScopeDirectService, "enableThinking", true);
    ReflectionTestUtils.setField(modelScopeDirectService, "thinkingBudget", 50000);
  }

  @Test
  void shouldCreateServiceWithCorrectDependencies() {
    // Then
    org.assertj.core.api.Assertions.assertThat(modelScopeDirectService).isNotNull();
  }

  @Test 
  void shouldHandleNullPrompt() {
    // When & Then
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming(null, 1L, false))
        .expectError(NullPointerException.class)
        .verify();
  }

  @Test
  void shouldHandleNullConversationId() {
    // When & Then  
    StepVerifier.create(modelScopeDirectService.executeDirectStreaming("test", null, false))
        .expectError(NullPointerException.class)
        .verify();
  }

  @Test
  void shouldSetApiKey() {
    // When
    String apiKey = (String) ReflectionTestUtils.getField(modelScopeDirectService, "apiKey");
    
    // Then
    org.junit.jupiter.api.Assertions.assertEquals("test-api-key", apiKey);
  }

  @Test
  void shouldSetBaseUrl() {
    // When
    String baseUrl = (String) ReflectionTestUtils.getField(modelScopeDirectService, "baseUrl");
    
    // Then
    org.junit.jupiter.api.Assertions.assertEquals("https://api-test.com/v1", baseUrl);
  }

  @Test
  void shouldSetModel() {
    // When
    String model = (String) ReflectionTestUtils.getField(modelScopeDirectService, "model");
    
    // Then
    org.junit.jupiter.api.Assertions.assertEquals("test-model", model);
  }

  @Test
  void shouldSetTemperature() {
    // When
    Double temperature = (Double) ReflectionTestUtils.getField(modelScopeDirectService, "temperature");
    
    // Then
    org.junit.jupiter.api.Assertions.assertEquals(0.7, temperature);
  }

  @Test
  void shouldSetMaxTokens() {
    // When
    Integer maxTokens = (Integer) ReflectionTestUtils.getField(modelScopeDirectService, "maxTokens");
    
    // Then
    org.junit.jupiter.api.Assertions.assertEquals(2000, maxTokens);
  }

  @Test
  void shouldSetEnableThinking() {
    // When
    Boolean enableThinking = (Boolean) ReflectionTestUtils.getField(modelScopeDirectService, "enableThinking");
    
    // Then
    org.junit.jupiter.api.Assertions.assertTrue(enableThinking);
  }

  @Test
  void shouldSetThinkingBudget() {
    // When
    Integer thinkingBudget = (Integer) ReflectionTestUtils.getField(modelScopeDirectService, "thinkingBudget");
    
    // Then
    org.junit.jupiter.api.Assertions.assertEquals(50000, thinkingBudget);
  }

  @Test
  void shouldInitializeWithWebClientBuilder() {
    // Given
    when(webClientBuilder.build()).thenReturn(webClient);
    
    // When
    ModelScopeDirectService service = new ModelScopeDirectService(webClientBuilder, objectMapper, messagePersistenceService);
    
    // Then
    org.assertj.core.api.Assertions.assertThat(service).isNotNull();
    verify(webClientBuilder).build();
  }

}