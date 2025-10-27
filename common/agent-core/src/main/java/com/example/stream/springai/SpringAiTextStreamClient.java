package com.example.stream.springai;

import com.example.stream.TextStreamClient;
import com.example.stream.TextStreamRequest;
import com.example.tool.ToolManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 基于 Spring AI ChatClient 的通用文本流客户端
 * 使用ToolManager动态注入工具，避免不必要的工具注册
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiTextStreamClient implements TextStreamClient {

  private final ChatClientResolver clientResolver;
  private final ChatOptionsFactory optionsFactory;
  
  @Autowired(required = false)
  private ToolManager toolManager;

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

    if (toolManager != null) {
      List<Object> tools = toolManager.resolveTools(request);
      if (!tools.isEmpty()) {
        promptSpec = promptSpec.tools(tools.toArray());
        log.debug("注入 {} 个工具到prompt", tools.size());
      }
    }

    return promptSpec
        .stream()
        .chatResponse()
        .mapNotNull(resp -> resp.getResult() != null ? resp.getResult().getOutput() : null)
        .mapNotNull(out -> out.getText())
        .filter(s -> s != null && !s.trim().isEmpty());
  }
}
