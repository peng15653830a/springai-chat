package com.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {
    
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public SseEmitter createEmitter(Long conversationId) {
        if (conversationId == null || conversationId <= 0) {
            throw new IllegalArgumentException("会话ID无效");
        }
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时，给AI回复足够时间
        
        emitter.onCompletion(() -> {
            emitters.remove(conversationId);
        });
        
        emitter.onTimeout(() -> {
            emitters.remove(conversationId);
            emitter.complete();
        });
        
        emitter.onError((ex) -> {
            emitters.remove(conversationId);
            emitter.completeWithError(ex);
        });
        
        emitters.put(conversationId, emitter);
        return emitter;
    }
    
    public SseEmitter getEmitter(Long conversationId) {
        return emitters.get(conversationId);
    }
    
    public void removeEmitter(Long conversationId) {
        SseEmitter emitter = emitters.remove(conversationId);
        if (emitter != null) {
            emitter.complete();
        }
    }
    
    public void sendMessage(Long conversationId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(conversationId);
        if (emitter != null) {
            try {
                Object sendData = data;
                
                if ("chunk".equals(eventName)) {
                    // 对于chunk事件，使用JSON包装以保留换行符
                    if (data != null) {
                        Map<String, String> wrapper = new HashMap<>();
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
                
                emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(sendData));
                    
            } catch (Exception e) {
                emitters.remove(conversationId);
                emitter.completeWithError(e);
            }
        }
    }
    
    public boolean hasEmitter(Long conversationId) {
        return emitters.containsKey(conversationId);
    }
}