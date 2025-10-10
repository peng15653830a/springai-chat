package com.example.stream.springai;

import com.example.stream.TextStreamClient;
import com.example.stream.TextStreamRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 基于 Spring AI ChatClient 的通用文本流客户端。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiTextStreamClient implements TextStreamClient {

  private final ChatClientResolver clientResolver;
  private final ChatOptionsFactory optionsFactory;
  private final ToolsProvider toolsProvider;

  @Override
  public Flux<String> stream(TextStreamRequest request) {
    ChatClient client = clientResolver.resolve(request.getProvider());
    var options = optionsFactory.build(request.getProvider(), request.getModel(), request);

    var promptBuilder =
        client
            .prompt()
            .user(request.getPrompt())
            .advisors(
                adv -> {
                  String cid =
                      request.getConversationId() == null
                          ? null
                          : String.valueOf(request.getConversationId());
                  String messageId =
                      request.getAssistantMessageId() == null
                          ? null
                          : String.valueOf(request.getAssistantMessageId());

                  // 只有非null值才添加到advisor参数中
                  if (cid != null) {
                    adv.param("conversationId", cid)
                       .param("chatMemoryId", cid)
                       .param("memoryId", cid)
                       .param("conversation_id", cid);
                  }
                  if (messageId != null) {
                    adv.param("messageId", messageId);
                  }
                  adv.param("searchEnabled", String.valueOf(request.isSearchEnabled()));
                });

    var promptSpec = options != null ? promptBuilder.options(options) : promptBuilder;

    Object[] tools = null;
    try {
      tools = toolsProvider != null ? toolsProvider.resolveTools(request) : null;
    } catch (Exception e) {
      log.warn("toolsProvider resolve failed: {}", e.getMessage());
    }
    if (tools != null && tools.length > 0) {
      promptSpec = promptSpec.tools(tools);
    }

    return promptSpec
        .stream()
        .chatResponse()
        .mapNotNull(resp -> resp.getResult() != null ? resp.getResult().getOutput() : null)
        .mapNotNull(out -> out.getText())
        .filter(s -> s != null && !s.trim().isEmpty());
  }
}
