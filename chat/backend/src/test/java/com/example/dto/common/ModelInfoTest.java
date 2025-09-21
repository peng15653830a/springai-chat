package com.example.dto.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * ModelInfo DTO测试类
 *
 * @author xupeng
 */
class ModelInfoTest {

  @Test
  void shouldCreateModelInfo() {
    // When
    ModelInfo modelInfo = new ModelInfo();

    // Then
    assertThat(modelInfo).isNotNull();
  }

  @Test
  void shouldSetAndGetId() {
    // Given
    ModelInfo modelInfo = new ModelInfo();
    Long id = 1L;

    // When
    modelInfo.setId(id);

    // Then
    assertThat(modelInfo.getId()).isEqualTo(id);
  }

  @Test
  void shouldSetAndGetName() {
    // Given
    ModelInfo modelInfo = new ModelInfo();
    String name = "deepseek-chat";

    // When
    modelInfo.setName(name);

    // Then
    assertThat(modelInfo.getName()).isEqualTo(name);
  }

  @Test
  void shouldSetAndGetDisplayName() {
    // Given
    ModelInfo modelInfo = new ModelInfo();
    String displayName = "DeepSeek Chat";

    // When
    modelInfo.setDisplayName(displayName);

    // Then
    assertThat(modelInfo.getDisplayName()).isEqualTo(displayName);
  }

  @Test
  void shouldSetAndGetMaxTokens() {
    // Given
    ModelInfo modelInfo = new ModelInfo();
    Integer maxTokens = 4096;

    // When
    modelInfo.setMaxTokens(maxTokens);

    // Then
    assertThat(modelInfo.getMaxTokens()).isEqualTo(maxTokens);
  }

  @Test
  void shouldSetAndGetTemperature() {
    // Given
    ModelInfo modelInfo = new ModelInfo();
    BigDecimal temperature = BigDecimal.valueOf(0.7);

    // When
    modelInfo.setTemperature(temperature);

    // Then
    assertThat(modelInfo.getTemperature()).isEqualTo(temperature);
  }

  @Test
  void shouldSetAndGetSupportsThinking() {
    // Given
    ModelInfo modelInfo = new ModelInfo();
    Boolean supportsThinking = true;

    // When
    modelInfo.setSupportsThinking(supportsThinking);

    // Then
    assertThat(modelInfo.getSupportsThinking()).isEqualTo(supportsThinking);
  }

  @Test
  void shouldSetAndGetSupportsStreaming() {
    // Given
    ModelInfo modelInfo = new ModelInfo();
    Boolean supportsStreaming = true;

    // When
    modelInfo.setSupportsStreaming(supportsStreaming);

    // Then
    assertThat(modelInfo.getSupportsStreaming()).isEqualTo(supportsStreaming);
  }

  @Test
  void shouldSetAndGetAvailable() {
    // Given
    ModelInfo modelInfo = new ModelInfo();
    Boolean available = true;

    // When
    modelInfo.setAvailable(available);

    // Then
    assertThat(modelInfo.getAvailable()).isEqualTo(available);
  }

  @Test
  void shouldSetAndGetSortOrder() {
    // Given
    ModelInfo modelInfo = new ModelInfo();
    Integer sortOrder = 1;

    // When
    modelInfo.setSortOrder(sortOrder);

    // Then
    assertThat(modelInfo.getSortOrder()).isEqualTo(sortOrder);
  }

  @Test
  void shouldGetFullModelId() {
    // Given
    ModelInfo modelInfo = new ModelInfo();
    modelInfo.setName("deepseek-chat");
    Long providerId = 1L;

    // When
    String fullModelId = modelInfo.getFullModelId(providerId);

    // Then
    assertThat(fullModelId).isEqualTo("1-deepseek-chat");
  }

  @Test
  void shouldTestEqualsAndHashCode() {
    // Given
    ModelInfo modelInfo1 = new ModelInfo();
    modelInfo1.setId(1L);
    modelInfo1.setName("deepseek-chat");

    ModelInfo modelInfo2 = new ModelInfo();
    modelInfo2.setId(1L);
    modelInfo2.setName("deepseek-chat");

    ModelInfo modelInfo3 = new ModelInfo();
    modelInfo3.setId(2L);
    modelInfo3.setName("different-model");

    // Then
    assertThat(modelInfo1).isEqualTo(modelInfo2);
    assertThat(modelInfo1).isNotEqualTo(modelInfo3);
    assertThat(modelInfo1.hashCode()).isEqualTo(modelInfo2.hashCode());
  }

  @Test
  void shouldTestToString() {
    // Given
    ModelInfo modelInfo = new ModelInfo();
    modelInfo.setId(1L);
    modelInfo.setName("deepseek-chat");

    // When
    String toString = modelInfo.toString();

    // Then
    assertThat(toString).isNotNull();
    assertThat(toString).contains("1");
    assertThat(toString).contains("deepseek-chat");
  }

  @Test
  void shouldHandleNullValues() {
    // Given
    ModelInfo modelInfo = new ModelInfo();

    // Then
    assertThat(modelInfo.getId()).isNull();
    assertThat(modelInfo.getName()).isNull();
    assertThat(modelInfo.getDisplayName()).isNull();
    assertThat(modelInfo.getMaxTokens()).isNull();
    assertThat(modelInfo.getTemperature()).isNull();
    assertThat(modelInfo.getSupportsThinking()).isNull();
    assertThat(modelInfo.getSupportsStreaming()).isNull();
    assertThat(modelInfo.getAvailable()).isNull();
    assertThat(modelInfo.getSortOrder()).isNull();
  }

  @Test
  void shouldHandleZeroValues() {
    // Given
    ModelInfo modelInfo = new ModelInfo();
    modelInfo.setMaxTokens(0);
    modelInfo.setSortOrder(0);

    // Then
    assertThat(modelInfo.getMaxTokens()).isEqualTo(0);
    assertThat(modelInfo.getSortOrder()).isEqualTo(0);
  }

  @Test
  void shouldHandleNegativeValues() {
    // Given
    ModelInfo modelInfo = new ModelInfo();
    modelInfo.setMaxTokens(-1);
    modelInfo.setSortOrder(-1);

    // Then
    assertThat(modelInfo.getMaxTokens()).isEqualTo(-1);
    assertThat(modelInfo.getSortOrder()).isEqualTo(-1);
  }
}
