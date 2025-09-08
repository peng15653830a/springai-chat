package com.example.dto.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserModelPreferenceDto测试类
 *
 * @author xupeng
 */
class UserModelPreferenceDtoTest {

    @Test
    void shouldCreateUserModelPreferenceDto() {
        // When
        UserModelPreferenceDto dto = new UserModelPreferenceDto();

        // Then
        assertThat(dto).isNotNull();
    }

    @Test
    void shouldSetAndGetUserId() {
        // Given
        UserModelPreferenceDto dto = new UserModelPreferenceDto();
        Long userId = 1L;

        // When
        dto.setUserId(userId);

        // Then
        assertThat(dto.getUserId()).isEqualTo(userId);
    }

    @Test
    void shouldSetAndGetProviderName() {
        // Given
        UserModelPreferenceDto dto = new UserModelPreferenceDto();
        String providerName = "DeepSeek";

        // When
        dto.setProviderName(providerName);

        // Then
        assertThat(dto.getProviderName()).isEqualTo(providerName);
    }

    @Test
    void shouldSetAndGetModelName() {
        // Given
        UserModelPreferenceDto dto = new UserModelPreferenceDto();
        String modelName = "deepseek-chat";

        // When
        dto.setModelName(modelName);

        // Then
        assertThat(dto.getModelName()).isEqualTo(modelName);
    }

    @Test
    void shouldSetAndGetProviderDisplayName() {
        // Given
        UserModelPreferenceDto dto = new UserModelPreferenceDto();
        String providerDisplayName = "DeepSeek API";

        // When
        dto.setProviderDisplayName(providerDisplayName);

        // Then
        assertThat(dto.getProviderDisplayName()).isEqualTo(providerDisplayName);
    }

    @Test
    void shouldSetAndGetModelDisplayName() {
        // Given
        UserModelPreferenceDto dto = new UserModelPreferenceDto();
        String modelDisplayName = "DeepSeek Chat";

        // When
        dto.setModelDisplayName(modelDisplayName);

        // Then
        assertThat(dto.getModelDisplayName()).isEqualTo(modelDisplayName);
    }

    @Test
    void shouldSetAndGetIsDefault() {
        // Given
        UserModelPreferenceDto dto = new UserModelPreferenceDto();
        Boolean isDefault = true;

        // When
        dto.setIsDefault(isDefault);

        // Then
        assertThat(dto.getIsDefault()).isEqualTo(isDefault);
    }

    @Test
    void shouldSetAndGetSupportsThinking() {
        // Given
        UserModelPreferenceDto dto = new UserModelPreferenceDto();
        Boolean supportsThinking = true;

        // When
        dto.setSupportsThinking(supportsThinking);

        // Then
        assertThat(dto.getSupportsThinking()).isEqualTo(supportsThinking);
    }

    @Test
    void shouldSetAndGetSupportsStreaming() {
        // Given
        UserModelPreferenceDto dto = new UserModelPreferenceDto();
        Boolean supportsStreaming = true;

        // When
        dto.setSupportsStreaming(supportsStreaming);

        // Then
        assertThat(dto.getSupportsStreaming()).isEqualTo(supportsStreaming);
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        // Given
        UserModelPreferenceDto dto1 = new UserModelPreferenceDto();
        dto1.setUserId(1L);
        dto1.setProviderName("DeepSeek");
        dto1.setModelName("deepseek-chat");

        UserModelPreferenceDto dto2 = new UserModelPreferenceDto();
        dto2.setUserId(1L);
        dto2.setProviderName("DeepSeek");
        dto2.setModelName("deepseek-chat");

        UserModelPreferenceDto dto3 = new UserModelPreferenceDto();
        dto3.setUserId(2L);
        dto3.setProviderName("DifferentProvider");
        dto3.setModelName("different-model");

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void shouldTestToString() {
        // Given
        UserModelPreferenceDto dto = new UserModelPreferenceDto();
        dto.setUserId(1L);
        dto.setProviderName("DeepSeek");
        dto.setModelName("deepseek-chat");

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("1");
        assertThat(toString).contains("DeepSeek");
        assertThat(toString).contains("deepseek-chat");
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        UserModelPreferenceDto dto = new UserModelPreferenceDto();

        // Then
        assertThat(dto.getUserId()).isNull();
        assertThat(dto.getProviderName()).isNull();
        assertThat(dto.getModelName()).isNull();
        assertThat(dto.getProviderDisplayName()).isNull();
        assertThat(dto.getModelDisplayName()).isNull();
        assertThat(dto.getIsDefault()).isNull();
        assertThat(dto.getSupportsThinking()).isNull();
        assertThat(dto.getSupportsStreaming()).isNull();
    }
}