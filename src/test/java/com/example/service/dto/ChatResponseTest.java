package com.example.service.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatResponse测试
 *
 * @author xupeng
 */
class ChatResponseTest {

  @Test
  void shouldCreateChatResponse() {
    // Given
    String id = "chatcmpl-123";
    String object = "chat.completion";
    String model = "gpt-3.5-turbo";
    Long created = System.currentTimeMillis();

    // When
    ChatResponse response = new ChatResponse();
    response.setId(id);
    response.setObject(object);
    response.setModel(model);
    response.setCreated(created);

    // Then
    assertEquals(id, response.getId());
    assertEquals(object, response.getObject());
    assertEquals(model, response.getModel());
    assertEquals(created, response.getCreated());
  }

  @Test
  void shouldCreateChoiceWithMessage() {
    // Given
    String role = "assistant";
    String content = "Hello! How can I help you?";
    String finishReason = "stop";
    Integer index = 0;

    // When
    ChatResponse.ResponseMessage message = new ChatResponse.ResponseMessage();
    message.setRole(role);
    message.setContent(content);

    ChatResponse.Choice choice = new ChatResponse.Choice();
    choice.setIndex(index);
    choice.setMessage(message);
    choice.setFinishReason(finishReason);

    // Then
    assertEquals(index, choice.getIndex());
    assertEquals(finishReason, choice.getFinishReason());
    assertNotNull(choice.getMessage());
    assertEquals(role, choice.getMessage().getRole());
    assertEquals(content, choice.getMessage().getContent());
  }

  @Test
  void shouldCreateUsageStats() {
    // Given
    Integer promptTokens = 10;
    Integer completionTokens = 20;
    Integer totalTokens = 30;

    // When
    ChatResponse.Usage usage = new ChatResponse.Usage();
    usage.setPromptTokens(promptTokens);
    usage.setCompletionTokens(completionTokens);
    usage.setTotalTokens(totalTokens);

    // Then
    assertEquals(promptTokens, usage.getPromptTokens());
    assertEquals(completionTokens, usage.getCompletionTokens());
    assertEquals(totalTokens, usage.getTotalTokens());
  }

  @Test
  void shouldCreateCompleteChatResponse() {
    // Given
    ChatResponse.ResponseMessage message = new ChatResponse.ResponseMessage();
    message.setRole("assistant");
    message.setContent("测试回复");

    ChatResponse.Choice choice = new ChatResponse.Choice();
    choice.setIndex(0);
    choice.setMessage(message);
    choice.setFinishReason("stop");

    ChatResponse.Usage usage = new ChatResponse.Usage();
    usage.setPromptTokens(15);
    usage.setCompletionTokens(25);
    usage.setTotalTokens(40);

    // When
    ChatResponse response = new ChatResponse();
    response.setId("test-123");
    response.setObject("chat.completion");
    response.setModel("test-model");
    response.setCreated(1234567890L);
    response.setChoices(Arrays.asList(choice));
    response.setUsage(usage);

    // Then
    assertEquals("test-123", response.getId());
    assertEquals("chat.completion", response.getObject());
    assertEquals("test-model", response.getModel());
    assertEquals(1234567890L, response.getCreated());
    
    assertNotNull(response.getChoices());
    assertEquals(1, response.getChoices().size());
    assertEquals("测试回复", response.getChoices().get(0).getMessage().getContent());
    
    assertNotNull(response.getUsage());
    assertEquals(40, response.getUsage().getTotalTokens());
  }

  @Test
  void shouldTestMessageEquality() {
    // Given
    ChatResponse.ResponseMessage message1 = new ChatResponse.ResponseMessage();
    message1.setRole("user");
    message1.setContent("Hello");

    ChatResponse.ResponseMessage message2 = new ChatResponse.ResponseMessage();
    message2.setRole("user");
    message2.setContent("Hello");

    ChatResponse.ResponseMessage message3 = new ChatResponse.ResponseMessage();
    message3.setRole("assistant");
    message3.setContent("Hi");

    // Then
    assertEquals(message1, message2);
    assertNotEquals(message1, message3);
    assertEquals(message1.hashCode(), message2.hashCode());
  }

  @Test
  void shouldTestChoiceEquality() {
    // Given
    ChatResponse.Choice choice1 = new ChatResponse.Choice();
    choice1.setIndex(0);
    choice1.setFinishReason("stop");

    ChatResponse.Choice choice2 = new ChatResponse.Choice();
    choice2.setIndex(0);
    choice2.setFinishReason("stop");

    ChatResponse.Choice choice3 = new ChatResponse.Choice();
    choice3.setIndex(1);
    choice3.setFinishReason("length");

    // Then
    assertEquals(choice1, choice2);
    assertNotEquals(choice1, choice3);
    assertEquals(choice1.hashCode(), choice2.hashCode());
  }

  @Test
  void shouldTestUsageEquality() {
    // Given
    ChatResponse.Usage usage1 = new ChatResponse.Usage();
    usage1.setPromptTokens(10);
    usage1.setCompletionTokens(20);
    usage1.setTotalTokens(30);

    ChatResponse.Usage usage2 = new ChatResponse.Usage();
    usage2.setPromptTokens(10);
    usage2.setCompletionTokens(20);
    usage2.setTotalTokens(30);

    ChatResponse.Usage usage3 = new ChatResponse.Usage();
    usage3.setPromptTokens(15);
    usage3.setCompletionTokens(25);
    usage3.setTotalTokens(40);

    // Then
    assertEquals(usage1, usage2);
    assertNotEquals(usage1, usage3);
    assertEquals(usage1.hashCode(), usage2.hashCode());
  }

  @Test
  void shouldHandleEmptyChoices() {
    // Given
    ChatResponse response = new ChatResponse();

    // When
    response.setChoices(Arrays.asList());

    // Then
    assertNotNull(response.getChoices());
    assertTrue(response.getChoices().isEmpty());
  }

  @Test
  void shouldHandleNullValues() {
    // Given
    ChatResponse response = new ChatResponse();

    // Then
    assertNull(response.getId());
    assertNull(response.getChoices());
    assertNull(response.getUsage());
  }
}