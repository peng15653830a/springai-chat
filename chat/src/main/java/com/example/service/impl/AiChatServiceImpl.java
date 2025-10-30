package com.example.service.impl;

import com.example.config.ChatStreamingProperties;
import com.example.dto.request.StreamChatRequest;
import com.example.dto.stream.ChatEvent;
import com.example.handler.ChatErrorHandler;
import com.example.service.AiChatService;
import com.example.service.ConversationService;
import com.example.service.MessageService;
import com.example.service.SseEventPublisher;
import com.example.stream.springai.SpringAiTextStreamClient;
import com.example.strategy.model.ModelSelector;
import com.example.strategy.prompt.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

  private final ChatStreamingProperties streamingProperties;
  private final ConversationService conversationService;
  private final MessageService messageService;
  private final ModelSelector modelSelector;
  private final PromptBuilder promptBuilder;
  private final ChatErrorHandler errorHandler;
  private final SseEventPublisher sseEventPublisher;
  private final SpringAiTextStreamClient textStreamClient;

  @Override
  public Flux<ChatEvent> streamChat(StreamChatRequest request) {
    log.info(
        "chat start cid={}, len={}, search={}, think={}, user={}, model={}->{}",
        request.getConversationId(),
        request.getMessage() != null ? request.getMessage().length() : 0,
        request.isSearchEnabled(),
        request.isDeepThinking(),
        request.getUserId(),
        request.getProvider(),
        request.getModel());

    var searchEventFlux = sseEventPublisher.registerConversationFlux(request.getConversationId());

    return Flux.merge(
            searchEventFlux,
            Flux.concat(prepareContext(request), processChat(request), finishChat(request)))
        .doFinally(signalType -> sseEventPublisher.removeConversation(request.getConversationId()))
        .onErrorResume(errorHandler::handleChatError);
  }

  private Flux<ChatEvent> prepareContext(StreamChatRequest request) {
    conversationService
        .generateTitleIfNeededAsync(request.getConversationId(), request.getMessage())
        .subscribe();
    return Flux.empty();
  }

  private Flux<ChatEvent> processChat(StreamChatRequest request) {
    String userMessage = request.getMessage();
    return Flux.defer(
        () -> {
          ModelSelector.ModelSelection selected = selectModel(request);
          return messageService
              .saveUserMessageAsync(request.getConversationId(), userMessage)
              .flatMapMany(
                  saved ->
                      buildPrompt(request)
                          .flatMapMany(
                              prompt -> {
                                var req =
                                    com.example.stream.TextStreamRequest.builder()
                                        .provider(selected.providerName())
                                        .model(selected.modelName())
                                        .prompt(prompt)
                                        .conversationId(request.getConversationId())
                                        .userId(request.getUserId())
                                        .deepThinking(request.isDeepThinking())
                                        .searchEnabled(request.isSearchEnabled())
                                        .build();

                                var source = textStreamClient.stream(req);
                                var hot = source.replay().autoConnect(2);

                                return Flux.merge(
                                    hot.map(ChatEvent::chunk),
                                    hot.scanWith(StringBuilder::new, (sb, c) -> sb.append(c))
                                        .takeLast(1)
                                        .flatMap(
                                            sb ->
                                                messageService
                                                    .saveAiMessageAsync(
                                                        request.getConversationId(), sb.toString(), null)));
                              }));
        });
  }

  private Flux<ChatEvent> finishChat(StreamChatRequest request) {
    return Flux.empty();
  }

  private Mono<String> buildPrompt(StreamChatRequest request) {
    return promptBuilder.buildPrompt(
        request.getConversationId(), request.getMessage(), request.isSearchEnabled());
  }

  private ModelSelector.ModelSelection selectModel(StreamChatRequest request) {
    if (request.getUserId() != null) {
      return modelSelector.selectModelForUser(
          request.getUserId(), request.getProvider(), request.getModel());
    } else {
      return new ModelSelector.ModelSelection(
          modelSelector.getActualProviderName(request.getProvider()),
          modelSelector.getActualModelName(
              modelSelector.getActualProviderName(request.getProvider()), request.getModel()));
    }
  }
}
