package com.example.service;

import static com.example.service.constants.AiChatConstants.SSE_EVENT_CHUNK;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
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
   * 向指定会话的客户端发送消息
   *
   * @param conversationId 会话ID
   * @param eventName 事件名称
   * @param data 消息数据
   */
  public void sendMessage(Long conversationId, String eventName, Object data) {
    SseEmitter emitter = emitters.get(conversationId);
    if (emitter != null) {
      try {
        Object sendData = data;

        // 处理chunk事件
        if (SSE_EVENT_CHUNK.equals(eventName)) {
          // 对于chunk事件，使用JSON包装以保留换行符
          if (data != null) {
            // 指定HashMap初始容量为2
            Map<String, String> wrapper = new HashMap<>(2);
            wrapper.put("content", String.valueOf(data));
            // JSON序列化会自动转义换行符，避免SSE协议冲突
            sendData = objectMapper.writeValueAsString(wrapper);
          } else {
            return;
          }
        } else if (!(data instanceof String) && data != null) {
          // 对于非chunk事件的非字符串数据，也进行JSON序列化
          sendData = objectMapper.writeValueAsString(data);
        }

        // 发送事件数据
        emitter.send(SseEmitter.event().name(eventName).data(sendData));

      } catch (Exception e) {
        log.error("发送SSE事件失败，会话ID: {}, 事件类型: {}, 错误: {}", 
                  conversationId, eventName, e.getMessage(), e);
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
   * 检查指定会话是否存在SSE Emitter
   *
   * @param conversationId 会话ID
   * @return 如果存在返回true，否则返回false
   */
  public boolean hasEmitter(Long conversationId) {
    return emitters.containsKey(conversationId);
  }
}
