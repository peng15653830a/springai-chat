package com.example.novel.streaming;

import com.example.novel.manager.NovelClientManager;
import com.example.stream.springai.ChatClientResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NovelClientResolver implements ChatClientResolver {
  private final NovelClientManager clientManager;

  @Override
  public ChatClient resolve(String provider) {
    return clientManager.getChatClient(provider);
  }
}
