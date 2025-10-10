package com.example.stream.springai;

import com.example.stream.TextStreamRequest;

/**
 * 根据 provider/model/请求参数构建 ChatOptions。
 */
public interface ChatOptionsFactory {
  org.springframework.ai.chat.prompt.ChatOptions build(String provider, String model, TextStreamRequest request);
}

