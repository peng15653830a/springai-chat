package com.example.ai.chat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.dto.request.ChatCompletionRequest;
import com.example.dto.response.ChatCompletionResponse;
import com.example.integration.ai.greatwall.GreatWallChatApi;
import com.example.integration.ai.greatwall.GreatWallChatModel;
import com.example.integration.ai.greatwall.GreatWallChatOptions;
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
class GreatWallChatModelTest {

  @Mock private GreatWallChatApi apiClient;

  private GreatWallChatOptions defaultOptions;

  private GreatWallChatModel chatModel;

  @BeforeEach
  void setUp() {
    defaultOptions =
        GreatWallChatOptions.builder()
            .model("greatwall-test")
            .temperature(0.7)
            .maxTokens(1000)
            .enableThinking(false)
            .build();

    chatModel = new GreatWallChatModel(apiClient, defaultOptions);
  }

  @Test
  void testCall() {
    // Given
    List<Message> messages = List.of(new UserMessage("Hello"));
    Prompt prompt = new Prompt(messages);

    // 使用正确的ChatCompletionResponse类型
    ChatCompletionResponse response =
        ChatCompletionResponse.builder()
            .id("test-id")
            .object("chat.completion")
            .created(System.currentTimeMillis() / 1000)
            .model("greatwall-test")
            .choices(
                List.of(
                    ChatCompletionResponse.Choice.builder()
                        .index(0)
                        .delta(ChatCompletionResponse.Delta.builder().content("Hi there!").build())
                        .build()))
            .build();

    when(apiClient.chatCompletionStream(any(ChatCompletionRequest.class)))
        .thenReturn(Flux.just(response));

    // When
    ChatResponse chatResponse = chatModel.call(prompt);

    // Then
    assertNotNull(chatResponse);
    assertEquals(1, chatResponse.getResults().size());
    assertEquals("Hi there!", chatResponse.getResults().get(0).getOutput().getText());

    verify(apiClient).chatCompletionStream(any(ChatCompletionRequest.class));
  }

  @Test
  void testStream() {
    // Given
    List<Message> messages = List.of(new UserMessage("Hello"));
    Prompt prompt = new Prompt(messages);

    // 使用正确的ChatCompletionResponse类型
    ChatCompletionResponse response1 =
        ChatCompletionResponse.builder()
            .id("test-id-1")
            .object("chat.completion.chunk")
            .created(System.currentTimeMillis() / 1000)
            .model("greatwall-test")
            .choices(
                List.of(
                    ChatCompletionResponse.Choice.builder()
                        .index(0)
                        .delta(ChatCompletionResponse.Delta.builder().content("Hi").build())
                        .build()))
            .build();

    ChatCompletionResponse response2 =
        ChatCompletionResponse.builder()
            .id("test-id-2")
            .object("chat.completion.chunk")
            .created(System.currentTimeMillis() / 1000)
            .model("greatwall-test")
            .choices(
                List.of(
                    ChatCompletionResponse.Choice.builder()
                        .index(0)
                        .delta(ChatCompletionResponse.Delta.builder().content(" there!").build())
                        .build()))
            .build();

    when(apiClient.chatCompletionStream(any(ChatCompletionRequest.class)))
        .thenReturn(
            Flux.just(
                ChatCompletionResponse.builder()
                    .id("test-id-1")
                    .object("chat.completion.chunk")
                    .created(System.currentTimeMillis() / 1000)
                    .model("greatwall-test")
                    .choices(
                        List.of(
                            ChatCompletionResponse.Choice.builder()
                                .index(0)
                                .delta(ChatCompletionResponse.Delta.builder().content("Hi").build())
                                .build()))
                    .build(),
                ChatCompletionResponse.builder()
                    .id("test-id-2")
                    .object("chat.completion.chunk")
                    .created(System.currentTimeMillis() / 1000)
                    .model("greatwall-test")
                    .choices(
                        List.of(
                            ChatCompletionResponse.Choice.builder()
                                .index(0)
                                .delta(
                                    ChatCompletionResponse.Delta.builder()
                                        .content(" there!")
                                        .build())
                                .build()))
                    .build()));

    // When & Then
    StepVerifier.create(chatModel.stream(prompt)).expectNextCount(2).verifyComplete();

    verify(apiClient).chatCompletionStream(any(ChatCompletionRequest.class));
  }

  @Test
  void testStream_Error() {
    // Given
    List<Message> messages = List.of(new UserMessage("Hello"));
    Prompt prompt = new Prompt(messages);

    when(apiClient.chatCompletionStream(any(ChatCompletionRequest.class)))
        .thenReturn(Flux.error(new RuntimeException("API Error")));

    // When & Then
    StepVerifier.create(chatModel.stream(prompt)).expectError(RuntimeException.class).verify();

    verify(apiClient).chatCompletionStream(any(ChatCompletionRequest.class));
  }

  @Test
  void testGetDefaultOptions() {
    // When
    GreatWallChatOptions options = (GreatWallChatOptions) chatModel.getDefaultOptions();

    // Then
    assertNotNull(options);
    assertEquals("greatwall-test", options.getModel());
    assertEquals(0.7, options.getTemperature());
    assertEquals(1000, options.getMaxTokens());
    assertFalse(options.getEnableThinking());
  }

  @Test
  void testMergeOptions_WithGreatWallChatOptions() {
    // Given
    GreatWallChatOptions promptOptions =
        GreatWallChatOptions.builder()
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
    GreatWallChatOptions promptOptions =
        GreatWallChatOptions.builder()
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
    GreatWallChatOptions promptOptions = new GreatWallChatOptions();
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
    GreatWallChatOptions promptOptions =
        GreatWallChatOptions.builder()
            .model("thinking-model")
            .temperature(0.8)
            .maxTokens(3000)
            .enableThinking(true)
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
    GreatWallChatOptions promptOptions =
        GreatWallChatOptions.builder().enableThinking(true).build();
    Prompt prompt = new Prompt(messages, promptOptions);

    Generation generation = new Generation(new AssistantMessage("I'm thinking..."));
    ChatResponse expectedResponse = new ChatResponse(List.of(generation));

    when(apiClient.chatCompletionStream(any(ChatCompletionRequest.class)))
        .thenReturn(
            Flux.just(
                ChatCompletionResponse.builder()
                    .id("test-id")
                    .object("chat.completion")
                    .created(System.currentTimeMillis() / 1000)
                    .model("greatwall-test")
                    .choices(
                        List.of(
                            ChatCompletionResponse.Choice.builder()
                                .index(0)
                                .delta(
                                    ChatCompletionResponse.Delta.builder()
                                        .content("I'm thinking...")
                                        .build())
                                .build()))
                    .build()));

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

    // When & Then - 期望抛出IllegalArgumentException
    StepVerifier.create(chatModel.stream(prompt))
        .expectError(IllegalArgumentException.class)
        .verify();
  }

  @Test
  void testStream_WithNullMessages() {
    // Given - 直接测试null消息的情况，而不是通过Prompt构造函数
    // 由于Spring的Prompt构造函数不允许null消息，我们直接测试模型内部的处理逻辑
    List<Message> nullMessages = null;

    // 创建一个mock的Prompt来绕过构造函数限制
    Prompt mockPrompt = mock(Prompt.class);
    when(mockPrompt.getInstructions()).thenReturn(nullMessages);

    // When & Then - 期望抛出IllegalArgumentException
    StepVerifier.create(chatModel.stream(mockPrompt))
        .expectError(IllegalArgumentException.class)
        .verify();
  }

  @Test
  void testStream_ApiClientException() {
    // Given
    List<Message> messages = List.of(new UserMessage("Hello"));
    Prompt prompt = new Prompt(messages);

    when(apiClient.chatCompletionStream(any(ChatCompletionRequest.class)))
        .thenReturn(Flux.error(new RuntimeException("API client error")));

    // When & Then
    StepVerifier.create(chatModel.stream(prompt)).expectError(RuntimeException.class).verify();
  }

  @Test
  void testMergeOptions_InheritanceFromChatOptions() {
    // Given
    // 创建一个匿名ChatOptions实现来测试继承情况
    ChatOptions promptOptions =
        new ChatOptions() {
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
