package com.example.config;

import com.example.ai.chat.DeepSeekChatModel;
import com.example.ai.chat.GreatWallChatModel;
import com.example.service.ChatModelRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.ApplicationContext;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnhancedAiConfigTest {

    @Mock
    private MultiModelProperties multiModelProperties;
    
    @Mock
    private ApplicationContext applicationContext;

    private EnhancedAiConfig config;

    @BeforeEach
    void setUp() {
        config = new EnhancedAiConfig();
    }

    @Test
    void testChatModelRegistryCreation() {
        // When
        ChatModelRegistry registry = config.chatModelRegistry(applicationContext, multiModelProperties);

        // Then
        assertNotNull(registry);
    }
}