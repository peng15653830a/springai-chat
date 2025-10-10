package com.example.streaming;

import com.example.manager.ChatClientManager;
import com.example.stream.springai.ChatClientResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatModuleClientResolver implements ChatClientResolver {
  private final ChatClientManager chatClientManager;
  @Override
  public ChatClient resolve(String provider) { return chatClientManager.getChatClient(provider); }
}

