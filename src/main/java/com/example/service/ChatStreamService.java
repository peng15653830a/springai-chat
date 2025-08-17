package com.example.service;

import com.example.config.ChatStreamingProperties;
import com.example.service.dto.SseEventResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 核心流式聊天服务
 *
 * @author xupeng
 */
@Slf4j
@Service
public class ChatStreamService {

  @Autowired private ChatStreamingProperties streamingProperties;
  @Autowired private ModelScopeDirectService modelScopeDirectService;

  /**
   * 执行流式AI聊天
   *
   * @param prompt 完整的聊天提示
   * @param conversationId 会话ID（用于保存AI响应）
   * @param deepThinking 是否启用深度思考模式
   * @return 响应式SSE事件流
   */
  public Flux<SseEventResponse> executeStreamingChat(String prompt, Long conversationId, boolean deepThinking) {
    log.debug("开始执行流式AI聊天，提示长度: {}, 会话ID: {}, 深度思考: {}", prompt.length(), conversationId, deepThinking);

    // 统一使用ModelScope直接API调用，通过deepThinking参数控制是否启用推理
    log.info("🚀 使用ModelScope直接API调用，深度思考: {}", deepThinking);
    return modelScopeDirectService.executeDirectStreaming(prompt, conversationId, deepThinking)
        .timeout(streamingProperties.getResponseTimeout())
        .onErrorResume(this::handleChatError);
  }



  /**
   * 处理聊天错误
   */
  private Flux<SseEventResponse> handleChatError(Throwable error) {
    log.error("流式聊天发生错误", error);
    
    String errorMessage = getErrorMessage(error);
    return Flux.just(SseEventResponse.error(errorMessage));
  }

  
  /**
   * 获取用户友好的错误信息
   */
  private String getErrorMessage(Throwable error) {
    String message = error.getMessage();
    if (message == null) {
      message = error.getClass().getSimpleName();
    }
    
    if (message.contains("401")) {
      return "API密钥无效，请检查配置";
    } else if (message.contains("429")) {
      return "API调用频率超限，请稍后重试";
    } else if (message.contains("timeout")) {
      return "请求超时，请检查网络连接";
    } else if (message.contains("Connection")) {
      return "网络连接失败，请检查网络";
    }
    
    return "AI服务暂时不可用，请稍后重试";
  }
}