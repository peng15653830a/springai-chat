package com.example.service.api.impl;

import com.example.config.MultiModelProperties;
import com.example.service.sse.impl.DeepSeekSseParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DeepSeekApiClient测试类
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
class DeepSeekApiClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DeepSeekSseParser sseParser;

    @Mock
    private MultiModelProperties multiModelProperties;

    @Test
    void shouldGetProviderName() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setBaseUrl("https://api.deepseek.com");
        when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of("DeepSeek", providerConfig));
        when(multiModelProperties.getApiKey("DeepSeek")).thenReturn("test-api-key");
        
        // 模拟WebClient.Builder的链式调用
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);

        // When
        String providerName = apiClient.getProviderName();

        // Then
        assertThat(providerName).isEqualTo("DeepSeek");
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);

        // When
        boolean available = apiClient.isAvailable();

        // Then
        assertThat(available).isTrue();
    }

    @Test
    void shouldCheckAvailabilityWithNullProviderConfig() {
        // Given
        when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of());

        // When & Then
        assertThatThrownBy(() -> new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("DeepSeek配置未找到");
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);

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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);

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
        when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of("DeepSeek", providerConfig));
        when(multiModelProperties.getApiKey("DeepSeek")).thenReturn(null);

        // 模拟WebClient.Builder的链式调用
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);

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
        when(multiModelProperties.getApiKey("DeepSeek")).thenReturn("test-api-key");
        
        // 模拟WebClient.Builder的链式调用
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);

        // When
        String apiEndpoint = apiClient.getApiEndpoint();

        // Then
        assertThat(apiEndpoint).isEqualTo("https://api.deepseek.com/v1/chat/completions");
    }

    @Test
    void shouldThrowExceptionWhenProviderConfigNotFound() {
        // Given
        when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of());

        // When & Then
        assertThatThrownBy(() -> new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("DeepSeek配置未找到");
    }

    @Test
    void shouldChatCompletionStream() {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, maxTokens, enableThinking))
                .expectNext(chatResponse)
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
        String modelName = "deepseek-chat";
        Double temperature = 0.7;
        Integer maxTokens = 2048;
        Boolean enableThinking = false;
        
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
        
        // Mock SSE parser
        when(sseParser.parseStream(any())).thenReturn(Flux.error(new RuntimeException("Parse Error")));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, maxTokens, enableThinking))
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
        String modelName = "invalid-model";
        Double temperature = 0.7;
        Integer maxTokens = 2048;
        Boolean enableThinking = false;
        
        // Mock WebClient to throw exception
        when(webClient.post()).thenThrow(new RuntimeException("WebClient Error"));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, maxTokens, enableThinking))
                .expectError(RuntimeException.class)
                .verify();
    }

    // ========================= 新增的测试用例 =========================

    @Test
    void shouldHandleSpecialCharactersInModelName() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setBaseUrl("https://api.deepseek.com");
        
        // 添加模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("deepseek-chat🌟");
        modelConfig.setEnabled(true);
        modelConfig.setTemperature(new java.math.BigDecimal("0.7"));
        modelConfig.setMaxTokens(2048);
        providerConfig.setModels(List.of(modelConfig));
        
        when(multiModelProperties.getProviders()).thenReturn(
            java.util.Map.of("DeepSeek", providerConfig));
        when(multiModelProperties.getApiKey("DeepSeek")).thenReturn("test-api-key");
        
        // 模拟WebClient.Builder的链式调用
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
        String modelName = "deepseek-chat🌟";
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, maxTokens, enableThinking))
                .expectNext(chatResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleLongModelName() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setBaseUrl("https://api.deepseek.com");
        
        // 添加模型配置
        StringBuilder longModelName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelNameValue, temperature, maxTokens, enableThinking))
                .expectNext(chatResponse)
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(null, modelName, temperature, maxTokens, enableThinking))
                .expectNext(chatResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleEmptyMessages() {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of();
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, maxTokens, enableThinking))
                .expectNext(chatResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleNullTemperature() {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
        String modelName = "deepseek-chat";
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, null, maxTokens, enableThinking))
                .expectNext(chatResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleNullMaxTokens() {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
        String modelName = "deepseek-chat";
        Double temperature = 0.7;
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, null, enableThinking))
                .expectNext(chatResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleNullEnableThinking() {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
        String modelName = "deepseek-chat";
        Double temperature = 0.7;
        Integer maxTokens = 2048;
        
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, maxTokens, null))
                .expectNext(chatResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleUnicodeCharactersInMessages() {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("你好，世界！🌟"));
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, maxTokens, enableThinking))
                .expectNext(chatResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleVeryLongMessageContent() {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longMessage.append("这是很长的消息内容，用来测试处理长文本的能力。");
        }
        String messageContent = longMessage.toString();
        
        List<Message> messages = List.of(new UserMessage(messageContent));
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, maxTokens, enableThinking))
                .expectNext(chatResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleZeroMaxTokens() {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
        String modelName = "deepseek-chat";
        Double temperature = 0.7;
        Integer maxTokens = 0;
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, maxTokens, enableThinking))
                .expectNext(chatResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleNegativeMaxTokens() {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
        String modelName = "deepseek-chat";
        Double temperature = 0.7;
        Integer maxTokens = -1;
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, maxTokens, enableThinking))
                .expectNext(chatResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleVeryHighTemperature() {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
        String modelName = "deepseek-chat";
        Double temperature = 2.0;
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, maxTokens, enableThinking))
                .expectNext(chatResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleVeryLowTemperature() {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
        String modelName = "deepseek-chat";
        Double temperature = 0.0;
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, maxTokens, enableThinking))
                .expectNext(chatResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleNullModelName() {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, null, temperature, maxTokens, enableThinking))
                .expectNext(chatResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleEmptyModelName() {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
        String modelName = "";
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, maxTokens, enableThinking))
                .expectNext(chatResponse)
                .verifyComplete();
    }

    @Test
    void shouldHandleWhitespaceModelName() {
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
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        DeepSeekApiClient apiClient = new DeepSeekApiClient(webClientBuilder, objectMapper, sseParser, multiModelProperties);
        
        List<Message> messages = List.of(new UserMessage("Hello"));
        String modelName = "   ";
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
        when(responseSpec.bodyToFlux(String.class)).thenReturn(Flux.just("data: test"));
        
        // Mock SSE parser
        Generation generation = new Generation(new AssistantMessage("test"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(sseParser.parseStream(any())).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(apiClient.chatCompletionStream(messages, modelName, temperature, maxTokens, enableThinking))
                .expectNext(chatResponse)
                .verifyComplete();
    }
}