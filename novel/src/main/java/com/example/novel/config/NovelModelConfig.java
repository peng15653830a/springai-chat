package com.example.novel.config;

import com.example.config.MultiModelProperties;
import com.example.strategy.model.ModelSelector;
import com.example.strategy.model.SimpleModelSelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Novel模块的模型选择器配置
 * 使用SimpleModelSelector，但默认值来自通用 MultiModelProperties，
 * 从而支持配置一个或多个任意 Provider/Model。
 */
@Configuration
public class NovelModelConfig {

  @Bean
  public ModelSelector modelSelector(MultiModelProperties multiModelProperties) {
    String defaultProvider = multiModelProperties.getDefaultProvider();
    String defaultModel = multiModelProperties.getDefaultModel();
    return new SimpleModelSelector(defaultProvider, defaultModel);
  }
}
