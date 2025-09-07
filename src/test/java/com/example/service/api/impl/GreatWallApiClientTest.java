package com.example.service.api.impl;

import com.example.config.GreatWallProperties;
import com.example.config.MultiModelProperties;
import com.example.service.sse.impl.GreatWallSseParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * 长城大模型API客户端单元测试
 */
@ExtendWith(MockitoExtension.class)
class GreatWallApiClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private GreatWallSseParser sseParser;

    @Mock
    private MultiModelProperties multiModelProperties;

    @Mock
    private GreatWallProperties greatWallProperties;

    private GreatWallApiClient apiClient;

    @BeforeEach
    void setUp() {
        // 模拟配置
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.greatwall.com");
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        
        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("greatwall-large");
        modelConfig.setApiRunId("test-api-run-id");
        modelConfig.setTpuidPrefix("test-user");
        providerConfig.setModels(List.of(modelConfig));
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn("test-api-key");
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);
    }

    @Test
    void testConstructor() {
        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );

        // 验证结果
        assertThat(apiClient).isNotNull();
        assertThat(apiClient.getProviderName()).isEqualTo("greatwall");
    }

    @Test
    void testConstructorWithMissingProviderConfig() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - 移除提供者配置
        when(multiModelProperties.getProviders()).thenReturn(new HashMap<>());

        // 验证异常
        assertThatThrownBy(() -> new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        )).isInstanceOf(IllegalStateException.class)
          .hasMessage("长城大模型配置未找到");
    }

    @Test
    void testIsAvailableWithValidConfig() {
        // 准备测试数据
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );

        // 执行测试
        boolean available = apiClient.isAvailable();

        // 验证结果
        assertThat(available).isTrue();
    }

    @Test
    void testIsAvailableWithDisabledProvider() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - 禁用提供者
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(false);
        providerConfig.setBaseUrl("https://api.greatwall.com");
        providers.put("greatwall", providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );
        boolean available = apiClient.isAvailable();

        // 验证结果
        assertThat(available).isFalse();
    }

    @Test
    void testIsAvailableWithMissingApiKey() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - 空API密钥
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.greatwall.com");
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        
        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("greatwall-large");
        modelConfig.setApiRunId("test-api-run-id");
        modelConfig.setTpuidPrefix("test-user");
        providerConfig.setModels(List.of(modelConfig));
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn("");
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );
        boolean available = apiClient.isAvailable();

        // 验证结果
        assertThat(available).isFalse();
    }

    @Test
    void testGetApiEndpoint() {
        // 准备测试数据
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );

        // 执行测试
        String endpoint = apiClient.getApiEndpoint();

        // 验证结果
        assertThat(endpoint).isEqualTo("https://api.greatwall.com");
    }

    // ========================= 新增的测试用例 =========================

    @Test
    void testConstructorWithNullProviderConfig() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - null提供者配置
        when(multiModelProperties.getProviders()).thenReturn(null);

        // 验证异常
        assertThatThrownBy(() -> new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        )).isInstanceOf(IllegalStateException.class)
          .hasMessage("长城大模型配置未找到");
    }

    @Test
    void testIsAvailableWithNullApiKey() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - null API密钥
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.greatwall.com");
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        
        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("greatwall-large");
        modelConfig.setApiRunId("test-api-run-id");
        modelConfig.setTpuidPrefix("test-user");
        providerConfig.setModels(List.of(modelConfig));
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn(null);
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );
        boolean available = apiClient.isAvailable();

        // 验证结果
        assertThat(available).isFalse();
    }

    @Test
    void testIsAvailableWithSpecialCharactersInApiKey() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - 特殊字符API密钥
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.greatwall.com");
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        
        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("greatwall-large");
        modelConfig.setApiRunId("test-api-run-id");
        modelConfig.setTpuidPrefix("test-user");
        providerConfig.setModels(List.of(modelConfig));
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn("test-api-key🌟🔍🚀");
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );
        boolean available = apiClient.isAvailable();

        // 验证结果
        assertThat(available).isTrue();
    }

    @Test
    void testIsAvailableWithLongApiKey() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - 长API密钥
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.greatwall.com");
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        
        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("greatwall-large");
        modelConfig.setApiRunId("test-api-run-id");
        modelConfig.setTpuidPrefix("test-user");
        providerConfig.setModels(List.of(modelConfig));
        
        providers.put("greatwall", providerConfig);
        
        StringBuilder longApiKey = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longApiKey.append("long-api-key");
        }
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn(longApiKey.toString());
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );
        boolean available = apiClient.isAvailable();

        // 验证结果
        assertThat(available).isTrue();
    }

    @Test
    void testGetApiEndpointWithSpecialCharactersInBaseUrl() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - 特殊字符基础URL
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.greatwall🌟🔍🚀.com");
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        
        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("greatwall-large");
        modelConfig.setApiRunId("test-api-run-id");
        modelConfig.setTpuidPrefix("test-user");
        providerConfig.setModels(List.of(modelConfig));
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn("test-api-key");
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );

        // 执行测试
        String endpoint = apiClient.getApiEndpoint();

        // 验证结果
        assertThat(endpoint).isEqualTo("https://api.greatwall🌟🔍🚀.com");
    }

    @Test
    void testGetApiEndpointWithLongBaseUrl() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - 长基础URL
        StringBuilder longBaseUrl = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longBaseUrl.append("https://api.greatwall-long-url");
        }
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl(longBaseUrl.toString());
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        
        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("greatwall-large");
        modelConfig.setApiRunId("test-api-run-id");
        modelConfig.setTpuidPrefix("test-user");
        providerConfig.setModels(List.of(modelConfig));
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn("test-api-key");
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );

        // 执行测试
        String endpoint = apiClient.getApiEndpoint();

        // 验证结果
        assertThat(endpoint).isEqualTo(longBaseUrl.toString());
    }

    @Test
    void testGetApiEndpointWithNullBaseUrl() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - null基础URL
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl(null);
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        
        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("greatwall-large");
        modelConfig.setApiRunId("test-api-run-id");
        modelConfig.setTpuidPrefix("test-user");
        providerConfig.setModels(List.of(modelConfig));
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn("test-api-key");
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );

        // 执行测试
        String endpoint = apiClient.getApiEndpoint();

        // 验证结果
        assertThat(endpoint).isNull();
    }

    @Test
    void testGetApiEndpointWithEmptyBaseUrl() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - 空基础URL
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("");
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        
        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("greatwall-large");
        modelConfig.setApiRunId("test-api-run-id");
        modelConfig.setTpuidPrefix("test-user");
        providerConfig.setModels(List.of(modelConfig));
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn("test-api-key");
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );

        // 执行测试
        String endpoint = apiClient.getApiEndpoint();

        // 验证结果
        assertThat(endpoint).isEqualTo("");
    }

    @Test
    void testIsAvailableWithUnicodeCharactersInProviderName() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - Unicode字符提供者名称
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.greatwall.com");
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        
        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("greatwall-large");
        modelConfig.setApiRunId("test-api-run-id");
        modelConfig.setTpuidPrefix("test-user");
        providerConfig.setModels(List.of(modelConfig));
        
        providers.put("长城大模型测试", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("长城大模型测试")).thenReturn("test-api-key");
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        GreatWallApiClient unicodeApiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );
        boolean available = unicodeApiClient.isAvailable();

        // 验证结果
        assertThat(available).isTrue();
        assertThat(unicodeApiClient.getProviderName()).isEqualTo("长城大模型测试");
    }

    @Test
    void testConstructorWithMultipleModels() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - 多个模型配置
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.greatwall.com");
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        
        // 模拟多个模型配置
        MultiModelProperties.ModelConfig modelConfig1 = new MultiModelProperties.ModelConfig();
        modelConfig1.setName("greatwall-large");
        modelConfig1.setApiRunId("test-api-run-id-1");
        modelConfig1.setTpuidPrefix("test-user-1");
        
        MultiModelProperties.ModelConfig modelConfig2 = new MultiModelProperties.ModelConfig();
        modelConfig2.setName("greatwall-medium");
        modelConfig2.setApiRunId("test-api-run-id-2");
        modelConfig2.setTpuidPrefix("test-user-2");
        
        providerConfig.setModels(List.of(modelConfig1, modelConfig2));
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn("test-api-key");
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );

        // 验证结果
        assertThat(apiClient).isNotNull();
        assertThat(apiClient.getProviderName()).isEqualTo("greatwall");
    }

    @Test
    void testIsAvailableWithZeroTimeouts() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - 零超时设置
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.greatwall.com");
        providerConfig.setConnectTimeoutMs(0);
        providerConfig.setReadTimeoutMs(0);
        
        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("greatwall-large");
        modelConfig.setApiRunId("test-api-run-id");
        modelConfig.setTpuidPrefix("test-user");
        providerConfig.setModels(List.of(modelConfig));
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn("test-api-key");
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );
        boolean available = apiClient.isAvailable();

        // 验证结果
        assertThat(available).isTrue();
    }

    @Test
    void testIsAvailableWithNegativeTimeouts() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - 负超时设置
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.greatwall.com");
        providerConfig.setConnectTimeoutMs(-1);
        providerConfig.setReadTimeoutMs(-1);
        
        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("greatwall-large");
        modelConfig.setApiRunId("test-api-run-id");
        modelConfig.setTpuidPrefix("test-user");
        providerConfig.setModels(List.of(modelConfig));
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn("test-api-key");
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );
        boolean available = apiClient.isAvailable();

        // 验证结果
        assertThat(available).isTrue();
    }

    @Test
    void testConstructorWithNullModelConfig() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - null模型配置
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.greatwall.com");
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        providerConfig.setModels(null); // null模型配置
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn("test-api-key");
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );

        // 验证结果
        assertThat(apiClient).isNotNull();
        assertThat(apiClient.getProviderName()).isEqualTo("greatwall");
    }

    @Test
    void testConstructorWithEmptyModelConfig() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - 空模型配置
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.greatwall.com");
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        providerConfig.setModels(List.of()); // 空模型配置
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn("test-api-key");
        
        // 模拟长城大模型属性
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(false);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );

        // 验证结果
        assertThat(apiClient).isNotNull();
        assertThat(apiClient.getProviderName()).isEqualTo("greatwall");
    }

    @Test
    void testIsAvailableWithSslSkipVerificationTrue() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - SSL跳过验证为true
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.greatwall.com");
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        
        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("greatwall-large");
        modelConfig.setApiRunId("test-api-run-id");
        modelConfig.setTpuidPrefix("test-user");
        providerConfig.setModels(List.of(modelConfig));
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn("test-api-key");
        
        // 模拟长城大模型属性 - SSL跳过验证为true
        GreatWallProperties.Ssl ssl = new GreatWallProperties.Ssl();
        ssl.setSkipVerification(true);
        when(greatWallProperties.getSsl()).thenReturn(ssl);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );
        boolean available = apiClient.isAvailable();

        // 验证结果
        assertThat(available).isTrue();
    }

    @Test
    void testIsAvailableWithNullSslConfig() {
        // 重新设置mock，避免不必要的stubbing
        reset(multiModelProperties, greatWallProperties, webClientBuilder);
        
        // 准备测试数据 - null SSL配置
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("https://api.greatwall.com");
        providerConfig.setConnectTimeoutMs(5000);
        providerConfig.setReadTimeoutMs(30000);
        
        // 模拟模型配置
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("greatwall-large");
        modelConfig.setApiRunId("test-api-run-id");
        modelConfig.setTpuidPrefix("test-user");
        providerConfig.setModels(List.of(modelConfig));
        
        providers.put("greatwall", providerConfig);
        
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey("greatwall")).thenReturn("test-api-key");
        
        // 模拟长城大模型属性 - null SSL配置
        when(greatWallProperties.getSsl()).thenReturn(null);
        
        // 模拟WebClient.Builder
        WebClient mockWebClient = mock(WebClient.class);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // 执行测试
        apiClient = new GreatWallApiClient(
                webClientBuilder,
                objectMapper,
                sseParser,
                multiModelProperties,
                greatWallProperties
        );
        boolean available = apiClient.isAvailable();

        // 验证结果
        assertThat(available).isTrue();
    }
}