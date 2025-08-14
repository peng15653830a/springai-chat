package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.ConversationRequest;
import com.example.dto.MessageRequest;
import com.example.entity.Conversation;
import com.example.entity.Message;
import com.example.service.ConversationService;
import com.example.service.MessageService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 对话控制器，处理对话相关的HTTP请求
 *
 * @author xupeng
 */
@Slf4j
@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "*")
public class ConversationController {

  @Autowired private ConversationService conversationService;

  @Autowired private MessageService messageService;

  /** 默认对话标题 */
  private static final String DEFAULT_CONVERSATION_TITLE = "新对话";

  /**
   * 获取用户对话列表
   *
   * @param userId 用户ID
   * @return 对话列表的ApiResponse
   */
  @GetMapping
  public ApiResponse<List<Conversation>> getConversations(@RequestParam Long userId) {
    log.debug("获取用户对话列表，用户ID: {}", userId);

    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException("用户ID无效");
    }

    List<Conversation> conversations = conversationService.getUserConversations(userId);
    log.debug("获取对话列表成功，用户ID: {}, 对话数量: {}", userId, conversations.size());
    return ApiResponse.success(conversations);
  }

  /**
   * 创建新对话
   *
   * @param userId 用户ID
   * @param request 对话请求对象，包含对话标题
   * @return 新创建对话的ApiResponse
   */
  @PostMapping
  public ApiResponse<Conversation> createConversation(
      @RequestParam Long userId, @RequestBody ConversationRequest request) {
    log.info("创建新对话，用户ID: {}, 标题: {}", userId, request.getTitle());

    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException("用户ID无效");
    }

    String title = request.getTitle();
    
    // 如果标题为空，使用默认标题"新对话"，后续会在发送第一条消息时自动生成
    if (title == null || title.trim().isEmpty()) {
      title = "新对话";
    } else {
      title = title.trim();
    }

    Conversation conversation = conversationService.createConversation(userId, title);
    log.info("对话创建成功，对话ID: {}", conversation.getId());
    return ApiResponse.success("创建对话成功", conversation);
  }

  /**
   * 获取指定对话的详细信息
   *
   * @param id 对话ID
   * @return 对话详细信息的ApiResponse
   */
  @GetMapping("/{id}")
  public ApiResponse<Conversation> getConversation(@PathVariable Long id) {
    Conversation conversation = conversationService.getConversationById(id);
    return ApiResponse.success(conversation);
  }

  /**
   * 删除指定对话
   *
   * @param id 对话ID
   * @return 删除结果的ApiResponse
   */
  @DeleteMapping("/{id}")
  public ApiResponse<Void> deleteConversation(@PathVariable Long id) {
    log.info("删除对话请求，对话ID: {}", id);

    if (id == null || id <= 0) {
      throw new IllegalArgumentException("对话ID无效");
    }

    conversationService.deleteConversation(id);
    log.info("对话删除成功，对话ID: {}", id);
    return ApiResponse.success("删除成功", null);
  }

  /**
   * 获取指定对话的消息列表
   *
   * @param id 对话ID
   * @return 消息列表的ApiResponse
   */
  @GetMapping("/{id}/messages")
  public ApiResponse<List<Message>> getMessages(@PathVariable Long id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("对话ID无效");
    }

    List<Message> messages = conversationService.getConversationMessages(id);
    return ApiResponse.success(messages);
  }

  /**
   * 在指定对话中发送消息
   *
   * @param id 对话ID
   * @param request 消息请求对象，包含消息内容
   * @return 发送的消息的ApiResponse
   */
  @PostMapping("/{id}/messages")
  public ApiResponse<Message> sendMessage(
      @PathVariable Long id, @RequestBody MessageRequest request) {
    Message userMessage = messageService.saveMessage(id, "user", request.getContent());
    return ApiResponse.success("消息发送成功", userMessage);
  }
}
