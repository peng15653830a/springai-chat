package com.example.service;

import com.example.config.MultiModelProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatModel注册表
 * 统一管理所有的ChatModel Bean，支持根据provider名称获取对应的ChatModel和ChatClient
 * 
 * @author xupeng
 */
@Slf4j
@Component
public class ChatModelRegistry {

    private final ApplicationContext applicationContext;
    private final MultiModelProperties multiModelProperties;
    private final Map<String, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();

    public ChatModelRegistry(ApplicationContext applicationContext,
                           MultiModelProperties multiModelProperties) {
        this.applicationContext = applicationContext;
        this.multiModelProperties = multiModelProperties;
    }

    @PostConstruct
    public void initializeChatModels() {
        log.info("🚀 初始化ChatModel注册表");
        
        // 自动发现所有ChatModel Bean
        Map<String, ChatModel> beans = applicationContext.getBeansOfType(ChatModel.class);
        log.info("发现 {} 个ChatModel Bean: {}", beans.size(), beans.keySet());
        
        for (Map.Entry<String, ChatModel> entry : beans.entrySet()) {
            String beanName = entry.getKey();
            ChatModel chatModel = entry.getValue();
            
            // 根据bean名称推断provider名称
            String providerName = extractProviderName(beanName);
            if (providerName != null) {
                chatModelCache.put(providerName, chatModel);
                log.info("✅ 注册ChatModel: {} -> {}", providerName, beanName);
            }
        }
    }

    /**
     * 获取指定提供者和模型的ChatModel
     */
    public ChatModel getChatModel(String providerName, String modelName) {
        String key = providerName + ":" + modelName;
        
        // 首先尝试从缓存获取
        ChatModel cachedModel = chatModelCache.get(key);
        if (cachedModel != null) {
            return cachedModel;
        }
        
        // 尝试获取provider级别的默认ChatModel
        ChatModel providerModel = chatModelCache.get(providerName.toLowerCase());
        if (providerModel != null) {
            // 缓存具体模型的ChatModel（这里可以考虑为不同模型创建不同配置）
            chatModelCache.put(key, providerModel);
            return providerModel;
        }
        
        log.warn("未找到提供者 {} 的ChatModel", providerName);
        return null;
    }

    /**
     * 获取指定提供者和模型的ChatClient
     */
    public ChatClient getChatClient(String providerName, String modelName) {
        String key = providerName + ":" + modelName;
        return chatClientCache.computeIfAbsent(key, k -> createChatClient(providerName, modelName));
    }

    /**
     * 检查指定提供者的ChatModel是否可用
     */
    public boolean isModelAvailable(String providerName, String modelName) {
        try {
            ChatModel chatModel = getChatModel(providerName, modelName);
            return chatModel != null;
        } catch (Exception e) {
            log.warn("检查模型可用性失败: {}:{} - {}", providerName, modelName, e.getMessage());
            return false;
        }
    }

    /**
     * 获取所有可用的提供者
     */
    public Map<String, ChatModel> getAllAvailableModels() {
        return new ConcurrentHashMap<>(chatModelCache);
    }

    /**
     * 创建ChatClient实例
     */
    private ChatClient createChatClient(String providerName, String modelName) {
        log.info("🏗️ 创建ChatClient实例: {} - {}", providerName, modelName);
        
        ChatModel chatModel = getChatModel(providerName, modelName);
        if (chatModel == null) {
            throw new IllegalArgumentException("ChatModel not available for: " + providerName + ":" + modelName);
        }
        
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个有用的AI助手。")
                .build();
    }

    /**
     * 从bean名称提取provider名称
     */
    private String extractProviderName(String beanName) {
        // 处理标准命名格式：providerChatModel -> provider
        if (beanName.endsWith("ChatModel")) {
            String providerName = beanName.substring(0, beanName.length() - "ChatModel".length());
            return mapProviderName(providerName);
        }
        return null;
    }

    /**
     * 映射provider名称到标准格式
     */
    private String mapProviderName(String providerName) {
        switch (providerName.toLowerCase()) {
            case "deepseek":
                return "DeepSeek";
            case "greatwall":
                return "greatwall";
            case "openai":
                return "openai";
            case "qwen":
                return "qwen";
            case "kimi2":
                return "kimi2";
            default:
                return providerName.toLowerCase();
        }
    }
}