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

  @Test
  void shouldCreateUsingStaticFactory() {
    // Given
    String model = "factory-model";
    List<ChatMessage> messages = Arrays.asList(new ChatMessage("user", "Factory test"));
    double temperature = 0.6;
    int maxTokens = 800;
    boolean stream = true;

    // When
    AiChatRequest request = AiChatRequest.create(model, messages, temperature, maxTokens, stream);

    // Then
    assertEquals(model, request.getModel());
    assertEquals(messages, request.getMessages());
    assertEquals(temperature, request.getTemperature());
    assertEquals(maxTokens, request.getMaxTokens());
    assertTrue(request.isStream());
  }

  @Test
  void shouldCreateUsingStaticFactoryWithFalseStream() {
    // Given
    String model = "non-stream-model";
    List<ChatMessage> messages = Arrays.asList(new ChatMessage("assistant", "No streaming"));
    double temperature = 0.2;
    int maxTokens = 300;
    boolean stream = false;

    // When
    AiChatRequest request = AiChatRequest.create(model, messages, temperature, maxTokens, stream);

    // Then
    assertEquals(model, request.getModel());
    assertEquals(messages, request.getMessages());
    assertEquals(temperature, request.getTemperature());
    assertEquals(maxTokens, request.getMaxTokens());
    assertFalse(request.isStream());
  }

  @Test
  void shouldTestEqualityWithNullModel() {
    // Given
    List<ChatMessage> messages = Arrays.asList(new ChatMessage("user", "test"));
    
    AiChatRequest request1 = new AiChatRequest();
    request1.setMessages(messages);
    request1.setTemperature(0.5);
    request1.setMaxTokens(100);
    request1.setStream(true);
    
    AiChatRequest request2 = new AiChatRequest();
    request2.setMessages(messages);
    request2.setTemperature(0.5);
    request2.setMaxTokens(100);
    request2.setStream(true);

    // Then
    assertEquals(request1, request2); // 两个都是null model
  }

  @Test
  void shouldTestInequalityWithDifferentModel() {
    // Given
    List<ChatMessage> messages = Arrays.asList(new ChatMessage("user", "test"));
    
    AiChatRequest request1 = new AiChatRequest();
    request1.setModel("model1");
    request1.setMessages(messages);
    request1.setTemperature(0.5);
    request1.setMaxTokens(100);
    request1.setStream(true);
    
    AiChatRequest request2 = new AiChatRequest();
    request2.setModel("model2");
    request2.setMessages(messages);
    request2.setTemperature(0.5);
    request2.setMaxTokens(100);
    request2.setStream(true);

    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualityWithNullMessages() {
    // Given
    AiChatRequest request1 = new AiChatRequest();
    request1.setModel("model1");
    request1.setTemperature(0.5);
    request1.setMaxTokens(100);
    request1.setStream(true);
    
    AiChatRequest request2 = new AiChatRequest();
    request2.setModel("model1");
    request2.setTemperature(0.5);
    request2.setMaxTokens(100);
    request2.setStream(true);

    // Then
    assertEquals(request1, request2); // 两个都是null messages
  }

  @Test
  void shouldTestInequalityWithDifferentMessages() {
    // Given
    AiChatRequest request1 = new AiChatRequest();
    request1.setModel("model1");
    request1.setMessages(Arrays.asList(new ChatMessage("user", "message1")));
    request1.setTemperature(0.5);
    request1.setMaxTokens(100);
    request1.setStream(true);
    
    AiChatRequest request2 = new AiChatRequest();
    request2.setModel("model1");
    request2.setMessages(Arrays.asList(new ChatMessage("user", "message2")));
    request2.setTemperature(0.5);
    request2.setMaxTokens(100);
    request2.setStream(true);

    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestInequalityWithDifferentTemperature() {
    // Given
    List<ChatMessage> messages = Arrays.asList(new ChatMessage("user", "test"));
    
    AiChatRequest request1 = new AiChatRequest();
    request1.setModel("model1");
    request1.setMessages(messages);
    request1.setTemperature(0.5);
    request1.setMaxTokens(100);
    request1.setStream(true);
    
    AiChatRequest request2 = new AiChatRequest();
    request2.setModel("model1");
    request2.setMessages(messages);
    request2.setTemperature(0.8);
    request2.setMaxTokens(100);
    request2.setStream(true);

    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestInequalityWithDifferentMaxTokens() {
    // Given
    List<ChatMessage> messages = Arrays.asList(new ChatMessage("user", "test"));
    
    AiChatRequest request1 = new AiChatRequest();
    request1.setModel("model1");
    request1.setMessages(messages);
    request1.setTemperature(0.5);
    request1.setMaxTokens(100);
    request1.setStream(true);
    
    AiChatRequest request2 = new AiChatRequest();
    request2.setModel("model1");
    request2.setMessages(messages);
    request2.setTemperature(0.5);
    request2.setMaxTokens(200);
    request2.setStream(true);

    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestInequalityWithDifferentStream() {
    // Given
    List<ChatMessage> messages = Arrays.asList(new ChatMessage("user", "test"));
    
    AiChatRequest request1 = new AiChatRequest();
    request1.setModel("model1");
    request1.setMessages(messages);
    request1.setTemperature(0.5);
    request1.setMaxTokens(100);
    request1.setStream(true);
    
    AiChatRequest request2 = new AiChatRequest();
    request2.setModel("model1");
    request2.setMessages(messages);
    request2.setTemperature(0.5);
    request2.setMaxTokens(100);
    request2.setStream(false);

    // Then
    assertNotEquals(request1, request2);
  }

  @Test
  void shouldTestEqualityWithItself() {
    // Given
    AiChatRequest request = new AiChatRequest();
    request.setModel("model1");
    request.setMessages(Arrays.asList(new ChatMessage("user", "test")));
    request.setTemperature(0.5);
    request.setMaxTokens(100);
    request.setStream(true);

    // Then
    assertEquals(request, request);
    assertEquals(request.hashCode(), request.hashCode());
  }

  @Test
  void shouldTestHashCodeConsistency() {
    // Given
    AiChatRequest request = new AiChatRequest("model", 
        Arrays.asList(new ChatMessage("user", "test")), 0.5, 100, true);
    
    int hashCode1 = request.hashCode();
    int hashCode2 = request.hashCode();
    
    // Then
    assertEquals(hashCode1, hashCode2);
  }

  @Test
  void shouldTestToStringWithNullFields() {
    // Given
    AiChatRequest request = new AiChatRequest();
    
    // When
    String toString = request.toString();
    
    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("AiChatRequest"));
  }

  @Test
  void shouldHandleZeroTemperature() {
    // Given
    AiChatRequest request = new AiChatRequest();
    
    // When
    request.setTemperature(0.0);
    
    // Then
    assertEquals(0.0, request.getTemperature());
  }

  @Test
  void shouldHandleHighTemperature() {
    // Given
    AiChatRequest request = new AiChatRequest();
    
    // When
    request.setTemperature(2.0);
    
    // Then
    assertEquals(2.0, request.getTemperature());
  }

  @Test
  void shouldHandleZeroMaxTokens() {
    // Given
    AiChatRequest request = new AiChatRequest();
    
    // When
    request.setMaxTokens(0);
    
    // Then
    assertEquals(0, request.getMaxTokens());
  }

  @Test
  void shouldHandleVeryHighMaxTokens() {
    // Given
    AiChatRequest request = new AiChatRequest();
    
    // When
    request.setMaxTokens(100000);
    
    // Then
    assertEquals(100000, request.getMaxTokens());
  }

  @Test
  void shouldHandleNegativeTemperature() {
    // Given
    AiChatRequest request = new AiChatRequest();
    
    // When
    request.setTemperature(-0.1);
    
    // Then
    assertEquals(-0.1, request.getTemperature());
  }

  @Test
  void shouldHandleNegativeMaxTokens() {
    // Given
    AiChatRequest request = new AiChatRequest();
    
    // When
    request.setMaxTokens(-10);
    
    // Then
    assertEquals(-10, request.getMaxTokens());
  }

  @Test
  void shouldCreateIdenticalRequestsUsingFactoryAndConstructor() {
    // Given
    String model = "test-model";
    List<ChatMessage> messages = Arrays.asList(new ChatMessage("user", "test"));
    double temperature = 0.7;
    int maxTokens = 1000;
    boolean stream = true;
    
    // When
    AiChatRequest factoryRequest = AiChatRequest.create(model, messages, temperature, maxTokens, stream);
    AiChatRequest constructorRequest = new AiChatRequest(model, messages, temperature, maxTokens, stream);
    
    // Then
    assertEquals(factoryRequest, constructorRequest);
    assertEquals(factoryRequest.hashCode(), constructorRequest.hashCode());
  }

  @Test
  void shouldHandleComplexMessages() {
    // Given
    List<ChatMessage> complexMessages = Arrays.asList(
        new ChatMessage("system", "You are a helpful AI assistant specialized in code review"),
        new ChatMessage("user", "Please review this Java code:\npublic class Test {\n  // some code\n}"),
        new ChatMessage("assistant", "The code looks good but could use some improvements"),
        new ChatMessage("user", "What specific improvements?")
    );
    
    // When
    AiChatRequest request = new AiChatRequest();
    request.setMessages(complexMessages);
    
    // Then
    assertEquals(4, request.getMessages().size());
    assertEquals("system", request.getMessages().get(0).getRole());
    assertEquals("user", request.getMessages().get(3).getRole());
    assertTrue(request.getMessages().get(1).getContent().contains("Java code"));
  }
}