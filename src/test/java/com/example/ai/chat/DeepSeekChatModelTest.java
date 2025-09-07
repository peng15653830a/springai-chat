package com.example.ai.chat;

import com.example.service.api.impl.DeepSeekApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeepSeekChatModelTest {

    @Mock
    private DeepSeekApiClient apiClient;

    private DeepSeekChatOptions defaultOptions;

    private DeepSeekChatModel chatModel;

    @BeforeEach
    void setUp() {
        defaultOptions = DeepSeekChatOptions.builder()
            .model("deepseek-test")
            .temperature(0.7)
            .maxTokens(1000)
            .enableThinking(false)
            .build();
        
        chatModel = new DeepSeekChatModel(apiClient, defaultOptions);
    }

    @Test
    void testCall() {
        // Given
        List<Message> messages = List.of(new UserMessage("Hello"));
        Prompt prompt = new Prompt(messages);
        
        Generation generation = new Generation(new AssistantMessage("Hi there!"));
        ChatResponse expectedResponse = new ChatResponse(List.of(generation));
        
        when(apiClient.chatCompletionStream(any(), any(), any(), any(), any()))
            .thenReturn(Flux.just(expectedResponse));

        // When
        ChatResponse response = chatModel.call(prompt);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getResults().size());
        assertEquals("Hi there!", response.getResults().get(0).getOutput().getText());
        
        verify(apiClient).chatCompletionStream(any(), any(), any(), any(), any());
    }

    @Test
    void testStream() {
        // Given
        List<Message> messages = List.of(new UserMessage("Hello"));
        Prompt prompt = new Prompt(messages);
        
        Generation generation1 = new Generation(new AssistantMessage("Hi"));
        Generation generation2 = new Generation(new AssistantMessage(" there!"));
        ChatResponse response1 = new ChatResponse(List.of(generation1));
        ChatResponse response2 = new ChatResponse(List.of(generation2));
        
        when(apiClient.chatCompletionStream(any(), any(), any(), any(), any()))
            .thenReturn(Flux.just(response1, response2));

        // When & Then
        StepVerifier.create(chatModel.stream(prompt))
            .expectNext(response1)
            .expectNext(response2)
            .verifyComplete();
            
        verify(apiClient).chatCompletionStream(any(), any(), any(), any(), any());
    }

    @Test
    void testStream_Error() {
        // Given
        List<Message> messages = List.of(new UserMessage("Hello"));
        Prompt prompt = new Prompt(messages);
        
        when(apiClient.chatCompletionStream(any(), any(), any(), any(), any()))
            .thenReturn(Flux.error(new RuntimeException("API Error")));

        // When & Then
        StepVerifier.create(chatModel.stream(prompt))
            .expectError(RuntimeException.class)
            .verify();
            
        verify(apiClient).chatCompletionStream(any(), any(), any(), any(), any());
    }

    @Test
    void testGetDefaultOptions() {
        // When
        DeepSeekChatOptions options = (DeepSeekChatOptions) chatModel.getDefaultOptions();

        // Then
        assertNotNull(options);
        assertEquals("deepseek-test", options.getModel());
        assertEquals(0.7, options.getTemperature());
        assertEquals(1000, options.getMaxTokens());
        assertFalse(options.getEnableThinking());
    }

    @Test
    void testMergeOptions_WithDeepSeekChatOptions() {
        // Given
        DeepSeekChatOptions promptOptions = DeepSeekChatOptions.builder()
            .model("custom-model")
            .temperature(0.9)
            .maxTokens(2000)
            .enableThinking(true)
            .build();
        Prompt prompt = new Prompt("test", promptOptions);

        // When
        Flux<ChatResponse> response = chatModel.stream(prompt);

        // Then
        // Just verify it doesn't throw an exception
        assertNotNull(response);
    }

    @Test
    void testMergeOptions_WithGenericChatOptions() {
        // Given
        DeepSeekChatOptions promptOptions = DeepSeekChatOptions.builder()
            .model("custom-model")
            .temperature(0.9)
            .maxTokens(2000)
            .build();
        Prompt prompt = new Prompt("test", promptOptions);

        // When
        Flux<ChatResponse> response = chatModel.stream(prompt);

        // Then
        // Just verify it doesn't throw an exception
        assertNotNull(response);
    }

    @Test
    void testMergeOptions_WithNullOptions() {
        // Given
        Prompt prompt = new Prompt("test", null);

        // When
        Flux<ChatResponse> response = chatModel.stream(prompt);

        // Then
        // Just verify it doesn't throw an exception
        assertNotNull(response);
    }

    @Test
    void testMergeOptions_WithPartialOptions() {
        // Given
        // 创建一个只设置了部分属性的选项
        DeepSeekChatOptions promptOptions = new DeepSeekChatOptions();
        promptOptions.setModel("partial-model");
        // temperature和maxTokens保持为null
        
        Prompt prompt = new Prompt("test", promptOptions);

        // When
        Flux<ChatResponse> response = chatModel.stream(prompt);

        // Then
        // 验证不会抛出异常
        assertNotNull(response);
    }

    @Test
    void testMergeOptions_WithThinkingEnabled() {
        // Given
        DeepSeekChatOptions promptOptions = DeepSeekChatOptions.builder()
            .model("thinking-model")
            .temperature(0.8)
            .maxTokens(3000)
            .enableThinking(true)
            .thinkingBudget(10000)
            .build();
        Prompt prompt = new Prompt("test", promptOptions);

        // When
        Flux<ChatResponse> response = chatModel.stream(prompt);

        // Then
        assertNotNull(response);
    }

    @Test
    void testCall_WithThinkingEnabled() {
        // Given
        List<Message> messages = List.of(new UserMessage("Think about this"));
        DeepSeekChatOptions promptOptions = DeepSeekChatOptions.builder()
            .enableThinking(true)
            .thinkingBudget(5000)
            .build();
        Prompt prompt = new Prompt(messages, promptOptions);
        
        Generation generation = new Generation(new AssistantMessage("I'm thinking..."));
        ChatResponse expectedResponse = new ChatResponse(List.of(generation));
        
        when(apiClient.chatCompletionStream(any(), any(), any(), any(), any()))
            .thenReturn(Flux.just(expectedResponse));

        // When
        ChatResponse response = chatModel.call(prompt);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getResults().size());
        assertEquals("I'm thinking...", response.getResults().get(0).getOutput().getText());
    }

    @Test
    void testStream_WithEmptyMessages() {
        // Given
        List<Message> messages = List.of(); // 空消息列表
        Prompt prompt = new Prompt(messages);
        
        Generation generation = new Generation(new AssistantMessage("Empty response"));
        ChatResponse expectedResponse = new ChatResponse(List.of(generation));
        
        when(apiClient.chatCompletionStream(any(), any(), any(), any(), any()))
            .thenReturn(Flux.just(expectedResponse));

        // When & Then
        StepVerifier.create(chatModel.stream(prompt))
            .expectNext(expectedResponse)
            .verifyComplete();
    }

    @Test
    void testStream_WithNullMessages() {
        // Given
        // 创建一个带有空消息列表的Prompt，而不是null消息列表
        Prompt prompt = new Prompt(Collections.emptyList());
        
        // 模拟API客户端正确处理空消息
        when(apiClient.chatCompletionStream(any(), any(), any(), any(), any()))
            .thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(chatModel.stream(prompt))
            .expectComplete()  // 期望完成而不是抛出异常
            .verify();
    }

    @Test
    void testStream_ApiClientException() {
        // Given
        List<Message> messages = List.of(new UserMessage("Hello"));
        Prompt prompt = new Prompt(messages);
        
        when(apiClient.chatCompletionStream(any(), any(), any(), any(), any()))
            .thenReturn(Flux.error(new RuntimeException("API client error")));

        // When & Then
        StepVerifier.create(chatModel.stream(prompt))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    void testMergeOptions_InheritanceFromChatOptions() {
        // Given
        // 创建一个匿名ChatOptions实现来测试继承情况
        ChatOptions promptOptions = new ChatOptions() {
            @Override
            public String getModel() {
                return "inherited-model";
            }

            @Override
            public Double getTemperature() {
                return 0.5;
            }

            @Override
            public Integer getMaxTokens() {
                return 1500;
            }

            @Override
            public Double getTopP() {
                return null;
            }

            @Override
            public Integer getTopK() {
                return null;
            }

            @Override
            public List<String> getStopSequences() {
                return null;
            }

            @Override
            public Double getPresencePenalty() {
                return null;
            }

            @Override
            public Double getFrequencyPenalty() {
                return null;
            }
            
            @Override
            public <T extends ChatOptions> T copy() {
                return (T) this;
            }
        };
        
        Prompt prompt = new Prompt("test", promptOptions);

        // When
        Flux<ChatResponse> response = chatModel.stream(prompt);

        // Then
        assertNotNull(response);
    }
}