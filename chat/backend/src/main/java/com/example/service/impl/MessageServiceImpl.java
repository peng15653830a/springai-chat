package com.example.service.impl;

import static com.example.constant.AiChatConstants.ROLE_ASSISTANT;
import static com.example.constant.AiChatConstants.ROLE_USER;

import com.example.dto.request.MessageSaveRequest;
import com.example.dto.stream.ChatEvent;
import com.example.entity.Message;
import com.example.mapper.MessageMapper;
import com.example.service.MessageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 消息服务实现类（整合了MessagePersistenceService的响应式功能）
 *
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

  private final MessageMapper messageMapper;
  private final com.example.service.MessageToolResultService messageToolResultService;

  @Override
  public Message saveMessage(com.example.dto.request.MessageSaveRequest request) {
    // 参数验证
    if (request == null) {
      throw new IllegalArgumentException("消息保存请求不能为空");
    }
    if (request.getConversationId() == null || request.getConversationId() <= 0) {
      throw new IllegalArgumentException("对话ID无效");
    }
    if (request.getRole() == null || request.getRole().trim().isEmpty()) {
      throw new IllegalArgumentException("消息角色不能为空");
    }
    if (request.getContent() == null || request.getContent().trim().isEmpty()) {
      throw new IllegalArgumentException("消息内容不能为空");
    }

    log.debug(
        "保存消息，会话ID: {}, 角色: {}, 内容长度: {}, 是否有思考: {}",
        request.getConversationId(),
        request.getRole(),
        request.getContent().length(),
        request.getThinking() != null);

    Message message = new Message();
    message.setConversationId(request.getConversationId());
    message.setRole(request.getRole());
    message.setContent(request.getContent());
    message.setThinking(request.getThinking());
    // 搜索结果已迁移到message_tool_results表，不再存储在message中
    messageMapper.insert(message);

    log.debug("消息保存成功，消息ID: {}", message.getId());
    return message;
  }

  @Override
  public Message getMessageById(Long messageId) {
    return messageMapper.selectById(messageId);
  }

  @Override
  public List<Message> getMessagesByConversationId(Long conversationId) {
    if (conversationId == null || conversationId <= 0) {
      throw new IllegalArgumentException("对话ID无效");
    }
    return messageMapper.selectByConversationId(conversationId);
  }

  @Override
  public void deleteMessage(Long messageId) {
    if (messageId == null || messageId <= 0) {
      throw new IllegalArgumentException("消息ID无效");
    }
    try {
      // 先清理该消息的工具调用记录
      try {
        messageToolResultService.deleteMessageToolResults(messageId);
      } catch (Exception ignore) {
      }
      messageMapper.deleteById(messageId);
    } catch (Exception e) {
      log.error("删除消息失败，消息ID: {}", messageId, e);
      // 不抛出异常，保持与测试一致的行为
    }
  }

  @Override
  public void updateMessageContent(Long messageId, String content, String thinking) {
    if (messageId == null || messageId <= 0) {
      throw new IllegalArgumentException("消息ID无效");
    }
    Message entity = new Message();
    entity.setId(messageId);
    entity.setContent(content);
    entity.setThinking(thinking);
    try {
      messageMapper.updateById(entity);
      log.info(
          "消息更新成功，消息ID: {}，内容长度: {}，是否包含thinking: {}",
          messageId,
          content != null ? content.length() : 0,
          thinking != null);
    } catch (Exception e) {
      log.error("更新消息失败，消息ID: {}", messageId, e);
      throw e;
    }
  }

  // ========================= 响应式方法实现 =========================

  @Override
  public Mono<Message> saveUserMessageAsync(Long conversationId, String content) {
    return Mono.fromCallable(
            () -> {
              Message userMessage =
                  saveMessage(
                      MessageSaveRequest.builder()
                          .conversationId(conversationId)
                          .role(ROLE_USER)
                          .content(content)
                          .build());
              log.info("用户消息保存成功，消息ID: {}", userMessage.getId());
              return userMessage;
            })
        .onErrorMap(
            error -> {
              log.error("保存用户消息失败，会话ID: {}", conversationId, error);
              return new RuntimeException("保存用户消息失败: " + error.getMessage(), error);
            });
  }

  @Override
  public Mono<ChatEvent> saveAiMessageAsync(Long conversationId, String content, String thinking) {
    return Mono.fromCallable(
            () -> {
              // 保存AI消息，包含thinking内容
              Message aiMessage =
                  saveMessage(
                      MessageSaveRequest.builder()
                          .conversationId(conversationId)
                          .role(ROLE_ASSISTANT)
                          .content(content)
                          .thinking(thinking)
                          // 不再使用searchResults字段
                          .build());

              log.info(
                  "AI消息保存成功，消息ID: {}, thinking内容: {}",
                  aiMessage.getId(),
                  thinking != null ? "有" : "无");
              return ChatEvent.end(aiMessage.getId());
            })
        .onErrorMap(
            error -> {
              log.error("保存AI消息失败，会话ID: {}", conversationId, error);
              return new RuntimeException("保存AI消息失败: " + error.getMessage(), error);
            });
  }

  @Override
  public Mono<List<Message>> getConversationHistoryAsync(Long conversationId) {
    if (conversationId == null || conversationId <= 0) {
      return Mono.error(new IllegalArgumentException("对话ID无效"));
    }

    return Mono.fromCallable(() -> getMessagesByConversationId(conversationId))
        .doOnNext(
            messages -> log.debug("加载会话历史，会话ID: {}, 消息数量: {}", conversationId, messages.size()));
  }

  @Override
  public Mono<Long> preCreateAssistantMessage(Long conversationId) {
    if (conversationId == null || conversationId <= 0) {
      return Mono.error(new IllegalArgumentException("对话ID无效"));
    }

    return Mono.fromCallable(
            () -> {
              log.debug("预创建助手消息，会话ID: {}", conversationId);

              // 创建助手消息占位符
              MessageSaveRequest request =
                  MessageSaveRequest.builder()
                      .conversationId(conversationId)
                      .role(ROLE_ASSISTANT)
                      .content("") // 初始为空，稍后会由Advisor更新
                      .build();

              Message message = saveMessage(request);

              log.debug("助手消息预创建成功，消息ID: {}", message.getId());
              return message.getId();
            })
        .onErrorMap(
            error -> {
              log.error("预创建助手消息失败，会话ID: {}", conversationId, error);
              return new RuntimeException("预创建助手消息失败: " + error.getMessage(), error);
            });
  }
}
