package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ConversationTitleService {

  private static final int SHORT_MESSAGE_LENGTH = 20;
  private static final int FIRST_SENTENCE_MAX_LENGTH = 25;
  private static final int TRUNCATED_LENGTH = 20;
  private static final int MIN_TRUNCATE_POSITION = 10;
  private static final int MAX_TRUNCATE_POSITION = 18;
  private static final String SENTENCE_SPLIT_REGEX = "[。！？\n]";
  private static final String DEFAULT_CONVERSATION_TITLE = "新对话";
  private static final String PUNCTUATION_REGEX = ".*[。！？，、；：]$";

  private final ConversationService conversationService;

  public ConversationTitleService(ConversationService conversationService) {
    this.conversationService = conversationService;
  }

  public String generateTitle(String message) {
    if (message == null || message.trim().isEmpty()) {
      return DEFAULT_CONVERSATION_TITLE;
    }

    String cleanMessage = message.trim();

    if (cleanMessage.length() <= SHORT_MESSAGE_LENGTH) {
      return cleanMessage;
    }

    String[] sentences = cleanMessage.split(SENTENCE_SPLIT_REGEX);
    if (sentences.length > 0 && !sentences[0].trim().isEmpty()) {
      String firstSentence = sentences[0].trim();
      if (firstSentence.length() <= FIRST_SENTENCE_MAX_LENGTH) {
        return firstSentence;
      }
    }

    String truncated = cleanMessage.substring(0, TRUNCATED_LENGTH);
    if (!truncated.matches(PUNCTUATION_REGEX)) {
      for (int i = MAX_TRUNCATE_POSITION; i >= MIN_TRUNCATE_POSITION; i--) {
        char c = truncated.charAt(i);
        if (c == '，' || c == '、' || c == '；' || c == '：') {
          return truncated.substring(0, i + 1);
        }
      }
    }
    return truncated + "...";
  }

  public Mono<Void> updateTitleIfNeeded(Long conversationId, String userMessage) {
    return Mono.fromCallable(() -> conversationService.getConversationById(conversationId))
        .filter(this::needsTitle)
        .flatMap(conversation -> generateAndUpdateTitle(conversationId, userMessage))
        .then()
        .doOnSuccess(v -> log.debug("对话标题生成完成，会话ID: {}", conversationId))
        .onErrorResume(
            error -> {
              log.error("生成对话标题时发生异常，会话ID: {}", conversationId, error);
              return Mono.empty();
            });
  }

  private boolean needsTitle(com.example.entity.Conversation conversation) {
    return conversation.getTitle() == null
        || conversation.getTitle().trim().isEmpty()
        || DEFAULT_CONVERSATION_TITLE.equals(conversation.getTitle().trim());
  }

  private Mono<Void> generateAndUpdateTitle(Long conversationId, String userMessage) {
    return Mono.fromCallable(
        () -> {
          String newTitle = generateTitle(userMessage.trim());
          conversationService.updateConversationTitle(conversationId, newTitle);
          log.debug("自动生成对话标题成功，会话ID: {}, 标题: {}", conversationId, newTitle);
          return null;
        });
  }
}
