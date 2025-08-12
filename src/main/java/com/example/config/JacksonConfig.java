package com.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson配置类
 *
 * @author xupeng
 */
@Configuration
public class JacksonConfig {

  /** 创建全局ObjectMapper Bean 线程安全，可以在整个应用中共享使用 */
  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();

    // 配置JSON序列化行为
    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    
    // 启用JSR310模块以支持Java 8时间类型
    mapper.findAndRegisterModules();
    
    // 禁用将日期写为时间戳
    mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    // 忽略未知属性（避免反序列化时出错）
    mapper.configure(
        com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // 忽略空值
    mapper.configure(
        com.fasterxml.jackson.databind.SerializationFeature.WRITE_NULL_MAP_VALUES, false);

    return mapper;
  }
}
