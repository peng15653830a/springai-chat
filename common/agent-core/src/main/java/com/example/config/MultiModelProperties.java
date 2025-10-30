package com.example.config;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.models")
public class MultiModelProperties {

  private boolean enabled = true;
  private String defaultProvider = "deepseek";
  private String defaultModel = "deepseek-chat";
  private GlobalDefaults defaults = new GlobalDefaults();
  private Map<String, ProviderConfig> providers = new HashMap<>();

  @Autowired(required = false)
  private Environment environment;

  @Data
  public static class GlobalDefaults {
    private BigDecimal temperature = BigDecimal.valueOf(0.7);
    private Integer maxTokens = 2000;
    private Integer timeoutMs = 30000;
    private Integer thinkingBudget = 50000;
    private boolean streamEnabled = true;
  }

  @Data
  public static class ProviderConfig {
    private boolean enabled = true;
    private String displayName;
    private String apiKey;
    private String baseUrl;
    private Integer connectTimeoutMs = 10000;
    private Integer readTimeoutMs = 30000;
    private List<ModelConfig> models;
  }

  @Data
  public static class ModelConfig {
    private String name;
    private String displayName;
    private Integer maxTokens;
    private BigDecimal temperature;
    private boolean supportsThinking = false;
    private boolean supportsStreaming = true;
    private boolean supportsTools = false;
    private boolean enabled = true;
    private Integer sortOrder = 0;
    private Integer thinkingBudget;
    private boolean nonStandardApi = false;
    private String apiRunId;
    private String tpuidPrefix = "guest";
  }

  public String getApiKey(String providerName) {
    ProviderConfig provider = providers.get(providerName);
    if (provider == null || provider.getApiKey() == null) {
      return null;
    }
    return provider.getApiKey();
  }

  public boolean isProviderAvailable(String providerName) {
    ProviderConfig provider = providers.get(providerName);
    if (provider == null || !provider.isEnabled()) {
      return false;
    }

    String apiKey = getApiKey(providerName);
    if (apiKey != null && !apiKey.trim().isEmpty()) {
      return true;
    }

    if (environment != null) {
      List<String> profiles = Arrays.asList(environment.getActiveProfiles());
      boolean isDev =
          profiles.contains("dev") || profiles.contains("development") || profiles.isEmpty();
      return isDev;
    }
    return true;
  }

  public java.util.Optional<ModelConfig> getModelConfig(String providerName, String modelName) {
    ProviderConfig provider = providers.get(providerName);
    if (provider == null || provider.getModels() == null) {
      return java.util.Optional.empty();
    }
    return provider.getModels().stream()
        .filter(model -> model.getName().equals(modelName))
        .findFirst();
  }

  public List<ModelConfig> getEnabledModels(String providerName) {
    ProviderConfig provider = providers.get(providerName);
    if (provider == null || !provider.isEnabled() || provider.getModels() == null) {
      return List.of();
    }
    return provider.getModels().stream().filter(ModelConfig::isEnabled).toList();
  }
}

