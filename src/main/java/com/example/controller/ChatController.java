package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.MessageRequest;
import com.example.entity.Message;
import com.example.service.AiChatService;
import com.example.service.SseEmitterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天控制器，处理聊天相关的HTTP请求
 *
 * @author xupeng
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

  @Autowired private SseEmitterManager sseEmitterManager;

  @Autowired private AiChatService aiChatService;

  /**
   * 建立SSE连接，用于接收AI聊天响应的流式数据
   *
   * @param conversationId 会话ID
   * @return SseEmitter实例
   */
  @GetMapping(value = "/stream/{conversationId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamChat(@PathVariable Long conversationId) {
    log.info("创建SSE连接，会话ID: {}", conversationId);
    return sseEmitterManager.createEmitter(conversationId);
  }

  /**
   * 发送用户消息到AI聊天服务
   *
   * @param conversationId 会话ID
   * @param request 消息请求对象，包含消息内容和搜索开关
   * @return 包含用户消息的ApiResponse
   */
  @PostMapping("/conversations/{conversationId}/messages")
  public ApiResponse<Message> sendMessage(
      @PathVariable Long conversationId, @RequestBody MessageRequest request) {
    log.info(
        "接收到消息发送请求，会话ID: {}, 消息长度: {}, 搜索开启: {}",
        conversationId,
        request.getContent() != null ? request.getContent().length() : 0,
        request.getSearchEnabled());

    try {
      Message userMessage =
          aiChatService.sendMessage(conversationId, request.getContent(), request.getSearchEnabled());
      log.info("消息发送处理完成，会话ID: {}", conversationId);
      return ApiResponse.success("消息发送成功", userMessage);
    } catch (Exception e) {
      log.error("消息发送失败，会话ID: {}", conversationId, e);
      return ApiResponse.error("消息发送失败: " + e.getMessage());
    }
  }

  /**
   * 测试AI配置是否正常
   *
   * @return 测试结果
   */
  @GetMapping("/test")
  public ApiResponse<String> testAiConfig() {
    log.info("测试AI配置");
    try {
      // 简单的测试调用
      var response = aiChatService.chatWithAi("你好，这是一个测试", java.util.Collections.emptyList());
      return ApiResponse.success("AI配置测试成功", response.getContent());
    } catch (Exception e) {
      log.error("AI配置测试失败", e);
      return ApiResponse.error("AI配置测试失败: " + e.getMessage());
    }
  }

  /**
   * 查看AI配置信息
   *
   * @return 配置信息
   */
  @GetMapping("/config")
  public ApiResponse<java.util.Map<String, Object>> getAiConfig() {
    java.util.Map<String, Object> config = new java.util.HashMap<>();
    
    // 从环境变量和配置文件中获取值
    config.put("api-key", System.getProperty("spring.ai.openai.api-key", "未设置"));
    config.put("base-url", System.getProperty("spring.ai.openai.base-url", "未设置"));
    config.put("model", System.getProperty("spring.ai.openai.chat.options.model", "未设置"));
    
    // 从环境变量获取
    config.put("env-api-key", System.getenv("OPENAI_API_KEY"));
    config.put("env-base-url", System.getenv("OPENAI_BASE_URL"));
    config.put("env-model", System.getenv("OPENAI_MODEL"));
    
    return ApiResponse.success("AI配置信息", config);
  }
}
