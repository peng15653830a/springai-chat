package com.example.service.impl;

import com.example.config.ChatStreamingProperties;
import com.example.dto.request.StreamChatRequest;
import com.example.dto.stream.ChatEvent;
import com.example.entity.Message;
import com.example.manager.ChatClientManager;
import com.example.service.ConversationService;
import com.example.service.MessageService;
import com.example.service.SearchService;
import com.example.handler.ChatErrorHandler;
import com.example.strategy.model.ModelSelector;
import com.example.strategy.prompt.PromptBuilder;
import com.example.service.impl.SseEventPublisherImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.example.constant.AiChatConstants.ROLE_USER;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AiChatServiceImpl测试类
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
class AiChatServiceImplTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private SearchService searchService;

    @Mock
    private MessageService messageService;

    @Mock
    private ChatClientManager chatClientManager;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec promptRequestSpec;
    
    @Mock
    private ChatClient.StreamResponseSpec streamResponseSpec;

    @Mock
    private ModelSelector modelSelector;

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private ChatErrorHandler errorHandler;

    @Mock
    private ChatStreamingProperties streamingProperties;

    @Mock
    private SseEventPublisherImpl sseEventPublisher;

    @InjectMocks
    private AiChatServiceImpl aiChatService;

    private final Long conversationId = 1L;
    private final String userMessage = "Hello, AI!";
    private final Long userId = 123L;

    // Helper method to create Message objects
    private Message createMessage(Long id, Long conversationId, String role, String content) {
        Message message = new Message();
        message.setId(id);
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

    @BeforeEach
    void setUp() {
        lenient().when(streamingProperties.getResponseTimeout()).thenReturn(Duration.ofSeconds(30));
        lenient().when(conversationService.generateTitleIfNeededAsync(any(), any())).thenReturn(Mono.empty());
        
        // 添加对SseEventPublisherImpl方法的mock
        Sinks.Many<ChatEvent> mockSink = mock(Sinks.Many.class);
        lenient().when(sseEventPublisher.registerConversation(anyLong())).thenReturn(mockSink);
        lenient().when(mockSink.asFlux()).thenReturn(Flux.empty());
        
        // 添加对其他SseEventPublisherImpl方法的mock（仅保留 removeConversation）
        lenient().doNothing().when(sseEventPublisher).removeConversation(anyLong());
        
        // 添加对registerConversationFlux的mock
        lenient().when(sseEventPublisher.registerConversationFlux(anyLong())).thenReturn(Flux.empty());
        
        // 移除全局ModelSelector mock设置，改为在具体测试中设置
    }

    @Test
    void shouldHandleErrorInMessageSaving() {
        // Given
        StreamChatRequest request = new StreamChatRequest();
        request.setConversationId(conversationId);
        request.setMessage(userMessage);
        request.setSearchEnabled(false);
        request.setDeepThinking(false);

        when(messageService.saveUserMessageAsync(conversationId, userMessage))
            .thenReturn(Mono.error(new RuntimeException("Database error")));
        
        lenient().when(promptBuilder.buildPrompt(conversationId, userMessage, false))
            .thenReturn(Mono.just("提示词"));
            
        when(errorHandler.handleChatError(any()))
            .thenReturn(Flux.just(ChatEvent.error("AI服务暂时不可用，请稍后重试")));

        // When & Then
        StepVerifier.create(aiChatService.streamChat(request))
            .expectNext(ChatEvent.error("AI服务暂时不可用，请稍后重试"))
            .verifyComplete();

        verify(errorHandler).handleChatError(any());
    }
    
    @Test
    void shouldStreamChatSuccessfully() {
        // Given
        StreamChatRequest request = new StreamChatRequest();
        request.setConversationId(conversationId);
        request.setMessage(userMessage);
        request.setSearchEnabled(false);
        request.setDeepThinking(false);
        request.setUserId(null);
        request.setProvider(null);
        request.setModel(null);

        when(messageService.saveUserMessageAsync(conversationId, userMessage))
            .thenReturn(Mono.just(createMessage(1L, conversationId, ROLE_USER, userMessage)));

        lenient().when(promptBuilder.buildPrompt(conversationId, userMessage, false))
            .thenReturn(Mono.just("构建的完整提示词"));
        
        // Mock ModelSelector
        lenient().when(modelSelector.getActualProviderName(null)).thenReturn("test-provider");
        lenient().when(modelSelector.getActualModelName("test-provider", null)).thenReturn("test-model");
            
        // Mock ChatClient chain
        lenient().when(chatClientManager.getChatClient("test-provider")).thenReturn(chatClient);
        lenient().when(chatClient.prompt()).thenReturn(promptRequestSpec);
        lenient().when(promptRequestSpec.user(anyString())).thenReturn(promptRequestSpec);
        lenient().when(promptRequestSpec.options(any())).thenReturn(promptRequestSpec);
        lenient().doReturn(promptRequestSpec).when(promptRequestSpec).advisors(any(org.springframework.ai.chat.client.advisor.api.Advisor[].class));
        lenient().doReturn(promptRequestSpec).when(promptRequestSpec).advisors(any(java.util.function.Consumer.class));
        lenient().when(promptRequestSpec.toolContext(anyMap())).thenReturn(promptRequestSpec);
        lenient().when(promptRequestSpec.stream()).thenReturn(streamResponseSpec);
        
        // 使用chatResponse而不是content，并模拟ChatResponse对象
        org.springframework.ai.chat.model.ChatResponse chatResponse = mock(org.springframework.ai.chat.model.ChatResponse.class);
        org.springframework.ai.chat.model.Generation generation = mock(org.springframework.ai.chat.model.Generation.class);
        org.springframework.ai.chat.messages.AssistantMessage assistantMessage = mock(org.springframework.ai.chat.messages.AssistantMessage.class);
        
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        when(assistantMessage.getText()).thenReturn("AI response chunk");
        lenient().when(streamResponseSpec.chatResponse()).thenReturn(Flux.just(chatResponse));

        // When & Then
        StepVerifier.create(aiChatService.streamChat(request))
            .expectNext(ChatEvent.start("AI正在思考中..."))
            .expectNext(ChatEvent.chunk("AI response chunk"))
            .expectNextMatches(ev -> ev.getType() == ChatEvent.ChatEventType.END)
            .verifyComplete();
    }
}
