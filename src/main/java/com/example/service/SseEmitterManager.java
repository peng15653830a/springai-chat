package com.example.service;

import com.example.service.dto.SseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE Emitter管理器，用于管理与客户端的SSE连接
 *
 * @author xupeng
 */
@Slf4j
@Component
public class SseEmitterManager {

  /** 存储会话ID与SseEmitter的映射关系 */
  private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
  
  /** JSON对象映射器 */
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * 为指定会话创建SSE Emitter
   *
   * @param conversationId 会话ID
   * @return SseEmitter实例
   * @throws IllegalArgumentException 当会话ID无效时抛出
   */
  public SseEmitter createEmitter(Long conversationId) {
    // 验证会话ID
    if (conversationId == null || conversationId <= 0) {
      throw new IllegalArgumentException("会话ID无效");
    }
    
    // 创建SSE Emitter，设置5分钟超时时间给AI回复足够时间
    SseEmitter emitter = new SseEmitter(300000L);

    // 注册完成回调
    emitter.onCompletion(
        () -> {
          emitters.remove(conversationId);
        });

    // 注册超时回调
    emitter.onTimeout(
        () -> {
          emitters.remove(conversationId);
          emitter.complete();
        });

    // 注册错误回调
    emitter.onError(
        (ex) -> {
          emitters.remove(conversationId);
          emitter.completeWithError(ex);
        });

    // 将Emitter存入映射表
    emitters.put(conversationId, emitter);
    return emitter;
  }

  /**
   * 获取指定会话的SSE Emitter
   *
   * @param conversationId 会话ID
   * @return SseEmitter实例，如果不存在则返回null
   */
  public SseEmitter getEmitter(Long conversationId) {
    return emitters.get(conversationId);
  }

  /**
   * 移除并完成指定会话的SSE Emitter
   *
   * @param conversationId 会话ID
   */
  public void removeEmitter(Long conversationId) {
    SseEmitter emitter = emitters.remove(conversationId);
    if (emitter != null) {
      emitter.complete();
    }
  }

  /**
   * 向指定会话的客户端发送标准SSE事件
   *
   * @param conversationId 会话ID
   * @param sseEvent SSE事件对象
   */
  public void sendEvent(Long conversationId, SseEvent sseEvent) {
    SseEmitter emitter = emitters.get(conversationId);
    if (emitter != null) {
      try {
        // 将SseEvent序列化为JSON字符串，作为标准SSE的data字段
        String jsonData = objectMapper.writeValueAsString(sseEvent);
        
        // 发送标准SSE格式：只使用data字段，不使用自定义事件名
        emitter.send(SseEmitter.event().data(jsonData));
        
        log.debug("发送SSE事件成功，会话ID: {}, 事件类型: {}", conversationId, sseEvent.getType());

      } catch (Exception e) {
        log.error("发送SSE事件失败，会话ID: {}, 事件类型: {}, 错误: {}", 
                  conversationId, sseEvent.getType(), e.getMessage(), e);
        emitters.remove(conversationId);
        try {
          emitter.completeWithError(e);
        } catch (Exception completeError) {
          log.warn("完成SSE连接时发生异常: {}", completeError.getMessage());
        }
      }
    }
  }

  /**
   * 向指定会话的客户端发送消息 (保持向后兼容)
   * @deprecated 请使用 sendEvent(Long, SseEvent) 方法
   */
  @Deprecated
  public void sendMessage(Long conversationId, String eventName, Object data) {
    // 为了向后兼容，将旧格式转换为新格式
    SseEvent event = new SseEvent(eventName, data);
    sendEvent(conversationId, event);
  }

  /**
   * 检查指定会话是否存在SSE Emitter
   *
   * @param conversationId 会话ID
   * @return 如果存在返回true，否则返回false
   */
  public boolean hasEmitter(Long conversationId) {
    return emitters.containsKey(conversationId);
  }
}
