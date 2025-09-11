package com.example.ai.chat;

import com.example.integration.ai.deepseek.DeepSeekChatOptions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.ChatOptions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DeepSeekChatOptions测试类
 *
 * @author xupeng
 */
class DeepSeekChatOptionsTest {

    @Test
    void shouldCreateDefaultOptions() {
        // When
        DeepSeekChatOptions options = new DeepSeekChatOptions();

        // Then
        assertThat(options).isNotNull();
        assertThat(options.getModel()).isNull();
        assertThat(options.getTemperature()).isNull();
        assertThat(options.getMaxTokens()).isNull();
        assertThat(options.getEnableThinking()).isNull();
        assertThat(options.getThinkingBudget()).isNull();
        assertThat(options.getStream()).isTrue(); // Default value
        assertThat(options.getTopP()).isEqualTo(1.0); // Default value
        assertThat(options.getTopK()).isNull();
        assertThat(options.getStopSequences()).isNull();
        assertThat(options.getPresencePenalty()).isNull();
        assertThat(options.getFrequencyPenalty()).isNull();
    }

    @Test
    void shouldCreateWithOptionsUsingBuilder() {
        // When
        DeepSeekChatOptions options = DeepSeekChatOptions.builder()
                .model("deepseek-chat")
                .temperature(0.7)
                .maxTokens(2048)
                .enableThinking(true)
                .thinkingBudget(50000)
                .stream(true)
                .topP(0.9)
                .topK(50)
                .stopSequences(List.of("\n", "User:"))
                .presencePenalty(0.1)
                .frequencyPenalty(0.1)
                .build();

        // Then
        assertThat(options.getModel()).isEqualTo("deepseek-chat");
        assertThat(options.getTemperature()).isEqualTo(0.7);
        assertThat(options.getMaxTokens()).isEqualTo(2048);
        assertThat(options.getEnableThinking()).isTrue();
        assertThat(options.getThinkingBudget()).isEqualTo(50000);
        assertThat(options.getStream()).isTrue();
        assertThat(options.getTopP()).isEqualTo(0.9);
        assertThat(options.getTopK()).isEqualTo(50);
        assertThat(options.getStopSequences()).containsExactly("\n", "User:");
        assertThat(options.getPresencePenalty()).isEqualTo(0.1);
        assertThat(options.getFrequencyPenalty()).isEqualTo(0.1);
    }

    @Test
    void shouldCreateDefaultOptionsUsingFactoryMethod() {
        // When
        DeepSeekChatOptions options = DeepSeekChatOptions.create();

        // Then
        assertThat(options.getTemperature()).isEqualTo(0.7);
        assertThat(options.getMaxTokens()).isEqualTo(4192);
        assertThat(options.getEnableThinking()).isFalse();
        assertThat(options.getStream()).isTrue();
    }

    @Test
    void shouldCreateThinkingOptionsUsingFactoryMethod() {
        // When
        DeepSeekChatOptions options = DeepSeekChatOptions.createWithThinking(30000);

        // Then
        assertThat(options.getTemperature()).isEqualTo(0.7);
        assertThat(options.getMaxTokens()).isEqualTo(4192);
        assertThat(options.getEnableThinking()).isTrue();
        assertThat(options.getThinkingBudget()).isEqualTo(30000);
        assertThat(options.getStream()).isTrue();
    }

    @Test
    void shouldCreateThinkingOptionsWithDefaultBudget() {
        // When
        DeepSeekChatOptions options = DeepSeekChatOptions.createWithThinking(null);

        // Then
        assertThat(options.getTemperature()).isEqualTo(0.7);
        assertThat(options.getMaxTokens()).isEqualTo(4192);
        assertThat(options.getEnableThinking()).isTrue();
        assertThat(options.getThinkingBudget()).isEqualTo(50000); // Default value
        assertThat(options.getStream()).isTrue();
    }

    @Test
    void shouldCopyOptions() {
        // Given
        DeepSeekChatOptions original = DeepSeekChatOptions.builder()
                .model("deepseek-chat")
                .temperature(0.7)
                .maxTokens(2048)
                .enableThinking(true)
                .thinkingBudget(50000)
                .stream(true)
                .topP(0.9)
                .topK(50)
                .stopSequences(List.of("\n"))
                .presencePenalty(0.1)
                .frequencyPenalty(0.1)
                .build();

        // When
        DeepSeekChatOptions copied = original.copy();

        // Then
        assertThat(copied).isNotSameAs(original);
        assertThat(copied.getModel()).isEqualTo(original.getModel());
        assertThat(copied.getTemperature()).isEqualTo(original.getTemperature());
        assertThat(copied.getMaxTokens()).isEqualTo(original.getMaxTokens());
        assertThat(copied.getEnableThinking()).isEqualTo(original.getEnableThinking());
        assertThat(copied.getThinkingBudget()).isEqualTo(original.getThinkingBudget());
        assertThat(copied.getStream()).isEqualTo(original.getStream());
        assertThat(copied.getTopP()).isEqualTo(original.getTopP());
        assertThat(copied.getTopK()).isEqualTo(original.getTopK());
        assertThat(copied.getStopSequences()).isEqualTo(original.getStopSequences());
        assertThat(copied.getPresencePenalty()).isEqualTo(original.getPresencePenalty());
        assertThat(copied.getFrequencyPenalty()).isEqualTo(original.getFrequencyPenalty());
    }

    @Test
    void shouldEnableThinkingWithOptions() {
        // Given
        DeepSeekChatOptions original = DeepSeekChatOptions.create();

        // When
        DeepSeekChatOptions withThinking = original.withThinking(40000);

        // Then
        assertThat(withThinking).isNotSameAs(original);
        assertThat(withThinking.getEnableThinking()).isTrue();
        assertThat(withThinking.getThinkingBudget()).isEqualTo(40000);
        // Other properties should remain the same
        assertThat(withThinking.getTemperature()).isEqualTo(original.getTemperature());
        assertThat(withThinking.getMaxTokens()).isEqualTo(original.getMaxTokens());
    }

    @Test
    void shouldDisableThinking() {
        // Given
        DeepSeekChatOptions original = DeepSeekChatOptions.createWithThinking(50000);

        // When
        DeepSeekChatOptions withoutThinking = original.withoutThinking();

        // Then
        assertThat(withoutThinking).isNotSameAs(original);
        assertThat(withoutThinking.getEnableThinking()).isFalse();
        assertThat(withoutThinking.getThinkingBudget()).isNull();
        // Other properties should remain the same
        assertThat(withoutThinking.getTemperature()).isEqualTo(original.getTemperature());
        assertThat(withoutThinking.getMaxTokens()).isEqualTo(original.getMaxTokens());
    }

    @Test
    void shouldConvertFromGenericChatOptions() {
        // Given
        ChatOptions genericOptions = new ChatOptions() {
            @Override
            public String getModel() {
                return "generic-model";
            }

            @Override
            public Double getTemperature() {
                return 0.8;
            }

            @Override
            public Integer getMaxTokens() {
                return 3072;
            }

            @Override
            public Double getTopP() {
                return 0.9;
            }

            @Override
            public Integer getTopK() {
                return 40;
            }

            @Override
            public List<String> getStopSequences() {
                return List.of("\n");
            }

            @Override
            public Double getPresencePenalty() {
                return 0.1;
            }

            @Override
            public Double getFrequencyPenalty() {
                return 0.1;
            }
            
            @Override
            public ChatOptions copy() {
                return this;
            }
        };

        // When
        DeepSeekChatOptions converted = DeepSeekChatOptions.from(genericOptions);

        // Then
        assertThat(converted.getModel()).isEqualTo("generic-model");
        assertThat(converted.getTemperature()).isEqualTo(0.8);
        assertThat(converted.getMaxTokens()).isEqualTo(3072);
        assertThat(converted.getTopP()).isEqualTo(0.9);
        assertThat(converted.getTopK()).isEqualTo(40);
        assertThat(converted.getStopSequences()).containsExactly("\n");
        assertThat(converted.getPresencePenalty()).isEqualTo(0.1);
        assertThat(converted.getFrequencyPenalty()).isEqualTo(0.1);
        // Default values for DeepSeek-specific options
        assertThat(converted.getEnableThinking()).isFalse();
        assertThat(converted.getStream()).isTrue();
    }

    @Test
    void shouldHandleNullGenericChatOptions() {
        // When
        DeepSeekChatOptions converted = DeepSeekChatOptions.from(null);

        // Then
        assertThat(converted).isNotNull();
        // Default values for DeepSeek-specific options
        assertThat(converted.getEnableThinking()).isFalse();
        assertThat(converted.getStream()).isTrue();
        assertThat(converted.getTopP()).isEqualTo(1.0);
        // Other properties should be null
        assertThat(converted.getModel()).isNull();
        assertThat(converted.getTemperature()).isNull();
        assertThat(converted.getMaxTokens()).isNull();
        assertThat(converted.getTopK()).isNull();
        assertThat(converted.getStopSequences()).isNull();
        assertThat(converted.getPresencePenalty()).isNull();
        assertThat(converted.getFrequencyPenalty()).isNull();
    }

    @Test
    void shouldReturnSameInstanceWhenConvertingFromDeepSeekChatOptions() {
        // Given
        DeepSeekChatOptions original = DeepSeekChatOptions.create();

        // When
        DeepSeekChatOptions converted = DeepSeekChatOptions.from(original);

        // Then
        assertThat(converted).isSameAs(original);
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        // Given
        DeepSeekChatOptions options1 = DeepSeekChatOptions.builder()
                .model("deepseek-chat")
                .temperature(0.7)
                .maxTokens(2048)
                .build();

        DeepSeekChatOptions options2 = DeepSeekChatOptions.builder()
                .model("deepseek-chat")
                .temperature(0.7)
                .maxTokens(2048)
                .build();

        DeepSeekChatOptions options3 = DeepSeekChatOptions.builder()
                .model("different-model")
                .temperature(0.7)
                .maxTokens(2048)
                .build();

        // Then
        assertThat(options1).isEqualTo(options2);
        assertThat(options1).isNotEqualTo(options3);
        assertThat(options1.hashCode()).isEqualTo(options2.hashCode());
        assertThat(options1.hashCode()).isNotEqualTo(options3.hashCode());
    }

    @Test
    void shouldTestToString() {
        // Given
        DeepSeekChatOptions options = DeepSeekChatOptions.builder()
                .model("deepseek-chat")
                .temperature(0.7)
                .build();

        // When
        String toString = options.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("deepseek-chat");
        assertThat(toString).contains("0.7");
    }

    @Test
    void shouldTestNoArgsConstructor() {
        // When
        DeepSeekChatOptions options = new DeepSeekChatOptions();

        // Then
        assertThat(options).isNotNull();
    }

    @Test
    void shouldTestAllArgsConstructor() {
        // When
        DeepSeekChatOptions options = new DeepSeekChatOptions(
                "deepseek-chat",
                0.7,
                2048,
                true,
                50000,
                false,
                0.9,
                50,
                List.of("\n"),
                0.1,
                0.1
        );

        // Then
        assertThat(options.getModel()).isEqualTo("deepseek-chat");
        assertThat(options.getTemperature()).isEqualTo(0.7);
        assertThat(options.getMaxTokens()).isEqualTo(2048);
        assertThat(options.getEnableThinking()).isTrue();
        assertThat(options.getThinkingBudget()).isEqualTo(50000);
        assertThat(options.getStream()).isFalse();
        assertThat(options.getTopP()).isEqualTo(0.9);
        assertThat(options.getTopK()).isEqualTo(50);
        assertThat(options.getStopSequences()).containsExactly("\n");
        assertThat(options.getPresencePenalty()).isEqualTo(0.1);
        assertThat(options.getFrequencyPenalty()).isEqualTo(0.1);
    }
}