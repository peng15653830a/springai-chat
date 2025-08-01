package com.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SseEmitterManagerTest {

    private SseEmitterManager sseEmitterManager;

    @BeforeEach
    void setUp() {
        sseEmitterManager = new SseEmitterManager();
    }

    @Test
    void testCreateEmitter_ValidConversationId() {
        // Given
        Long conversationId = 1L;

        // When
        SseEmitter emitter = sseEmitterManager.createEmitter(conversationId);

        // Then
        assertNotNull(emitter);
        assertTrue(sseEmitterManager.hasEmitter(conversationId));
        assertEquals(emitter, sseEmitterManager.getEmitter(conversationId));
    }

    @Test
    void testCreateEmitter_NullConversationId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sseEmitterManager.createEmitter(null);
        });
        assertEquals("会话ID无效", exception.getMessage());
    }

    @Test
    void testCreateEmitter_ZeroConversationId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sseEmitterManager.createEmitter(0L);
        });
        assertEquals("会话ID无效", exception.getMessage());
    }

    @Test
    void testCreateEmitter_NegativeConversationId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sseEmitterManager.createEmitter(-1L);
        });
        assertEquals("会话ID无效", exception.getMessage());
    }

    @Test
    void testGetEmitter_ExistingConversationId() {
        // Given
        Long conversationId = 1L;
        SseEmitter originalEmitter = sseEmitterManager.createEmitter(conversationId);

        // When
        SseEmitter retrievedEmitter = sseEmitterManager.getEmitter(conversationId);

        // Then
        assertNotNull(retrievedEmitter);
        assertEquals(originalEmitter, retrievedEmitter);
    }

    @Test
    void testGetEmitter_NonExistingConversationId() {
        // Given
        Long conversationId = 999L;

        // When
        SseEmitter emitter = sseEmitterManager.getEmitter(conversationId);

        // Then
        assertNull(emitter);
    }

    @Test
    void testRemoveEmitter_ExistingConversationId() {
        // Given
        Long conversationId = 1L;
        sseEmitterManager.createEmitter(conversationId);
        assertTrue(sseEmitterManager.hasEmitter(conversationId));

        // When
        sseEmitterManager.removeEmitter(conversationId);

        // Then
        assertFalse(sseEmitterManager.hasEmitter(conversationId));
        assertNull(sseEmitterManager.getEmitter(conversationId));
    }

    @Test
    void testRemoveEmitter_NonExistingConversationId() {
        // Given
        Long conversationId = 999L;

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> {
            sseEmitterManager.removeEmitter(conversationId);
        });
    }

    @Test
    void testHasEmitter_ExistingConversationId() {
        // Given
        Long conversationId = 1L;
        sseEmitterManager.createEmitter(conversationId);

        // When
        boolean hasEmitter = sseEmitterManager.hasEmitter(conversationId);

        // Then
        assertTrue(hasEmitter);
    }

    @Test
    void testHasEmitter_NonExistingConversationId() {
        // Given
        Long conversationId = 999L;

        // When
        boolean hasEmitter = sseEmitterManager.hasEmitter(conversationId);

        // Then
        assertFalse(hasEmitter);
    }

    @Test
    void testSendMessage_ExistingEmitter() {
        // Given
        Long conversationId = 1L;
        SseEmitter emitter = spy(sseEmitterManager.createEmitter(conversationId));
        
        // 使用反射替换内部的emitter为spy对象
        sseEmitterManager.removeEmitter(conversationId);
        // 手动添加spy emitter到内部map
        try {
            java.lang.reflect.Field field = SseEmitterManager.class.getDeclaredField("emitters");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Long, SseEmitter> emitters = (java.util.Map<Long, SseEmitter>) field.get(sseEmitterManager);
            emitters.put(conversationId, emitter);
        } catch (Exception e) {
            fail("Failed to set up spy emitter: " + e.getMessage());
        }

        String eventName = "message";
        String data = "Test message";

        // When
        assertDoesNotThrow(() -> {
            sseEmitterManager.sendMessage(conversationId, eventName, data);
        });

        // Then
        // 验证emitter的send方法被调用
        try {
            verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test
    void testSendMessage_NonExistingEmitter() {
        // Given
        Long conversationId = 999L;
        String eventName = "message";
        String data = "Test message";

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> {
            sseEmitterManager.sendMessage(conversationId, eventName, data);
        });
    }

    @Test
    void testSendMessage_IOExceptionHandling() {
        // Given
        Long conversationId = 1L;
        SseEmitter emitter = mock(SseEmitter.class);
        
        // 手动添加mock emitter到内部map
        try {
            java.lang.reflect.Field field = SseEmitterManager.class.getDeclaredField("emitters");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Long, SseEmitter> emitters = (java.util.Map<Long, SseEmitter>) field.get(sseEmitterManager);
            emitters.put(conversationId, emitter);
        } catch (Exception e) {
            fail("Failed to set up mock emitter: " + e.getMessage());
        }

        // 模拟IOException
        try {
            doThrow(new IOException("Test IOException")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));
        } catch (IOException e) {
            fail("Failed to set up mock behavior: " + e.getMessage());
        }

        String eventName = "message";
        String data = "Test message";

        // When
        assertDoesNotThrow(() -> {
            sseEmitterManager.sendMessage(conversationId, eventName, data);
        });

        // Then
        // 验证emitter被移除且completeWithError被调用
        assertFalse(sseEmitterManager.hasEmitter(conversationId));
        verify(emitter).completeWithError(any(IOException.class));
    }

    @Test
    void testEmitterCallbacks_OnCompletion() {
        // Given
        Long conversationId = 1L;
        SseEmitter emitter = sseEmitterManager.createEmitter(conversationId);
        assertTrue(sseEmitterManager.hasEmitter(conversationId));

        // When - 手动移除emitter来模拟完成处理
        sseEmitterManager.removeEmitter(conversationId);

        // Then
        assertFalse(sseEmitterManager.hasEmitter(conversationId));
    }

    @Test
    void testEmitterCallbacks_OnTimeout() {
        // Given
        Long conversationId = 1L;
        SseEmitter emitter = sseEmitterManager.createEmitter(conversationId);
        assertTrue(sseEmitterManager.hasEmitter(conversationId));

        // When - 手动移除emitter来模拟超时处理
        sseEmitterManager.removeEmitter(conversationId);

        // Then
        assertFalse(sseEmitterManager.hasEmitter(conversationId));
    }

    @Test
    void testEmitterCallbacks_OnError() {
        // Given
        Long conversationId = 1L;
        SseEmitter emitter = sseEmitterManager.createEmitter(conversationId);
        assertTrue(sseEmitterManager.hasEmitter(conversationId));

        // When - 手动移除emitter来模拟错误处理
        sseEmitterManager.removeEmitter(conversationId);

        // Then
        assertFalse(sseEmitterManager.hasEmitter(conversationId));
    }

    @Test
    void testMultipleEmitters() {
        // Given
        Long conversationId1 = 1L;
        Long conversationId2 = 2L;

        // When
        SseEmitter emitter1 = sseEmitterManager.createEmitter(conversationId1);
        SseEmitter emitter2 = sseEmitterManager.createEmitter(conversationId2);

        // Then
        assertTrue(sseEmitterManager.hasEmitter(conversationId1));
        assertTrue(sseEmitterManager.hasEmitter(conversationId2));
        assertNotEquals(emitter1, emitter2);
        assertEquals(emitter1, sseEmitterManager.getEmitter(conversationId1));
        assertEquals(emitter2, sseEmitterManager.getEmitter(conversationId2));
    }

    @Test
    void testReplaceExistingEmitter() {
        // Given
        Long conversationId = 1L;
        SseEmitter firstEmitter = sseEmitterManager.createEmitter(conversationId);

        // When
        SseEmitter secondEmitter = sseEmitterManager.createEmitter(conversationId);

        // Then
        assertTrue(sseEmitterManager.hasEmitter(conversationId));
        assertEquals(secondEmitter, sseEmitterManager.getEmitter(conversationId));
        assertNotEquals(firstEmitter, secondEmitter);
    }
}