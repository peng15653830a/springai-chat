package com.example.stream.springai;

import com.example.config.ChatOptionsProperties;
import com.example.config.MultiModelProperties;
import com.example.stream.TextStreamRequest;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.ChatOptions;

/**
 * ChatOptionsFactory的抽象基类
 * 
 * <p>提供通用的ChatOptions构建逻辑：
 * <ul>
 *   <li>从配置中读取默认值（ChatOptionsProperties和MultiModelProperties）</li>
 *   <li>请求参数优先，其次模型配置，最后全局默认值</li>
 *   <li>子类实现具体provider的options构建（如OpenAI、GreatWall等）</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractChatOptionsFactory implements ChatOptionsFactory {

  protected final MultiModelProperties multiModelProperties;
  protected final ChatOptionsProperties chatOptionsProperties;

  @Override
  public ChatOptions build(String provider, String model, TextStreamRequest request) {
    MultiModelProperties.ProviderConfig providerConfig =
        multiModelProperties.getProviders().get(provider);
    MultiModelProperties.ModelConfig modelConfig = findModelConfig(providerConfig, model);

    double temperature = resolveTemperature(request, modelConfig);
    Integer maxTokens = resolveMaxTokens(request, modelConfig);
    Double topP = resolveTopP(request);

    log.debug(
        "构建ChatOptions: provider={}, model={}, temp={}, maxTokens={}, topP={}",
        provider,
        model,
        temperature,
        maxTokens,
        topP);

    return buildProviderSpecificOptions(
        provider, model, temperature, maxTokens, topP, request, modelConfig);
  }

  protected abstract ChatOptions buildProviderSpecificOptions(
      String provider,
      String model,
      double temperature,
      Integer maxTokens,
      Double topP,
      TextStreamRequest request,
      MultiModelProperties.ModelConfig modelConfig);

  protected MultiModelProperties.ModelConfig findModelConfig(
      MultiModelProperties.ProviderConfig providerConfig, String modelName) {
    if (providerConfig == null || providerConfig.getModels() == null) {
      return null;
    }
    return providerConfig.getModels().stream()
        .filter(m -> modelName.equals(m.getName()))
        .findFirst()
        .orElse(null);
  }

  protected double resolveTemperature(
      TextStreamRequest request, MultiModelProperties.ModelConfig modelConfig) {
    if (request.getTemperature() != null) {
      return request.getTemperature();
    }

    if (modelConfig != null && modelConfig.getTemperature() != null) {
      return modelConfig.getTemperature().doubleValue();
    }

    BigDecimal defaultTemp = multiModelProperties.getDefaults().getTemperature();
    return defaultTemp != null ? defaultTemp.doubleValue() : 0.7;
  }

  protected Integer resolveMaxTokens(
      TextStreamRequest request, MultiModelProperties.ModelConfig modelConfig) {
    if (request.getMaxTokens() != null) {
      return request.getMaxTokens();
    }

    if (modelConfig != null && modelConfig.getMaxTokens() != null) {
      return modelConfig.getMaxTokens();
    }

    return multiModelProperties.getDefaults().getMaxTokens();
  }

  protected Double resolveTopP(TextStreamRequest request) {
    if (request.getTopP() != null) {
      return request.getTopP();
    }

    return null;
  }
}
