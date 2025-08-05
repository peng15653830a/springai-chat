package com.example.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {
    
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    
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
                emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
            } catch (IOException e) {
                emitters.remove(conversationId);
                emitter.completeWithError(e);
            }
        }
    }
    
    public boolean hasEmitter(Long conversationId) {
        return emitters.containsKey(conversationId);
    }
}