package com.example.config;

import com.example.service.ChatModelRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * 增强版AI配置类
 * 通过@Import导入各个Provider的配置类，实现自动创建ChatModel Bean
 * 使用ChatModelRegistry统一管理所有ChatModel
 *
 * @author xupeng
 */
@Slf4j
@Configuration
@Import({DeepSeekConfig.class, GreatWallConfig.class, OpenAiCompatibleConfig.class})
public class EnhancedAiConfig {

    /**
     * 创建ChatModelRegistry Bean（主要的ChatModel管理器）
     */
    @Bean
    @Primary
    public ChatModelRegistry chatModelRegistry(ApplicationContext applicationContext,
                                              MultiModelProperties multiModelProperties) {
        log.info("🏗️ 创建ChatModelRegistry Bean");
        return new ChatModelRegistry(applicationContext, multiModelProperties);
    }

    // EnhancedChatClientFactory内部类已移除
    // ChatModel的创建逻辑分散到各个Provider的配置类中
    // 使用ChatModelRegistry进行统一管理
}