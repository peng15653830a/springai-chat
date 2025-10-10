package com.example.stream;

import org.springframework.ai.chat.client.ChatClient;
import java.util.List;

/**
 * 流式客户端管理器接口
 * 定义ChatClient管理的通用行为
 *
 * @author 系统自动整合
 */
public interface ClientManager {

  /**
   * 根据提供者名称获取ChatClient
   */
  ChatClient getChatClient(String provider);

  /**
   * 检查提供者是否可用
   */
  boolean isAvailable(String provider);

  /**
   * 获取所有可用的提供者名称
   */
  List<String> getAvailableProviders();
}