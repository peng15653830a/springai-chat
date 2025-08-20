package com.example.service.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatResponse测试类
 *
 * @author xupeng
 */
class ChatResponseTest {

  @Test
  void shouldCreateChatResponse() {
    // Given
    String id = "chatcmpl-123";
    String object = "chat.completion";
    Long created = System.currentTimeMillis();
    String model = "gpt-4";

    // When
    ChatResponse response = new ChatResponse();
    response.setId(id);
    response.setObject(object);
    response.setCreated(created);
    response.setModel(model);

    // Then
    assertEquals(id, response.getId());
    assertEquals(object, response.getObject());
    assertEquals(created, response.getCreated());
    assertEquals(model, response.getModel());
  }

  @Test
  void shouldCreateChoiceWithMessage() {
    // Given
    ChatResponse.ResponseMessage message = new ChatResponse.ResponseMessage();
    message.setRole("assistant");
    message.setContent("Hello! How can I help you?");

    ChatResponse.Choice choice = new ChatResponse.Choice();
    choice.setIndex(0);
    choice.setMessage(message);
    choice.setFinishReason("stop");

    // When & Then
    assertEquals(0, choice.getIndex());
    assertEquals(message, choice.getMessage());
    assertEquals("stop", choice.getFinishReason());
    assertEquals("assistant", choice.getMessage().getRole());
    assertEquals("Hello! How can I help you?", choice.getMessage().getContent());
  }

  @Test
  void shouldCreateUsageInformation() {
    // Given
    ChatResponse.Usage usage = new ChatResponse.Usage();
    usage.setPromptTokens(50);
    usage.setCompletionTokens(30);
    usage.setTotalTokens(80);

    // When & Then
    assertEquals(50, usage.getPromptTokens());
    assertEquals(30, usage.getCompletionTokens());
    assertEquals(80, usage.getTotalTokens());
  }

  @Test
  void shouldCreateCompleteResponse() {
    // Given
    ChatResponse.ResponseMessage message = new ChatResponse.ResponseMessage();
    message.setRole("assistant");
    message.setContent("This is a test response");

    ChatResponse.Choice choice = new ChatResponse.Choice();
    choice.setIndex(0);
    choice.setMessage(message);
    choice.setFinishReason("stop");

    ChatResponse.Usage usage = new ChatResponse.Usage();
    usage.setPromptTokens(100);
    usage.setCompletionTokens(50);
    usage.setTotalTokens(150);

    ChatResponse response = new ChatResponse();
    response.setId("chatcmpl-test");
    response.setObject("chat.completion");
    response.setCreated(System.currentTimeMillis());
    response.setModel("gpt-4");
    response.setChoices(Arrays.asList(choice));
    response.setUsage(usage);

    // When & Then
    assertNotNull(response.getId());
    assertEquals("chat.completion", response.getObject());
    assertEquals("gpt-4", response.getModel());
    assertEquals(1, response.getChoices().size());
    assertEquals(choice, response.getChoices().get(0));
    assertEquals(usage, response.getUsage());
  }

  @Test
  void shouldHandleMultipleChoices() {
    // Given
    ChatResponse.Choice choice1 = new ChatResponse.Choice();
    choice1.setIndex(0);
    choice1.setFinishReason("stop");

    ChatResponse.Choice choice2 = new ChatResponse.Choice();
    choice2.setIndex(1);
    choice2.setFinishReason("length");

    List<ChatResponse.Choice> choices = Arrays.asList(choice1, choice2);

    ChatResponse response = new ChatResponse();
    response.setChoices(choices);

    // When & Then
    assertEquals(2, response.getChoices().size());
    assertEquals(0, response.getChoices().get(0).getIndex());
    assertEquals(1, response.getChoices().get(1).getIndex());
    assertEquals("stop", response.getChoices().get(0).getFinishReason());
    assertEquals("length", response.getChoices().get(1).getFinishReason());
  }

  @Test
  void shouldTestEqualsAndHashCode() {
    // Given
    ChatResponse response1 = new ChatResponse();
    response1.setId("test-id");
    response1.setModel("gpt-4");

    ChatResponse response2 = new ChatResponse();
    response2.setId("test-id");
    response2.setModel("gpt-4");

    ChatResponse response3 = new ChatResponse();
    response3.setId("different-id");
    response3.setModel("gpt-4");

    // Then
    assertEquals(response1, response2);
    assertNotEquals(response1, response3);
    assertEquals(response1.hashCode(), response2.hashCode());
  }

  @Test
  void shouldTestToString() {
    // Given
    ChatResponse response = new ChatResponse();
    response.setId("test-id");
    response.setModel("gpt-4");

    // When
    String toString = response.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("test-id"));
    assertTrue(toString.contains("gpt-4"));
  }

  @Test
  void shouldTestMessageEqualsAndHashCode() {
    // Given
    ChatResponse.ResponseMessage message1 = new ChatResponse.ResponseMessage();
    message1.setRole("assistant");
    message1.setContent("Hello");

    ChatResponse.ResponseMessage message2 = new ChatResponse.ResponseMessage();
    message2.setRole("assistant");
    message2.setContent("Hello");

    ChatResponse.ResponseMessage message3 = new ChatResponse.ResponseMessage();
    message3.setRole("user");
    message3.setContent("Hello");

    // Then
    assertEquals(message1, message2);
    assertNotEquals(message1, message3);
    assertEquals(message1.hashCode(), message2.hashCode());
  }

  @Test
  void shouldTestChoiceEqualsAndHashCode() {
    // Given
    ChatResponse.Choice choice1 = new ChatResponse.Choice();
    choice1.setIndex(0);
    choice1.setFinishReason("stop");

    ChatResponse.Choice choice2 = new ChatResponse.Choice();
    choice2.setIndex(0);
    choice2.setFinishReason("stop");

    ChatResponse.Choice choice3 = new ChatResponse.Choice();
    choice3.setIndex(1);
    choice3.setFinishReason("stop");

    // Then
    assertEquals(choice1, choice2);
    assertNotEquals(choice1, choice3);
    assertEquals(choice1.hashCode(), choice2.hashCode());
  }

  @Test
  void shouldTestUsageEqualsAndHashCode() {
    // Given
    ChatResponse.Usage usage1 = new ChatResponse.Usage();
    usage1.setPromptTokens(100);
    usage1.setTotalTokens(150);

    ChatResponse.Usage usage2 = new ChatResponse.Usage();
    usage2.setPromptTokens(100);
    usage2.setTotalTokens(150);

    ChatResponse.Usage usage3 = new ChatResponse.Usage();
    usage3.setPromptTokens(200);
    usage3.setTotalTokens(250);

    // Then
    assertEquals(usage1, usage2);
    assertNotEquals(usage1, usage3);
    assertEquals(usage1.hashCode(), usage2.hashCode());
  }

  @Test
  void shouldHandleNullValues() {
    // Given
    ChatResponse response = new ChatResponse();
    response.setId(null);
    response.setChoices(null);
    response.setUsage(null);

    // When & Then
    assertNull(response.getId());
    assertNull(response.getChoices());
    assertNull(response.getUsage());
  }

  @Test
  void shouldHandleEmptyChoicesList() {
    // Given
    ChatResponse response = new ChatResponse();
    response.setChoices(Arrays.asList());

    // When & Then
    assertNotNull(response.getChoices());
    assertTrue(response.getChoices().isEmpty());
  }

  @Test
  void shouldHandleNullModel() {
    // Given
    ChatResponse response = new ChatResponse();

    // When
    response.setModel(null);

    // Then
    assertNull(response.getModel());
  }

  @Test
  void shouldHandleComplexMessageContent() {
    // Given
    ChatResponse.ResponseMessage message = new ChatResponse.ResponseMessage();
    String complexContent = "这是一个包含中文、英文和特殊符号！@#$%^&*()的复杂消息内容。\nIt also contains\nmultiple lines.";

    // When
    message.setContent(complexContent);

    // Then
    assertEquals(complexContent, message.getContent());
    assertTrue(message.getContent().contains("中文"));
    assertTrue(message.getContent().contains("multiple lines"));
  }

  @Test
  void shouldTestResponseMessageWithAllFields() {
    // Given
    ChatResponse.ResponseMessage message = new ChatResponse.ResponseMessage();
    message.setRole("assistant");
    message.setContent("Content");
    message.setThinking("Thinking");
    message.setReasoningContent("Reasoning");

    // When & Then
    assertEquals("assistant", message.getRole());
    assertEquals("Content", message.getContent());
    assertEquals("Thinking", message.getThinking());
    assertEquals("Reasoning", message.getReasoningContent());
  }

  @Test
  void shouldTestResponseMessageEqualsWithDifferentThinking() {
    // Given
    ChatResponse.ResponseMessage message1 = new ChatResponse.ResponseMessage();
    message1.setRole("assistant");
    message1.setContent("Same");
    message1.setThinking("Thinking1");

    ChatResponse.ResponseMessage message2 = new ChatResponse.ResponseMessage();
    message2.setRole("assistant");
    message2.setContent("Same");
    message2.setThinking("Thinking2");

    // Then
    assertNotEquals(message1, message2);
  }

  @Test
  void shouldTestResponseMessageEqualsWithDifferentReasoning() {
    // Given
    ChatResponse.ResponseMessage message1 = new ChatResponse.ResponseMessage();
    message1.setRole("assistant");
    message1.setReasoningContent("Reasoning1");

    ChatResponse.ResponseMessage message2 = new ChatResponse.ResponseMessage();
    message2.setRole("assistant");
    message2.setReasoningContent("Reasoning2");

    // Then
    assertNotEquals(message1, message2);
  }

  @Test
  void shouldTestChoiceWithDelta() {
    // Given
    ChatResponse.ResponseMessage delta = new ChatResponse.ResponseMessage();
    delta.setContent("Delta content");

    ChatResponse.Choice choice = new ChatResponse.Choice();
    choice.setIndex(0);
    choice.setDelta(delta);
    choice.setFinishReason("stop");

    // When & Then
    assertEquals(delta, choice.getDelta());
    assertEquals("Delta content", choice.getDelta().getContent());
  }

  @Test
  void shouldTestChoiceEqualsWithDifferentDelta() {
    // Given
    ChatResponse.ResponseMessage delta1 = new ChatResponse.ResponseMessage();
    delta1.setContent("Delta1");

    ChatResponse.ResponseMessage delta2 = new ChatResponse.ResponseMessage();
    delta2.setContent("Delta2");

    ChatResponse.Choice choice1 = new ChatResponse.Choice();
    choice1.setIndex(0);
    choice1.setDelta(delta1);

    ChatResponse.Choice choice2 = new ChatResponse.Choice();
    choice2.setIndex(0);
    choice2.setDelta(delta2);

    // Then
    assertNotEquals(choice1, choice2);
  }

  @Test
  void shouldTestUsageEqualsWithDifferentCompletionTokens() {
    // Given
    ChatResponse.Usage usage1 = new ChatResponse.Usage();
    usage1.setPromptTokens(100);
    usage1.setCompletionTokens(50);
    usage1.setTotalTokens(150);

    ChatResponse.Usage usage2 = new ChatResponse.Usage();
    usage2.setPromptTokens(100);
    usage2.setCompletionTokens(60);
    usage2.setTotalTokens(160);

    // Then
    assertNotEquals(usage1, usage2);
  }

  @Test
  void shouldTestChatResponseEqualsWithNullFields() {
    // Given
    ChatResponse response1 = new ChatResponse();
    response1.setId("test");

    ChatResponse response2 = new ChatResponse();
    response2.setId("test");
    response2.setUsage(null);

    // Then
    assertEquals(response1, response2); // 两个都是null usage
  }

  @Test
  void shouldTestChatResponseHashCodeConsistency() {
    // Given
    ChatResponse response = new ChatResponse();
    response.setId("test");
    response.setModel("gpt-4");

    int hashCode1 = response.hashCode();
    int hashCode2 = response.hashCode();

    // Then
    assertEquals(hashCode1, hashCode2);
  }

  @Test
  void shouldTestResponseMessageToString() {
    // Given
    ChatResponse.ResponseMessage message = new ChatResponse.ResponseMessage();
    message.setRole("assistant");
    message.setContent("Test content");

    // When
    String toString = message.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("assistant"));
    assertTrue(toString.contains("Test content"));
  }

  @Test
  void shouldTestChoiceToString() {
    // Given
    ChatResponse.Choice choice = new ChatResponse.Choice();
    choice.setIndex(0);
    choice.setFinishReason("stop");

    // When
    String toString = choice.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("0"));
    assertTrue(toString.contains("stop"));
  }

  @Test
  void shouldTestUsageToString() {
    // Given
    ChatResponse.Usage usage = new ChatResponse.Usage();
    usage.setPromptTokens(100);
    usage.setTotalTokens(150);

    // When
    String toString = usage.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("100"));
    assertTrue(toString.contains("150"));
  }

  @Test
  void shouldTestChatResponseEqualsWithDifferentObject() {
    // Given
    ChatResponse response1 = new ChatResponse();
    response1.setId("test");
    response1.setObject("chat.completion");
    
    ChatResponse response2 = new ChatResponse();
    response2.setId("test");
    response2.setObject("text.completion");
    
    // Then
    assertNotEquals(response1, response2);
  }

  @Test
  void shouldTestChatResponseEqualsWithNullObject() {
    // Given
    ChatResponse response1 = new ChatResponse();
    response1.setId("test");
    
    ChatResponse response2 = new ChatResponse();
    response2.setId("test");
    
    // Then
    assertEquals(response1, response2); // 两个都是null object
  }

  @Test
  void shouldTestChatResponseEqualsWithDifferentCreated() {
    // Given
    ChatResponse response1 = new ChatResponse();
    response1.setId("test");
    response1.setCreated(1000L);
    
    ChatResponse response2 = new ChatResponse();
    response2.setId("test");
    response2.setCreated(2000L);
    
    // Then
    assertNotEquals(response1, response2);
  }

  @Test
  void shouldTestChatResponseEqualsWithNullCreated() {
    // Given
    ChatResponse response1 = new ChatResponse();
    response1.setId("test");
    
    ChatResponse response2 = new ChatResponse();
    response2.setId("test");
    
    // Then
    assertEquals(response1, response2); // 两个都是null created
  }

  @Test
  void shouldTestChatResponseEqualsWithDifferentChoices() {
    // Given
    ChatResponse response1 = new ChatResponse();
    response1.setId("test");
    response1.setChoices(Arrays.asList(new ChatResponse.Choice()));
    
    ChatResponse response2 = new ChatResponse();
    response2.setId("test");
    response2.setChoices(Arrays.asList());
    
    // Then
    assertNotEquals(response1, response2);
  }

  @Test
  void shouldTestChatResponseEqualsWithNullChoices() {
    // Given
    ChatResponse response1 = new ChatResponse();
    response1.setId("test");
    
    ChatResponse response2 = new ChatResponse();
    response2.setId("test");
    
    // Then
    assertEquals(response1, response2); // 两个都是null choices
  }

  @Test
  void shouldTestChatResponseEqualsWithDifferentUsage() {
    // Given
    ChatResponse.Usage usage1 = new ChatResponse.Usage();
    usage1.setTotalTokens(100);
    
    ChatResponse.Usage usage2 = new ChatResponse.Usage();
    usage2.setTotalTokens(200);
    
    ChatResponse response1 = new ChatResponse();
    response1.setId("test");
    response1.setUsage(usage1);
    
    ChatResponse response2 = new ChatResponse();
    response2.setId("test");
    response2.setUsage(usage2);
    
    // Then
    assertNotEquals(response1, response2);
  }

  @Test
  void shouldTestChoiceEqualsWithNullIndex() {
    // Given
    ChatResponse.Choice choice1 = new ChatResponse.Choice();
    choice1.setFinishReason("stop");
    
    ChatResponse.Choice choice2 = new ChatResponse.Choice();
    choice2.setFinishReason("stop");
    
    // Then
    assertEquals(choice1, choice2); // 两个都是null index
  }

  @Test
  void shouldTestChoiceEqualsWithDifferentMessage() {
    // Given
    ChatResponse.ResponseMessage message1 = new ChatResponse.ResponseMessage();
    message1.setContent("content1");
    
    ChatResponse.ResponseMessage message2 = new ChatResponse.ResponseMessage();
    message2.setContent("content2");
    
    ChatResponse.Choice choice1 = new ChatResponse.Choice();
    choice1.setIndex(0);
    choice1.setMessage(message1);
    
    ChatResponse.Choice choice2 = new ChatResponse.Choice();
    choice2.setIndex(0);
    choice2.setMessage(message2);
    
    // Then
    assertNotEquals(choice1, choice2);
  }

  @Test
  void shouldTestChoiceEqualsWithNullMessage() {
    // Given
    ChatResponse.Choice choice1 = new ChatResponse.Choice();
    choice1.setIndex(0);
    
    ChatResponse.Choice choice2 = new ChatResponse.Choice();
    choice2.setIndex(0);
    
    // Then
    assertEquals(choice1, choice2); // 两个都是null message
  }

  @Test
  void shouldTestChoiceEqualsWithDifferentFinishReason() {
    // Given
    ChatResponse.Choice choice1 = new ChatResponse.Choice();
    choice1.setIndex(0);
    choice1.setFinishReason("stop");
    
    ChatResponse.Choice choice2 = new ChatResponse.Choice();
    choice2.setIndex(0);
    choice2.setFinishReason("length");
    
    // Then
    assertNotEquals(choice1, choice2);
  }

  @Test
  void shouldTestChoiceEqualsWithNullFinishReason() {
    // Given
    ChatResponse.Choice choice1 = new ChatResponse.Choice();
    choice1.setIndex(0);
    
    ChatResponse.Choice choice2 = new ChatResponse.Choice();
    choice2.setIndex(0);
    
    // Then
    assertEquals(choice1, choice2); // 两个都是null finishReason
  }

  @Test
  void shouldTestResponseMessageEqualsWithNullRole() {
    // Given
    ChatResponse.ResponseMessage message1 = new ChatResponse.ResponseMessage();
    message1.setContent("content");
    
    ChatResponse.ResponseMessage message2 = new ChatResponse.ResponseMessage();
    message2.setContent("content");
    
    // Then
    assertEquals(message1, message2); // 两个都是null role
  }

  @Test
  void shouldTestResponseMessageEqualsWithDifferentContent() {
    // Given
    ChatResponse.ResponseMessage message1 = new ChatResponse.ResponseMessage();
    message1.setRole("assistant");
    message1.setContent("content1");
    
    ChatResponse.ResponseMessage message2 = new ChatResponse.ResponseMessage();
    message2.setRole("assistant");
    message2.setContent("content2");
    
    // Then
    assertNotEquals(message1, message2);
  }

  @Test
  void shouldTestResponseMessageEqualsWithNullContent() {
    // Given
    ChatResponse.ResponseMessage message1 = new ChatResponse.ResponseMessage();
    message1.setRole("assistant");
    
    ChatResponse.ResponseMessage message2 = new ChatResponse.ResponseMessage();
    message2.setRole("assistant");
    
    // Then
    assertEquals(message1, message2); // 两个都是null content
  }

  @Test
  void shouldTestUsageEqualsWithNullPromptTokens() {
    // Given
    ChatResponse.Usage usage1 = new ChatResponse.Usage();
    usage1.setTotalTokens(100);
    
    ChatResponse.Usage usage2 = new ChatResponse.Usage();
    usage2.setTotalTokens(100);
    
    // Then
    assertEquals(usage1, usage2); // 两个都是null promptTokens
  }

  @Test
  void shouldTestUsageEqualsWithDifferentPromptTokens() {
    // Given
    ChatResponse.Usage usage1 = new ChatResponse.Usage();
    usage1.setPromptTokens(50);
    usage1.setTotalTokens(100);
    
    ChatResponse.Usage usage2 = new ChatResponse.Usage();
    usage2.setPromptTokens(60);
    usage2.setTotalTokens(100);
    
    // Then
    assertNotEquals(usage1, usage2);
  }

  @Test
  void shouldTestUsageEqualsWithNullTotalTokens() {
    // Given
    ChatResponse.Usage usage1 = new ChatResponse.Usage();
    usage1.setPromptTokens(50);
    
    ChatResponse.Usage usage2 = new ChatResponse.Usage();
    usage2.setPromptTokens(50);
    
    // Then
    assertEquals(usage1, usage2); // 两个都是null totalTokens
  }

  @Test
  void shouldTestUsageEqualsWithDifferentTotalTokens() {
    // Given
    ChatResponse.Usage usage1 = new ChatResponse.Usage();
    usage1.setPromptTokens(50);
    usage1.setTotalTokens(100);
    
    ChatResponse.Usage usage2 = new ChatResponse.Usage();
    usage2.setPromptTokens(50);
    usage2.setTotalTokens(150);
    
    // Then
    assertNotEquals(usage1, usage2);
  }

  @Test
  void shouldTestUsageEqualsWithNullCompletionTokens() {
    // Given
    ChatResponse.Usage usage1 = new ChatResponse.Usage();
    usage1.setPromptTokens(50);
    
    ChatResponse.Usage usage2 = new ChatResponse.Usage();
    usage2.setPromptTokens(50);
    
    // Then
    assertEquals(usage1, usage2); // 两个都是null completionTokens
  }

  @Test
  void shouldCreateWithAllArgsConstructors() {
    // Given
    String id = "test-id";
    String object = "chat.completion";
    Long created = 1234567890L;
    String model = "gpt-4";
    List<ChatResponse.Choice> choices = Arrays.asList(new ChatResponse.Choice());
    ChatResponse.Usage usage = new ChatResponse.Usage();
    
    // When
    ChatResponse response = new ChatResponse(id, object, created, model, choices, usage);
    
    // Then
    assertEquals(id, response.getId());
    assertEquals(object, response.getObject());
    assertEquals(created, response.getCreated());
    assertEquals(model, response.getModel());
    assertEquals(choices, response.getChoices());
    assertEquals(usage, response.getUsage());
  }

  @Test
  void shouldCreateChoiceWithAllArgsConstructor() {
    // Given
    Integer index = 0;
    ChatResponse.ResponseMessage message = new ChatResponse.ResponseMessage();
    ChatResponse.ResponseMessage delta = new ChatResponse.ResponseMessage();
    String finishReason = "stop";
    
    // When
    ChatResponse.Choice choice = new ChatResponse.Choice(index, message, delta, finishReason);
    
    // Then
    assertEquals(index, choice.getIndex());
    assertEquals(message, choice.getMessage());
    assertEquals(delta, choice.getDelta());
    assertEquals(finishReason, choice.getFinishReason());
  }

  @Test
  void shouldCreateResponseMessageWithAllArgsConstructor() {
    // Given
    String role = "assistant";
    String content = "Hello";
    String thinking = "I should help";
    String reasoningContent = "User needs assistance";
    
    // When
    ChatResponse.ResponseMessage message = new ChatResponse.ResponseMessage(role, content, thinking, reasoningContent);
    
    // Then
    assertEquals(role, message.getRole());
    assertEquals(content, message.getContent());
    assertEquals(thinking, message.getThinking());
    assertEquals(reasoningContent, message.getReasoningContent());
  }

  @Test
  void shouldCreateUsageWithAllArgsConstructor() {
    // Given
    Integer promptTokens = 50;
    Integer completionTokens = 30;
    Integer totalTokens = 80;
    
    // When
    ChatResponse.Usage usage = new ChatResponse.Usage(promptTokens, completionTokens, totalTokens);
    
    // Then
    assertEquals(promptTokens, usage.getPromptTokens());
    assertEquals(completionTokens, usage.getCompletionTokens());
    assertEquals(totalTokens, usage.getTotalTokens());
  }

  @Test
  void shouldTestChatResponseEqualsWithSameInstance() {
    // Given
    ChatResponse response = new ChatResponse();
    response.setId("test");
    
    // Then
    assertEquals(response, response); // 同一对象引用
  }

  @Test
  void shouldTestChatResponseEqualsWithNull() {
    // Given
    ChatResponse response = new ChatResponse();
    
    // Then
    assertNotEquals(response, null); // 与null比较
  }

  @Test
  void shouldTestChatResponseEqualsWithDifferentType() {
    // Given
    ChatResponse response = new ChatResponse();
    String differentType = "not a ChatResponse";
    
    // Then
    assertNotEquals(response, differentType); // 不同类型对象
  }

  @Test
  void shouldTestChatResponseEqualsWithCanEqualFalse() {
    // Given
    ChatResponse response1 = new ChatResponse();
    response1.setId("test");
    
    // 创建一个匿名子类来测试canEqual返回false的情况
    ChatResponse response2 = new ChatResponse() {
      @Override
      public boolean canEqual(Object other) {
        return false;
      }
    };
    response2.setId("test");
    
    // Then
    assertNotEquals(response1, response2); // canEqual返回false
  }

  @Test
  void shouldTestChoiceEqualsWithSameInstance() {
    // Given
    ChatResponse.Choice choice = new ChatResponse.Choice();
    choice.setIndex(0);
    
    // Then
    assertEquals(choice, choice); // 同一对象引用
  }

  @Test
  void shouldTestChoiceEqualsWithNull() {
    // Given
    ChatResponse.Choice choice = new ChatResponse.Choice();
    
    // Then
    assertNotEquals(choice, null); // 与null比较
  }

  @Test
  void shouldTestChoiceEqualsWithDifferentType() {
    // Given
    ChatResponse.Choice choice = new ChatResponse.Choice();
    String differentType = "not a Choice";
    
    // Then
    assertNotEquals(choice, differentType); // 不同类型对象
  }

  @Test
  void shouldTestResponseMessageEqualsWithSameInstance() {
    // Given
    ChatResponse.ResponseMessage message = new ChatResponse.ResponseMessage();
    message.setRole("assistant");
    
    // Then
    assertEquals(message, message); // 同一对象引用
  }

  @Test
  void shouldTestResponseMessageEqualsWithNull() {
    // Given
    ChatResponse.ResponseMessage message = new ChatResponse.ResponseMessage();
    
    // Then
    assertNotEquals(message, null); // 与null比较
  }

  @Test
  void shouldTestResponseMessageEqualsWithDifferentType() {
    // Given
    ChatResponse.ResponseMessage message = new ChatResponse.ResponseMessage();
    String differentType = "not a ResponseMessage";
    
    // Then
    assertNotEquals(message, differentType); // 不同类型对象
  }

  @Test
  void shouldTestUsageEqualsWithSameInstance() {
    // Given
    ChatResponse.Usage usage = new ChatResponse.Usage();
    usage.setPromptTokens(100);
    
    // Then
    assertEquals(usage, usage); // 同一对象引用
  }

  @Test
  void shouldTestUsageEqualsWithNull() {
    // Given
    ChatResponse.Usage usage = new ChatResponse.Usage();
    
    // Then
    assertNotEquals(usage, null); // 与null比较
  }

  @Test
  void shouldTestUsageEqualsWithDifferentType() {
    // Given
    ChatResponse.Usage usage = new ChatResponse.Usage();
    String differentType = "not a Usage";
    
    // Then
    assertNotEquals(usage, differentType); // 不同类型对象
  }

  @Test
  void shouldTestHashCodeWithNullFields() {
    // Given
    ChatResponse response = new ChatResponse();
    // 所有字段都是null
    
    // When
    int hashCode = response.hashCode();
    
    // Then
    assertNotNull(hashCode); // hashCode方法应该处理null字段
  }

  @Test
  void shouldTestChoiceHashCodeWithNullFields() {
    // Given
    ChatResponse.Choice choice = new ChatResponse.Choice();
    // 所有字段都是null
    
    // When
    int hashCode = choice.hashCode();
    
    // Then
    assertNotNull(hashCode); // hashCode方法应该处理null字段
  }

  @Test
  void shouldTestResponseMessageHashCodeWithNullFields() {
    // Given
    ChatResponse.ResponseMessage message = new ChatResponse.ResponseMessage();
    // 所有字段都是null
    
    // When
    int hashCode = message.hashCode();
    
    // Then
    assertNotNull(hashCode); // hashCode方法应该处理null字段
  }

  @Test
  void shouldTestUsageHashCodeWithNullFields() {
    // Given
    ChatResponse.Usage usage = new ChatResponse.Usage();
    // 所有字段都是null
    
    // When
    int hashCode = usage.hashCode();
    
    // Then
    assertNotNull(hashCode); // hashCode方法应该处理null字段
  }

  @Test
  void shouldTestChatResponseEqualsWithOneNullId() {
    // Given
    ChatResponse response1 = new ChatResponse();
    response1.setId(null);
    response1.setModel("gpt-4");
    
    ChatResponse response2 = new ChatResponse();
    response2.setId("test-id");
    response2.setModel("gpt-4");
    
    // Then
    assertNotEquals(response1, response2); // 一个id为null，一个不为null
  }

  @Test
  void shouldTestChatResponseEqualsWithOneNullModel() {
    // Given
    ChatResponse response1 = new ChatResponse();
    response1.setId("test");
    response1.setModel(null);
    
    ChatResponse response2 = new ChatResponse();
    response2.setId("test");
    response2.setModel("gpt-4");
    
    // Then
    assertNotEquals(response1, response2); // 一个model为null，一个不为null
  }
}