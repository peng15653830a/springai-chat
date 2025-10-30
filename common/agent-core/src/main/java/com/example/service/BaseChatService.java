package com.example.service;

import com.example.dto.stream.ChatEvent;
import com.example.handler.ChatErrorHandler;
import com.example.stream.TextStreamRequest;
import com.example.stream.springai.SpringAiTextStreamClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public abstract class BaseChatService {

    private final SpringAiTextStreamClient textStreamClient;
    private final ChatErrorHandler errorHandler;

    protected Flux<ChatEvent> performStreamChat(TextStreamRequest request) {
        var source = textStreamClient.stream(request);
        var hot = source.replay().autoConnect(2);

        return Flux.merge(
                hot.map(ChatEvent::chunk),
                hot.scanWith(StringBuilder::new, (sb, c) -> sb.append(c))
                        .takeLast(1)
                        .flatMapMany(
                                content -> handleAssistantMessage(request.getConversationId(), content.toString())))
                .onErrorResume(errorHandler::handleChatError);
    }

    protected abstract Flux<ChatEvent> handleAssistantMessage(String conversationId, String content);
}
