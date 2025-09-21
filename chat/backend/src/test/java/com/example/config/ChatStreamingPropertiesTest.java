package com.example.config;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

  @Test
  void shouldTestPropertiesEqualsWithDifferentMaxHistorySize() {
    // Given
    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setMaxHistorySize(20);

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setMaxHistorySize(30);

    // Then
    assertNotEquals(props1, props2);
  }

  @Test
  void shouldTestPropertiesEqualsWithDifferentResponseTimeout() {
    // Given
    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setResponseTimeout(Duration.ofSeconds(300));

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setResponseTimeout(Duration.ofSeconds(600));

    // Then
    assertNotEquals(props1, props2);
  }

  @Test
  void shouldTestPropertiesEqualsWithNullResponseTimeout() {
    // Given
    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setResponseTimeout(null);

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setResponseTimeout(null);

    // Then
    assertEquals(props1, props2);
  }

  @Test
  void shouldTestPropertiesEqualsWithDifferentSseTimeout() {
    // Given
    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setSseTimeout(Duration.ofSeconds(300));

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setSseTimeout(Duration.ofSeconds(600));

    // Then
    assertNotEquals(props1, props2);
  }

  @Test
  void shouldTestPropertiesEqualsWithNullSseTimeout() {
    // Given
    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setSseTimeout(null);

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setSseTimeout(null);

    // Then
    assertEquals(props1, props2);
  }

  @Test
  void shouldTestPropertiesEqualsWithDifferentStreaming() {
    // Given
    ChatStreamingProperties.Streaming streaming1 = new ChatStreamingProperties.Streaming();
    streaming1.setChunkSize(100);

    ChatStreamingProperties.Streaming streaming2 = new ChatStreamingProperties.Streaming();
    streaming2.setChunkSize(200);

    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setStreaming(streaming1);

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setStreaming(streaming2);

    // Then
    assertNotEquals(props1, props2);
  }

  @Test
  void shouldTestPropertiesEqualsWithNullStreaming() {
    // Given
    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setStreaming(null);

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setStreaming(null);

    // Then
    assertEquals(props1, props2);
  }

  @Test
  void shouldTestPropertiesEqualsWithDifferentError() {
    // Given
    ChatStreamingProperties.Error error1 = new ChatStreamingProperties.Error();
    error1.setRetryAttempts(3);

    ChatStreamingProperties.Error error2 = new ChatStreamingProperties.Error();
    error2.setRetryAttempts(5);

    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setError(error1);

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setError(error2);

    // Then
    assertNotEquals(props1, props2);
  }

  @Test
  void shouldTestPropertiesEqualsWithNullError() {
    // Given
    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setError(null);

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setError(null);

    // Then
    assertEquals(props1, props2);
  }

  @Test
  void shouldTestStreamingEqualsWithDifferentChunkSize() {
    // Given
    ChatStreamingProperties.Streaming streaming1 = new ChatStreamingProperties.Streaming();
    streaming1.setChunkSize(50);

    ChatStreamingProperties.Streaming streaming2 = new ChatStreamingProperties.Streaming();
    streaming2.setChunkSize(100);

    // Then
    assertNotEquals(streaming1, streaming2);
  }

  @Test
  void shouldTestStreamingEqualsWithDifferentBufferTimeout() {
    // Given
    ChatStreamingProperties.Streaming streaming1 = new ChatStreamingProperties.Streaming();
    streaming1.setBufferTimeout(Duration.ofMillis(100));

    ChatStreamingProperties.Streaming streaming2 = new ChatStreamingProperties.Streaming();
    streaming2.setBufferTimeout(Duration.ofMillis(200));

    // Then
    assertNotEquals(streaming1, streaming2);
  }

  @Test
  void shouldTestStreamingEqualsWithNullBufferTimeout() {
    // Given
    ChatStreamingProperties.Streaming streaming1 = new ChatStreamingProperties.Streaming();
    streaming1.setBufferTimeout(null);

    ChatStreamingProperties.Streaming streaming2 = new ChatStreamingProperties.Streaming();
    streaming2.setBufferTimeout(null);

    // Then
    assertEquals(streaming1, streaming2);
  }

  @Test
  void shouldTestStreamingEqualsWithDifferentHeartbeatInterval() {
    // Given
    ChatStreamingProperties.Streaming streaming1 = new ChatStreamingProperties.Streaming();
    streaming1.setHeartbeatInterval(Duration.ofSeconds(30));

    ChatStreamingProperties.Streaming streaming2 = new ChatStreamingProperties.Streaming();
    streaming2.setHeartbeatInterval(Duration.ofSeconds(60));

    // Then
    assertNotEquals(streaming1, streaming2);
  }

  @Test
  void shouldTestStreamingEqualsWithNullHeartbeatInterval() {
    // Given
    ChatStreamingProperties.Streaming streaming1 = new ChatStreamingProperties.Streaming();
    streaming1.setHeartbeatInterval(null);

    ChatStreamingProperties.Streaming streaming2 = new ChatStreamingProperties.Streaming();
    streaming2.setHeartbeatInterval(null);

    // Then
    assertEquals(streaming1, streaming2);
  }

  @Test
  void shouldTestErrorEqualsWithDifferentRetryAttempts() {
    // Given
    ChatStreamingProperties.Error error1 = new ChatStreamingProperties.Error();
    error1.setRetryAttempts(3);

    ChatStreamingProperties.Error error2 = new ChatStreamingProperties.Error();
    error2.setRetryAttempts(5);

    // Then
    assertNotEquals(error1, error2);
  }

  @Test
  void shouldTestErrorEqualsWithDifferentRetryDelay() {
    // Given
    ChatStreamingProperties.Error error1 = new ChatStreamingProperties.Error();
    error1.setRetryDelay(Duration.ofMillis(1000));

    ChatStreamingProperties.Error error2 = new ChatStreamingProperties.Error();
    error2.setRetryDelay(Duration.ofMillis(2000));

    // Then
    assertNotEquals(error1, error2);
  }

  @Test
  void shouldTestErrorEqualsWithNullRetryDelay() {
    // Given
    ChatStreamingProperties.Error error1 = new ChatStreamingProperties.Error();
    error1.setRetryDelay(null);

    ChatStreamingProperties.Error error2 = new ChatStreamingProperties.Error();
    error2.setRetryDelay(null);

    // Then
    assertEquals(error1, error2);
  }

  @Test
  void shouldTestPropertiesHashCodeConsistency() {
    // Given
    ChatStreamingProperties props = new ChatStreamingProperties();
    props.setMaxHistorySize(25);

    int hashCode1 = props.hashCode();
    int hashCode2 = props.hashCode();

    // Then
    assertEquals(hashCode1, hashCode2);
  }

  @Test
  void shouldTestStreamingHashCodeConsistency() {
    // Given
    ChatStreamingProperties.Streaming streaming = new ChatStreamingProperties.Streaming();
    streaming.setChunkSize(75);

    int hashCode1 = streaming.hashCode();
    int hashCode2 = streaming.hashCode();

    // Then
    assertEquals(hashCode1, hashCode2);
  }

  @Test
  void shouldTestErrorHashCodeConsistency() {
    // Given
    ChatStreamingProperties.Error error = new ChatStreamingProperties.Error();
    error.setRetryAttempts(5);

    int hashCode1 = error.hashCode();
    int hashCode2 = error.hashCode();

    // Then
    assertEquals(hashCode1, hashCode2);
  }

  @Test
  void shouldTestPropertiesEqualsWithItself() {
    // Given
    ChatStreamingProperties props = new ChatStreamingProperties();

    // Then
    assertEquals(props, props);
  }

  @Test
  void shouldTestStreamingEqualsWithItself() {
    // Given
    ChatStreamingProperties.Streaming streaming = new ChatStreamingProperties.Streaming();

    // Then
    assertEquals(streaming, streaming);
  }

  @Test
  void shouldTestErrorEqualsWithItself() {
    // Given
    ChatStreamingProperties.Error error = new ChatStreamingProperties.Error();

    // Then
    assertEquals(error, error);
  }

  @Test
  void shouldHandleVeryLargeValues() {
    // Given
    ChatStreamingProperties props = new ChatStreamingProperties();

    // When
    props.setMaxHistorySize(Integer.MAX_VALUE);

    // Then
    assertEquals(Integer.MAX_VALUE, props.getMaxHistorySize());
  }

  @Test
  void shouldHandleVerySmallDurations() {
    // Given
    ChatStreamingProperties props = new ChatStreamingProperties();
    Duration verySmall = Duration.ofNanos(1);

    // When
    props.setResponseTimeout(verySmall);

    // Then
    assertEquals(verySmall, props.getResponseTimeout());
  }

  @Test
  void shouldHandleVeryLargeDurations() {
    // Given
    ChatStreamingProperties props = new ChatStreamingProperties();
    Duration veryLarge = Duration.ofDays(365);

    // When
    props.setSseTimeout(veryLarge);

    // Then
    assertEquals(veryLarge, props.getSseTimeout());
  }

  @Test
  void shouldSetStreamingWithNullBufferTimeout() {
    // Given
    ChatStreamingProperties.Streaming streaming = new ChatStreamingProperties.Streaming();

    // When
    streaming.setBufferTimeout(null);

    // Then
    assertNull(streaming.getBufferTimeout());
  }

  @Test
  void shouldSetStreamingWithNullHeartbeatInterval() {
    // Given
    ChatStreamingProperties.Streaming streaming = new ChatStreamingProperties.Streaming();

    // When
    streaming.setHeartbeatInterval(null);

    // Then
    assertNull(streaming.getHeartbeatInterval());
  }

  @Test
  void shouldSetErrorWithNullRetryDelay() {
    // Given
    ChatStreamingProperties.Error error = new ChatStreamingProperties.Error();

    // When
    error.setRetryDelay(null);

    // Then
    assertNull(error.getRetryDelay());
  }

  @Test
  void shouldTestChatStreamingPropertiesEqualsWithNull() {
    ChatStreamingProperties props = new ChatStreamingProperties();
    assertNotEquals(props, null); // Null comparison
  }

  @Test
  void shouldTestChatStreamingPropertiesEqualsWithDifferentType() {
    ChatStreamingProperties props = new ChatStreamingProperties();
    assertNotEquals(props, "not a ChatStreamingProperties"); // Different type
  }

  @Test
  void shouldTestChatStreamingPropertiesEqualsWithCanEqualFalse() {
    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setMaxHistorySize(20);

    // Anonymous subclass that overrides canEqual to return false
    ChatStreamingProperties props2 =
        new ChatStreamingProperties() {
          @Override
          public boolean canEqual(Object other) {
            return false;
          }
        };
    props2.setMaxHistorySize(20);

    assertNotEquals(props1, props2); // canEqual returns false
  }

  @Test
  void shouldTestStreamingEqualsWithNull() {
    ChatStreamingProperties.Streaming streaming = new ChatStreamingProperties.Streaming();
    assertNotEquals(streaming, null); // Null comparison
  }

  @Test
  void shouldTestStreamingEqualsWithDifferentType() {
    ChatStreamingProperties.Streaming streaming = new ChatStreamingProperties.Streaming();
    assertNotEquals(streaming, "not a Streaming"); // Different type
  }

  @Test
  void shouldTestStreamingEqualsWithCanEqualFalse() {
    ChatStreamingProperties.Streaming streaming1 = new ChatStreamingProperties.Streaming();
    streaming1.setChunkSize(50);

    // Anonymous subclass that overrides canEqual to return false
    ChatStreamingProperties.Streaming streaming2 =
        new ChatStreamingProperties.Streaming() {
          @Override
          public boolean canEqual(Object other) {
            return false;
          }
        };
    streaming2.setChunkSize(50);

    assertNotEquals(streaming1, streaming2); // canEqual returns false
  }

  @Test
  void shouldTestErrorEqualsWithNull() {
    ChatStreamingProperties.Error error = new ChatStreamingProperties.Error();
    assertNotEquals(error, null); // Null comparison
  }

  @Test
  void shouldTestErrorEqualsWithDifferentType() {
    ChatStreamingProperties.Error error = new ChatStreamingProperties.Error();
    assertNotEquals(error, "not an Error"); // Different type
  }

  @Test
  void shouldTestErrorEqualsWithCanEqualFalse() {
    ChatStreamingProperties.Error error1 = new ChatStreamingProperties.Error();
    error1.setRetryAttempts(3);

    // Anonymous subclass that overrides canEqual to return false
    ChatStreamingProperties.Error error2 =
        new ChatStreamingProperties.Error() {
          @Override
          public boolean canEqual(Object other) {
            return false;
          }
        };
    error2.setRetryAttempts(3);

    assertNotEquals(error1, error2); // canEqual returns false
  }

  @Test
  void shouldTestPropertiesEqualsWithOneNullResponseTimeoutField() {
    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setResponseTimeout(Duration.ofSeconds(300));

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setResponseTimeout(null);

    assertNotEquals(props1, props2); // One has null responseTimeout, other doesn't
  }

  @Test
  void shouldTestPropertiesEqualsWithOneNullSseTimeoutField() {
    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setSseTimeout(Duration.ofSeconds(300));

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setSseTimeout(null);

    assertNotEquals(props1, props2); // One has null sseTimeout, other doesn't
  }

  @Test
  void shouldTestPropertiesEqualsWithOneNullStreamingField() {
    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setStreaming(new ChatStreamingProperties.Streaming());

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setStreaming(null);

    assertNotEquals(props1, props2); // One has null streaming, other doesn't
  }

  @Test
  void shouldTestPropertiesEqualsWithOneNullErrorField() {
    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setError(new ChatStreamingProperties.Error());

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setError(null);

    assertNotEquals(props1, props2); // One has null error, other doesn't
  }

  @Test
  void shouldTestStreamingEqualsWithOneNullBufferTimeoutField() {
    ChatStreamingProperties.Streaming streaming1 = new ChatStreamingProperties.Streaming();
    streaming1.setBufferTimeout(Duration.ofMillis(100));

    ChatStreamingProperties.Streaming streaming2 = new ChatStreamingProperties.Streaming();
    streaming2.setBufferTimeout(null);

    assertNotEquals(streaming1, streaming2); // One has null bufferTimeout, other doesn't
  }

  @Test
  void shouldTestStreamingEqualsWithOneNullHeartbeatIntervalField() {
    ChatStreamingProperties.Streaming streaming1 = new ChatStreamingProperties.Streaming();
    streaming1.setHeartbeatInterval(Duration.ofSeconds(30));

    ChatStreamingProperties.Streaming streaming2 = new ChatStreamingProperties.Streaming();
    streaming2.setHeartbeatInterval(null);

    assertNotEquals(streaming1, streaming2); // One has null heartbeatInterval, other doesn't
  }

  @Test
  void shouldTestErrorEqualsWithOneNullRetryDelayField() {
    ChatStreamingProperties.Error error1 = new ChatStreamingProperties.Error();
    error1.setRetryDelay(Duration.ofMillis(1000));

    ChatStreamingProperties.Error error2 = new ChatStreamingProperties.Error();
    error2.setRetryDelay(null);

    assertNotEquals(error1, error2); // One has null retryDelay, other doesn't
  }

  @Test
  void shouldTestPropertiesHashCodeWithAllNullFields() {
    ChatStreamingProperties props1 = new ChatStreamingProperties();
    props1.setResponseTimeout(null);
    props1.setSseTimeout(null);
    props1.setStreaming(null);
    props1.setError(null);

    ChatStreamingProperties props2 = new ChatStreamingProperties();
    props2.setResponseTimeout(null);
    props2.setSseTimeout(null);
    props2.setStreaming(null);
    props2.setError(null);

    assertEquals(props1.hashCode(), props2.hashCode()); // Both have all null fields
  }

  @Test
  void shouldTestStreamingHashCodeWithAllNullFields() {
    ChatStreamingProperties.Streaming streaming1 = new ChatStreamingProperties.Streaming();
    streaming1.setBufferTimeout(null);
    streaming1.setHeartbeatInterval(null);

    ChatStreamingProperties.Streaming streaming2 = new ChatStreamingProperties.Streaming();
    streaming2.setBufferTimeout(null);
    streaming2.setHeartbeatInterval(null);

    assertEquals(streaming1.hashCode(), streaming2.hashCode()); // Both have null fields
  }

  @Test
  void shouldTestErrorHashCodeWithAllNullFields() {
    ChatStreamingProperties.Error error1 = new ChatStreamingProperties.Error();
    error1.setRetryDelay(null);

    ChatStreamingProperties.Error error2 = new ChatStreamingProperties.Error();
    error2.setRetryDelay(null);

    assertEquals(error1.hashCode(), error2.hashCode()); // Both have null retryDelay
  }
}
