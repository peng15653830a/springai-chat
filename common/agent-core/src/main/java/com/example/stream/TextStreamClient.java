package com.example.stream;

import reactor.core.publisher.Flux;

/**
 * 抽象的大模型文本流客户端。
 * 输入 TextStreamRequest，输出逐段文本流。
 */
public interface TextStreamClient {
  Flux<String> stream(TextStreamRequest request);
}

