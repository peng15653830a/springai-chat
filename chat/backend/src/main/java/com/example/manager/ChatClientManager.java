package com.example.manager;

import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import com.example.tool.WebSearchTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ChatClient统一管理器 - 使用Spring AI ChatClient.Builder模式
 * 
 * @author xupeng
 */
@Slf4j
@Component
public class ChatClientManager {

    @Autowired
    private Map<String, ChatModel> chatModels;
    
    @Autowired
    private MultiModelProperties properties;
    
    @Autowired
    private WebSearchTool webSearchTool;

    @Autowired(required = false)
    private com.example.advisor.SimplifiedMessageHistoryAdvisor simplifiedMessageHistoryAdvisor;

    @Autowired
    private org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor messageChatMemoryAdvisor;
    
    private final Map<String, ChatClient> clientCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initialize() {
        log.info("🚀 ChatClientManager初始化完成，发现ChatModel: {}", chatModels.keySet());
        log.info("🔧 WebSearchTool注入状态: {}", webSearchTool != null ? "成功" : "失败");
        log.info("🔧 SimplifiedMessageHistoryAdvisor注入状态: {}", simplifiedMessageHistoryAdvisor != null ? "成功" : "失败");
        log.info("🔧 MessageChatMemoryAdvisor注入状态: {}", messageChatMemoryAdvisor != null ? "成功" : "失败");
        if (webSearchTool != null) {
            log.info("🔧 WebSearchTool类型: {}", webSearchTool.getClass().getName());
        }
    }

    /**
     * 根据提供者名称获取ChatClient，使用Spring AI Builder模式
     */
    public ChatClient getChatClient(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider名称不能为空");
        }

        return clientCache.computeIfAbsent(provider, this::createChatClient);
    }
    
    /**
     * 创建ChatClient实例，使用Function方式注册Tool
     */
    private ChatClient createChatClient(String provider) {
        String modelBeanName = provider.toLowerCase() + "ChatModel";
        ChatModel chatModel = chatModels.get(modelBeanName);
        
        if (chatModel == null) {
            chatModel = chatModels.values().stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到可用的ChatModel: " + provider));
            
            log.warn("⚠️ 未找到精确匹配的ChatModel: {}，使用默认模型", modelBeanName);
        }
        
        log.info("🔧 开始为 {} 创建ChatClient", provider);
        log.info("🔧 WebSearchTool可用性: {}", webSearchTool != null);
        
        if (webSearchTool == null) {
            log.error("❌ WebSearchTool为null，无法配置Tool Calling功能");
        } else {
            log.info("✅ WebSearchTool已注入，准备配置defaultTools");
        }
        
        ChatClient client = ChatClient.builder(chatModel)
                .defaultSystem("""
                    你是一个智能AI助手，具有以下能力：

                    🔍 搜索能力：当用户询问需要实时信息的问题时，你会自动调用搜索功能获取最新信息
                    💭 理解能力：能够理解复杂的问题并提供准确的回答
                    🌐 多语言：支持中英文对话

                    回答风格：
                    - 准确、有用、友好
                    - 当需要最新信息时主动搜索
                    - 适当时引用信息来源

                    你有以下工具可用：
                    - 搜索工具：当用户询问需要实时数据、新闻、天气等信息时使用
                    """)
                .defaultTools(webSearchTool)
                .defaultAdvisors(messageChatMemoryAdvisor,
                        simplifiedMessageHistoryAdvisor)
                .build();
                
        log.info("✅ ChatClient创建完成，provider: {}", provider);
        return client;
    }

    /**
     * 检查提供者是否可用
     */
    public boolean isAvailable(String provider) {
        try {
            ChatClient client = getChatClient(provider);
            return client != null;
        } catch (Exception e) {
            log.debug("检查提供者 {} 可用性失败: {}", provider, e.getMessage());
            return false;
        }
    }

    /**
     * 获取指定提供者的所有模型信息
     */
    public List<ModelInfo> getModels(String provider) {
        MultiModelProperties.ProviderConfig config = properties.getProviders().get(provider);
        
        if (config == null || !config.isEnabled()) {
            return Collections.emptyList();
        }

        return config.getModels().stream()
                .filter(MultiModelProperties.ModelConfig::isEnabled)
                .map(this::convertToModelInfo)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有可用的提供者名称
     */
    public List<String> getAvailableProviders() {
        return properties.getProviders().entrySet().stream()
                .filter(entry -> entry.getValue().isEnabled())
                .filter(entry -> isAvailable(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取模型信息
     */
    public ModelInfo getModelInfo(String provider, String modelName) {
        MultiModelProperties.ProviderConfig config = properties.getProviders().get(provider);
        
        if (config == null) {
            return null;
        }

        return config.getModels().stream()
                .filter(model -> model.getName().equals(modelName))
                .map(this::convertToModelInfo)
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查模型是否支持思考模式
     */
    public boolean supportsThinking(String provider, String modelName) {
        return getModelConfig(provider, modelName)
                .map(MultiModelProperties.ModelConfig::isSupportsThinking)
                .orElse(false);
    }

    /**
     * 检查模型是否支持流式输出
     */
    public boolean supportsStreaming(String provider, String modelName) {
        return getModelConfig(provider, modelName)
                .map(MultiModelProperties.ModelConfig::isSupportsStreaming)
                .orElse(true);
    }

    /**
     * 将模型配置转换为ModelInfo
     */
    private ModelInfo convertToModelInfo(MultiModelProperties.ModelConfig config) {
        ModelInfo info = new ModelInfo();
        info.setId((long) config.getName().hashCode());
        info.setName(config.getName());
        info.setDisplayName(config.getDisplayName());
        info.setMaxTokens(config.getMaxTokens());
        info.setTemperature(config.getTemperature());
        info.setSupportsThinking(config.isSupportsThinking());
        info.setSupportsStreaming(config.isSupportsStreaming());
        info.setAvailable(config.isEnabled());
        info.setSortOrder(config.getSortOrder());
        return info;
    }

    /**
     * 获取模型配置
     */
    private Optional<MultiModelProperties.ModelConfig> getModelConfig(String provider, String modelName) {
        MultiModelProperties.ProviderConfig providerConfig = properties.getProviders().get(provider);
        
        if (providerConfig == null) {
            return Optional.empty();
        }

        return providerConfig.getModels().stream()
                .filter(model -> model.getName().equals(modelName))
                .findFirst();
    }
}
