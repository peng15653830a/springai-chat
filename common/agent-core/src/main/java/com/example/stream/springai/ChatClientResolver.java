package com.example.stream.springai;

import org.springframework.ai.chat.client.ChatClient;

/**
 * 解析并提供指定 provider 的 ChatClient。
 */
public interface ChatClientResolver {
  ChatClient resolve(String provider);
}

