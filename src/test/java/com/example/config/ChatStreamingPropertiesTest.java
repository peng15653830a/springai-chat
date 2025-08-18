package com.example.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatStreamingProperties测试类
 *
 * @author xupeng
 */
class ChatStreamingPropertiesTest {

  private ChatStreamingProperties properties;

  @BeforeEach
  void setUp() {
    properties = new ChatStreamingProperties();
  }

  @Test
  void shouldCreateWithDefaultValues() {
    // Then
    assertEquals(20, properties.getMaxHistorySize());
    assertEquals(Duration.ofSeconds(300), properties.getResponseTimeout());
    assertEquals(Duration.ofSeconds(300), properties.getSseTimeout());
    assertNotNull(properties.getStreaming());
    assertNotNull(properties.getError());
  }

  @Test
  void shouldSetAndGetMaxHistorySize() {
    // When
    properties.setMaxHistorySize(50);

    // Then
    assertEquals(50, properties.getMaxHistorySize());
  }

  @Test
  void shouldSetAndGetResponseTimeout() {
    // Given
    Duration timeout = Duration.ofMinutes(10);

    // When
    properties.setResponseTimeout(timeout);

    // Then
    assertEquals(timeout, properties.getResponseTimeout());
  }

  @Test
  void shouldSetAndGetSseTimeout() {
    // Given
    Duration timeout = Duration.ofMinutes(5);

    // When
    properties.setSseTimeout(timeout);

    // Then
    assertEquals(timeout, properties.getSseTimeout());
  }

  @Test
  void shouldSetAndGetStreamingConfig() {
    // Given
    ChatStreamingProperties.Streaming streaming = new ChatStreamingProperties.Streaming();
    streaming.setChunkSize(100);

    // When
    properties.setStreaming(streaming);

    // Then
    assertEquals(streaming, properties.getStreaming());
    assertEquals(100, properties.getStreaming().getChunkSize());
  }

  @Test
  void shouldSetAndGetErrorConfig() {
    // Given
    ChatStreamingProperties.Error error = new ChatStreamingProperties.Error();
    error.setRetryAttempts(5);

    // When
    properties.setError(error);

    // Then
    assertEquals(error, properties.getError());
    assertEquals(5, properties.getError().getRetryAttempts());
  }

  @Test
  void shouldTestStreamingDefaultValues() {
    // Given
    ChatStreamingProperties.Streaming streaming = new ChatStreamingProperties.Streaming();

    // Then
    assertEquals(50, streaming.getChunkSize());
    assertEquals(Duration.ofMillis(100), streaming.getBufferTimeout());
    assertEquals(Duration.ofSeconds(30), streaming.getHeartbeatInterval());
  }

  @Test
  void shouldSetStreamingChunkSize() {
    // Given
    ChatStreamingProperties.Streaming streaming = new ChatStreamingProperties.Streaming();

    // When
    streaming.setChunkSize(25);

    // Then
    assertEquals(25, streaming.getChunkSize());
  }

  @Test
  void shouldSetStreamingBufferTimeout() {
    // Given
    ChatStreamingProperties.Streaming streaming = new ChatStreamingProperties.Streaming();
    Duration timeout = Duration.ofMillis(200);

    // When
    streaming.setBufferTimeout(timeout);

    // Then
    assertEquals(timeout, streaming.getBufferTimeout());
  }

  @Test
  void shouldSetStreamingHeartbeatInterval() {
    // Given
    ChatStreamingProperties.Streaming streaming = new ChatStreamingProperties.Streaming();
    Duration interval = Duration.ofSeconds(60);

    // When
    streaming.setHeartbeatInterval(interval);

    // Then
    assertEquals(interval, streaming.getHeartbeatInterval());
  }

  @Test
  void shouldTestErrorDefaultValues() {
    // Given
    ChatStreamingProperties.Error error = new ChatStreamingProperties.Error();

    // Then
    assertEquals(3, error.getRetryAttempts());
    assertEquals(Duration.ofMillis(1000), error.getRetryDelay());
  }

  @Test
  void shouldSetErrorRetryAttempts() {
    // Given
    ChatStreamingProperties.Error error = new ChatStreamingProperties.Error();

    // When
    error.setRetryAttempts(5);

    // Then
    assertEquals(5, error.getRetryAttempts());
  }

  @Test
  void shouldSetErrorRetryDelay() {
    // Given
    ChatStreamingProperties.Error error = new ChatStreamingProperties.Error();
    Duration delay = Duration.ofMillis(2000);

    // When
    error.setRetryDelay(delay);

    // Then
    assertEquals(delay, error.getRetryDelay());
  }

  @Test
  void shouldTestPropertiesEqualsAndHashCode() {
    // Given
    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setMaxHistorySize(30);

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setMaxHistorySize(30);

    ChatStreamingProperties props3 = new ChatStreamingProperties();
    props3.setMaxHistorySize(40);

    // Then
    assertEquals(props1, props2);
    assertNotEquals(props1, props3);
    assertEquals(props1.hashCode(), props2.hashCode());
  }

  @Test
  void shouldTestPropertiesToString() {
    // Given
    properties.setMaxHistorySize(25);

    // When
    String toString = properties.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("25"));
  }

  @Test
  void shouldTestStreamingEqualsAndHashCode() {
    // Given
    ChatStreamingProperties.Streaming streaming1 = new ChatStreamingProperties.Streaming();
    streaming1.setChunkSize(75);

    ChatStreamingProperties.Streaming streaming2 = new ChatStreamingProperties.Streaming();
    streaming2.setChunkSize(75);

    ChatStreamingProperties.Streaming streaming3 = new ChatStreamingProperties.Streaming();
    streaming3.setChunkSize(100);

    // Then
    assertEquals(streaming1, streaming2);
    assertNotEquals(streaming1, streaming3);
    assertEquals(streaming1.hashCode(), streaming2.hashCode());
  }

  @Test
  void shouldTestStreamingToString() {
    // Given
    ChatStreamingProperties.Streaming streaming = new ChatStreamingProperties.Streaming();
    streaming.setChunkSize(75);

    // When
    String toString = streaming.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("75"));
  }

  @Test
  void shouldTestErrorEqualsAndHashCode() {
    // Given
    ChatStreamingProperties.Error error1 = new ChatStreamingProperties.Error();
    error1.setRetryAttempts(7);

    ChatStreamingProperties.Error error2 = new ChatStreamingProperties.Error();
    error2.setRetryAttempts(7);

    ChatStreamingProperties.Error error3 = new ChatStreamingProperties.Error();
    error3.setRetryAttempts(10);

    // Then
    assertEquals(error1, error2);
    assertNotEquals(error1, error3);
    assertEquals(error1.hashCode(), error2.hashCode());
  }

  @Test
  void shouldTestErrorToString() {
    // Given
    ChatStreamingProperties.Error error = new ChatStreamingProperties.Error();
    error.setRetryAttempts(7);

    // When
    String toString = error.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("7"));
  }

  @Test
  void shouldHandleNullValues() {
    // When
    properties.setResponseTimeout(null);
    properties.setSseTimeout(null);
    properties.setStreaming(null);
    properties.setError(null);

    // Then
    assertNull(properties.getResponseTimeout());
    assertNull(properties.getSseTimeout());
    assertNull(properties.getStreaming());
    assertNull(properties.getError());
  }

  @Test
  void shouldHandleZeroAndNegativeValues() {
    // When
    properties.setMaxHistorySize(0);

    // Then
    assertEquals(0, properties.getMaxHistorySize());

    // When
    properties.setMaxHistorySize(-1);

    // Then
    assertEquals(-1, properties.getMaxHistorySize());
  }

  @Test
  void shouldSetStreamingWithZeroValues() {
    // Given
    ChatStreamingProperties.Streaming streaming = properties.getStreaming();

    // When
    streaming.setChunkSize(0);

    // Then
    assertEquals(0, streaming.getChunkSize());
  }

  @Test
  void shouldSetErrorWithZeroValues() {
    // Given
    ChatStreamingProperties.Error error = properties.getError();

    // When
    error.setRetryAttempts(0);

    // Then
    assertEquals(0, error.getRetryAttempts());
  }
}