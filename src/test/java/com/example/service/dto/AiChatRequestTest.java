package com.example.service.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AiChatRequest测试
 *
 * @author xupeng
 */
class AiChatRequestTest {

  @Test
  void shouldCreateAiChatRequest() {
    // Given
    String model = "gpt-3.5-turbo";
    List<ChatMessage> messages = Arrays.asList(
        new ChatMessage("user", "Hello"),
        new ChatMessage("assistant", "Hi there!")
    );
    boolean stream = true;
    double temperature = 0.7;
    int maxTokens = 1000;

    // When
    AiChatRequest request = new AiChatRequest();
    request.setModel(model);
    request.setMessages(messages);
    request.setStream(stream);
    request.setTemperature(temperature);
    request.setMaxTokens(maxTokens);

    // Then
    assertEquals(model, request.getModel());
    assertEquals(messages, request.getMessages());
    assertTrue(request.isStream());
    assertEquals(temperature, request.getTemperature());
    assertEquals(maxTokens, request.getMaxTokens());
  }

  @Test
  void shouldCreateWithAllArgsConstructor() {
    // Given
    String model = "test-model";
    List<ChatMessage> messages = Arrays.asList(new ChatMessage("user", "test"));
    boolean stream = false;
    double temperature = 0.5;
    int maxTokens = 500;

    // When
    AiChatRequest request = new AiChatRequest(model, messages, temperature, maxTokens, stream);

    // Then
    assertEquals(model, request.getModel());
    assertEquals(messages, request.getMessages());
    assertFalse(request.isStream());
    assertEquals(temperature, request.getTemperature());
    assertEquals(maxTokens, request.getMaxTokens());
  }

  @Test
  void shouldCreateEmptyRequest() {
    // When
    AiChatRequest request = new AiChatRequest();

    // Then
    assertNull(request.getModel());
    assertNull(request.getMessages());
    assertFalse(request.isStream()); // default value
    assertEquals(0.0, request.getTemperature());
    assertEquals(0, request.getMaxTokens());
  }

  @Test
  void shouldTestEquality() {
    // Given
    List<ChatMessage> messages = Arrays.asList(new ChatMessage("user", "hello"));
    
    AiChatRequest request1 = new AiChatRequest("model1", messages, 0.7, 1000, true);
    AiChatRequest request2 = new AiChatRequest("model1", messages, 0.7, 1000, true);
    AiChatRequest request3 = new AiChatRequest("model2", messages, 0.7, 1000, true);

    // Then
    assertEquals(request1, request2);
    assertNotEquals(request1, request3);
    assertEquals(request1.hashCode(), request2.hashCode());
  }

  @Test
  void shouldTestToString() {
    // Given
    AiChatRequest request = new AiChatRequest();
    request.setModel("test-model");
    request.setStream(true);
    request.setTemperature(0.8);
    request.setMaxTokens(1500);

    // When
    String toString = request.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("test-model"));
    assertTrue(toString.contains("true"));
    assertTrue(toString.contains("0.8"));
    assertTrue(toString.contains("1500"));
  }

  @Test
  void shouldHandleEmptyMessages() {
    // Given
    AiChatRequest request = new AiChatRequest();

    // When
    request.setMessages(Arrays.asList());

    // Then
    assertNotNull(request.getMessages());
    assertTrue(request.getMessages().isEmpty());
  }

  @Test
  void shouldHandleNullMessages() {
    // Given
    AiChatRequest request = new AiChatRequest();

    // When
    request.setMessages(null);

    // Then
    assertNull(request.getMessages());
  }

  @Test
  void shouldSetAndGetAllProperties() {
    // Given
    AiChatRequest request = new AiChatRequest();
    String model = "custom-model";
    List<ChatMessage> messages = Arrays.asList(
        new ChatMessage("system", "You are a helpful assistant"),
        new ChatMessage("user", "What is AI?")
    );
    boolean stream = true;
    double temperature = 0.9;
    int maxTokens = 2000;

    // When
    request.setModel(model);
    request.setMessages(messages);
    request.setStream(stream);
    request.setTemperature(temperature);
    request.setMaxTokens(maxTokens);

    // Then
    assertEquals(model, request.getModel());
    assertEquals(2, request.getMessages().size());
    assertEquals("system", request.getMessages().get(0).getRole());
    assertEquals("You are a helpful assistant", request.getMessages().get(0).getContent());
    assertTrue(request.isStream());
    assertEquals(temperature, request.getTemperature());
    assertEquals(maxTokens, request.getMaxTokens());
  }

  @Test
  void shouldCompareTemperatureValues() {
    // Given
    AiChatRequest request1 = new AiChatRequest();
    request1.setTemperature(0.5);
    
    AiChatRequest request2 = new AiChatRequest();
    request2.setTemperature(0.8);

    // Then
    assertTrue(request2.getTemperature() > request1.getTemperature());
  }

  @Test
  void shouldCompareMaxTokensValues() {
    // Given
    AiChatRequest request1 = new AiChatRequest();
    request1.setMaxTokens(500);
    
    AiChatRequest request2 = new AiChatRequest();
    request2.setMaxTokens(1000);

    // Then
    assertTrue(request2.getMaxTokens() > request1.getMaxTokens());
  }
}