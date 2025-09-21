package com.example.manager;

import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import com.example.service.factory.ModelProviderFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ChatClient统一管理器 - 使用Spring AI ChatClient.Builder模式
 *
 * @author xupeng
 */
@Slf4j
@Component
public class ChatClientManager {

  @Autowired private Map<String, ChatModel> chatModels;

  @Autowired private MultiModelProperties properties;

  @Autowired private ModelProviderFactory modelProviderFactory;

  @Autowired
  private org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
      messageChatMemoryAdvisor;

  private final Map<String, ChatClient> clientCache = new ConcurrentHashMap<>();

  @PostConstruct
  public void initialize() {
    log.info("🚀 ChatClientManager初始化完成，发现ChatModel: {}", chatModels.keySet());
    log.info("🔧 MessageChatMemoryAdvisor注入状态: {}", messageChatMemoryAdvisor != null ? "成功" : "失败");
  }

  /** 根据提供者名称获取ChatClient，使用Spring AI Builder模式 */
  public ChatClient getChatClient(String provider) {
    if (provider == null || provider.trim().isEmpty()) {
      throw new IllegalArgumentException("Provider名称不能为空");
    }

    return clientCache.computeIfAbsent(provider, this::createChatClient);
  }

  /** 创建ChatClient实例，工具将按需在具体调用时注入 */
  private ChatClient createChatClient(String provider) {
    ChatModel chatModel = resolveChatModel(provider);

    log.info("🔧 开始为 {} 创建ChatClient", provider);

    ChatClient client =
        ChatClient.builder(chatModel)
            .defaultSystem(
                """
你是一个智能AI助手。请以清晰、可读的 Markdown 作答（无需 HTML）。

原则：
- 开头先给简短的自然段总览，直接进入主题；非必要不使用总标题。
- 需要分结构时，使用二级及以下标题，适度组织，避免过度格式化。
- 列表/表格按常规 Markdown 书写，优先保证可读性与信息准确性。
- 不确定时优先用自然段清晰表述，再视需要添加简单小节或列表。

能力：
- 🔍 需要最新信息时调用搜索工具。
- 💭 准确理解问题并给出有用答案。

风格：准确、有用、友好；必要时在结尾列出参考来源。
                    """)
            .defaultAdvisors(messageChatMemoryAdvisor)
            .build();

    log.info("✅ ChatClient创建完成，provider: {}", provider);
    return client;
  }

  /** 根据 provider 名称解析 ChatModel，使用工厂模式 */
  private ChatModel resolveChatModel(String provider) {
    try {
      return modelProviderFactory.getChatModel(provider);
    } catch (IllegalArgumentException e) {
      log.error("❌ 无法解析ChatModel for provider: {}, 错误: {}", provider, e.getMessage());
      throw e;
    }
  }

  /** 检查提供者是否可用 */
  public boolean isAvailable(String provider) {
    try {
      ChatClient client = getChatClient(provider);
      return client != null;
    } catch (Exception e) {
      log.debug("检查提供者 {} 可用性失败: {}", provider, e.getMessage());
      return false;
    }
  }

  /** 获取指定提供者的所有模型信息 */
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

  /** 获取所有可用的提供者名称 */
  public List<String> getAvailableProviders() {
    return properties.getProviders().entrySet().stream()
        .filter(entry -> entry.getValue().isEnabled())
        .filter(entry -> isAvailable(entry.getKey()))
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }

  /** 获取模型信息 */
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

  /** 检查模型是否支持思考模式 */
  public boolean supportsThinking(String provider, String modelName) {
    return getModelConfig(provider, modelName)
        .map(MultiModelProperties.ModelConfig::isSupportsThinking)
        .orElse(false);
  }

  /** 检查模型是否支持流式输出 */
  public boolean supportsStreaming(String provider, String modelName) {
    return getModelConfig(provider, modelName)
        .map(MultiModelProperties.ModelConfig::isSupportsStreaming)
        .orElse(true);
  }

  /** 将模型配置转换为ModelInfo */
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

  /** 获取模型配置 */
  private Optional<MultiModelProperties.ModelConfig> getModelConfig(
      String provider, String modelName) {
    MultiModelProperties.ProviderConfig providerConfig = properties.getProviders().get(provider);

    if (providerConfig == null) {
      return Optional.empty();
    }

    return providerConfig.getModels().stream()
        .filter(model -> model.getName().equals(modelName))
        .findFirst();
  }
}
