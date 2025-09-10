package com.example.service.api.impl;

import com.example.config.MultiModelProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.ai.api.impl.DeepSeekChatApi;
import com.example.dto.request.ChatCompletionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DeepSeekChatApi测试类
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
class DeepSeekChatApiTest {

    /**
     * 创建测试用的ChatCompletionRequest
     */
    private ChatCompletionRequest createTestRequest(List<ChatCompletionRequest.ChatMessage> messages,
                                                   String model, Double temperature, Integer maxTokens, Boolean enableThinking) {
        ChatCompletionRequest.ChatCompletionRequestBuilder builder = ChatCompletionRequest.builder()
                .model(model)
                .stream(true);

        if (messages != null) {
            builder.messages(messages);
        }

        if (temperature != null) {
            builder.temperature(temperature);
        }

        if (maxTokens != null) {
            builder.maxTokens(maxTokens);
        }

        if (enableThinking != null) {
            builder.extra(Map.of("enable_thinking", enableThinking));
        }

        return builder.build();
    }

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock(lenient = true)
    private MultiModelProperties multiModelProperties;

    @BeforeEach
    void setUp() {
        // 模拟配置 - 仅设置基本配置，不在全局setUp中进行过度mocking
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.deepseek.com");
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);

        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("deepseek-chat");
        modelConfig.setEnabled(true);
        modelConfig.setTemperature(new java.math.BigDecimal("0.7"));
        modelConfig.setMaxTokens(2048);
        providerConfig.setModels(List.of(modelConfig));

        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("DeepSeek")).thenReturn("test-api-key");

        // 不在全局setUp中mock WebClient，避免UnnecessaryStubbing
        // 各个测试方法有需要时单独mock
    }

    @Test
    void shouldGetProviderName() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setBaseUrl("https://api.deepseek.com");
        lenient().when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of("DeepSeek", providerConfig));
        lenient().when(multiModelProperties.getApiKey("DeepSeek")).thenReturn("test-api-key");

        // 模拟WebClient.Builder的链式调用
        lenient().when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        DeepSeekChatApi apiClient = new DeepSeekChatApi(webClientBuilder, objectMapper, multiModelProperties);

        // When
        String apiEndpoint = apiClient.getApiEndpoint();

        // Then
        assertThat(apiEndpoint).isEqualTo("https://api.deepseek.com");
    }

    @Test
    void shouldCheckAvailabilityWithValidConfiguration() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.deepseek.com");
        when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of("DeepSeek", providerConfig));
        when(multiModelProperties.getApiKey("DeepSeek")).thenReturn("test-api-key");

        // 模拟WebClient.Builder的链式调用
        lenient().when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        DeepSeekChatApi apiClient = new DeepSeekChatApi(webClientBuilder, objectMapper, multiModelProperties);

        // When
        boolean available = apiClient.isAvailable();

        // Then
        assertThat(available).isTrue();
    }

    @Test
    void shouldCheckAvailabilityWithNullProviderConfig() {
        // Given
        lenient().when(multiModelProperties.getProviders()).thenReturn(null);

        // 模拟WebClient.Builder的链式调用
        lenient().when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        DeepSeekChatApi apiClient = new DeepSeekChatApi(webClientBuilder, objectMapper, multiModelProperties);
        
        // When
        boolean available = apiClient.isAvailable();

        // Then
        assertThat(available).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenProviderConfigNotFound() {
        // Given
        lenient().when(multiModelProperties.getProviders()).thenReturn(new HashMap<>());
        lenient().when(multiModelProperties.getApiKey("DeepSeek")).thenReturn("test-api-key");

        // 模拟WebClient.Builder的链式调用
        WebClient mockWebClient = mock(WebClient.class);
        lenient().when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(mockWebClient);

        DeepSeekChatApi apiClient = new DeepSeekChatApi(webClientBuilder, objectMapper, multiModelProperties);

        // When
        boolean available = apiClient.isAvailable();

        // Then
        assertThat(available).isFalse();
    }

    @Test
    void shouldCheckAvailabilityWithDisabledProvider() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(false);
        providerConfig.setBaseUrl("https://api.deepseek.com");
        when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of("DeepSeek", providerConfig));
        when(multiModelProperties.getApiKey("DeepSeek")).thenReturn("test-api-key");

        // 模拟WebClient.Builder的链式调用
        lenient().when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        DeepSeekChatApi apiClient = new DeepSeekChatApi(webClientBuilder, objectMapper, multiModelProperties);

        // When
        boolean available = apiClient.isAvailable();

        // Then
        assertThat(available).isFalse();
    }

    @Test
    void shouldCheckAvailabilityWithEmptyApiKey() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.deepseek.com");
        when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of("DeepSeek", providerConfig));
        when(multiModelProperties.getApiKey("DeepSeek")).thenReturn("");

        // 模拟WebClient.Builder的链式调用
        lenient().when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        DeepSeekChatApi apiClient = new DeepSeekChatApi(webClientBuilder, objectMapper, multiModelProperties);

        // When
        boolean available = apiClient.isAvailable();

        // Then
        assertThat(available).isFalse();
    }

    @Test
    void shouldCheckAvailabilityWithNullApiKey() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.deepseek.com");
        lenient().when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of("DeepSeek", providerConfig));
        lenient().when(multiModelProperties.getApiKey("DeepSeek")).thenReturn(null);

        // 模拟WebClient.Builder的链式调用
        WebClient mockWebClient = mock(WebClient.class);
        lenient().when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(mockWebClient);

        DeepSeekChatApi apiClient = new DeepSeekChatApi(webClientBuilder, objectMapper, multiModelProperties);

        // When
        boolean available = apiClient.isAvailable();

        // Then
        assertThat(available).isFalse();
    }

    @Test
    void shouldGetApiEndpoint() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setBaseUrl("https://api.deepseek.com");
        when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of("DeepSeek", providerConfig));

        // 模拟WebClient.Builder的链式调用
        lenient().when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        DeepSeekChatApi apiClient = new DeepSeekChatApi(webClientBuilder, objectMapper, multiModelProperties);

        // When
        String apiEndpoint = apiClient.getApiEndpoint();

        // Then
        assertThat(apiEndpoint).isEqualTo("https://api.deepseek.com");
    }

    @Test
    void shouldExecuteChatCompletionStreamSuccessfully() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setBaseUrl("https://api.deepseek.com");

        // 添加模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("deepseek-chat");
        modelConfig.setEnabled(true);
        modelConfig.setTemperature(new java.math.BigDecimal("0.7"));
        modelConfig.setMaxTokens(2048);
        providerConfig.setModels(List.of(modelConfig));

        when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of("DeepSeek", providerConfig));
        when(multiModelProperties.getApiKey("DeepSeek")).thenReturn("test-api-key");

        // 模拟WebClient.Builder的链式调用
        lenient().when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        DeepSeekChatApi apiClient = new DeepSeekChatApi(webClientBuilder, objectMapper, multiModelProperties);

        // 使用新的ChatCompletionRequest构建器
        List<ChatCompletionRequest.ChatMessage> messages = List.of(
            ChatCompletionRequest.ChatMessage.builder()
                .role("user")
                .content("Hello")
                .build()
        );

        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model("deepseek-chat")
            .messages(messages)
            .temperature(0.7)
            .maxTokens(2048)
            .stream(true)
            .extra(java.util.Map.of("enable_thinking", false))
            .build();

        // Mock WebClient chain
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("{\"id\":\"test\",\"object\":\"chat.completion.chunk\",\"created\":1700000000,\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"test response\"}}]}", "[DONE]"));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(request))
                .expectNextCount(1) // 期望收到1个响应
                .verifyComplete();
    }

    @Test
    void shouldHandleChatCompletionStreamError() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setBaseUrl("https://api.deepseek.com");

        // 添加模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("deepseek-chat");
        modelConfig.setEnabled(true);
        modelConfig.setTemperature(new java.math.BigDecimal("0.7"));
        modelConfig.setMaxTokens(2048);
        providerConfig.setModels(List.of(modelConfig));

        when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of("DeepSeek", providerConfig));
        when(multiModelProperties.getApiKey("DeepSeek")).thenReturn("test-api-key");

        // 模拟WebClient.Builder的链式调用
        lenient().when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        DeepSeekChatApi apiClient = new DeepSeekChatApi(webClientBuilder, objectMapper, multiModelProperties);

        List<ChatCompletionRequest.ChatMessage> messages = List.of(
            ChatCompletionRequest.ChatMessage.builder()
                .role("user")
                .content("Hello")
                .build()
        );

        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model("deepseek-chat")
            .messages(messages)
            .temperature(0.7)
            .maxTokens(2048)
            .stream(true)
            .extra(java.util.Map.of("enable_thinking", false))
            .build();

        // Mock WebClient chain to throw error
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.error(new RuntimeException("API Error")));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(request))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void shouldHandleChatCompletionStreamException() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setBaseUrl("https://api.deepseek.com");

        // 添加模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("invalid-model");
        modelConfig.setEnabled(true);
        modelConfig.setTemperature(new java.math.BigDecimal("0.7"));
        modelConfig.setMaxTokens(2048);
        providerConfig.setModels(List.of(modelConfig));

        when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of("DeepSeek", providerConfig));
        when(multiModelProperties.getApiKey("DeepSeek")).thenReturn("test-api-key");

        // 模拟WebClient.Builder的链式调用
        lenient().when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        DeepSeekChatApi apiClient = new DeepSeekChatApi(webClientBuilder, objectMapper, multiModelProperties);

        List<ChatCompletionRequest.ChatMessage> messages = List.of(
            ChatCompletionRequest.ChatMessage.builder()
                .role("user")
                .content("Hello")
                .build()
        );

        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model("invalid-model")
            .messages(messages)
            .temperature(0.7)
            .maxTokens(2048)
            .stream(true)
            .extra(java.util.Map.of("enable_thinking", false))
            .build();

        // Mock WebClient to throw exception
        when(webClient.post()).thenThrow(new RuntimeException("WebClient Error"));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(request))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void shouldHandleLongModelName() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setBaseUrl("https://api.deepseek.com");

        // 添加模型配置
        StringBuilder longModelName = new StringBuilder();
        for (int i = 0; i < 100; i++) { // 减少长度以避免问题
            longModelName.append("long-model-name");
        }
        String modelNameValue = longModelName.toString();

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelNameValue);
        modelConfig.setEnabled(true);
        modelConfig.setTemperature(new java.math.BigDecimal("0.7"));
        modelConfig.setMaxTokens(2048);
        providerConfig.setModels(List.of(modelConfig));

        when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of("DeepSeek", providerConfig));
        when(multiModelProperties.getApiKey("DeepSeek")).thenReturn("test-api-key");

        // 模拟WebClient.Builder的链式调用
        lenient().when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        DeepSeekChatApi apiClient = new DeepSeekChatApi(webClientBuilder, objectMapper, multiModelProperties);

        List<ChatCompletionRequest.ChatMessage> messages = List.of(ChatCompletionRequest.ChatMessage.builder().role("user").content("Hello").build());
        Double temperature = 0.7;
        Integer maxTokens = 2048;
        Boolean enableThinking = false;

        // Mock WebClient chain
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("{\"id\":\"test\",\"object\":\"chat.completion.chunk\",\"created\":1700000000,\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"test response\"}}]}", "[DONE]"));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(createTestRequest(messages, modelNameValue, temperature, maxTokens, enableThinking)))
                .expectNextCount(1) // 期望收到1个响应
                .verifyComplete();
    }

    @Test
    void shouldHandleNullMessages() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setBaseUrl("https://api.deepseek.com");

        // 添加模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("deepseek-chat");
        modelConfig.setEnabled(true);
        modelConfig.setTemperature(new java.math.BigDecimal("0.7"));
        modelConfig.setMaxTokens(2048);
        providerConfig.setModels(List.of(modelConfig));

        when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of("DeepSeek", providerConfig));
        when(multiModelProperties.getApiKey("DeepSeek")).thenReturn("test-api-key");

        // 模拟WebClient.Builder的链式调用
        lenient().when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        DeepSeekChatApi apiClient = new DeepSeekChatApi(webClientBuilder, objectMapper, multiModelProperties);

        String modelName = "deepseek-chat";
        Double temperature = 0.7;
        Integer maxTokens = 2048;
        Boolean enableThinking = false;

        // Mock WebClient chain
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("{\"id\":\"test\",\"object\":\"chat.completion.chunk\",\"created\":1700000000,\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"test response\"}}]}", "[DONE]"));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(createTestRequest(null, modelName, temperature, maxTokens, enableThinking)))
                .expectNextCount(1) // 期望收到1个响应
                .verifyComplete();
    }
}