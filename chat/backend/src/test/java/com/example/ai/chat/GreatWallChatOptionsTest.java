package com.example.ai.chat;

import com.example.integration.ai.greatwall.GreatWallChatOptions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.ChatOptions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 长城大模型聊天选项配置单元测试
 */
class GreatWallChatOptionsTest {

    @Test
    void testBuilder() {
        // 执行测试
        GreatWallChatOptions options = GreatWallChatOptions.builder()
                .model("test-model")
                .temperature(0.8)
                .maxTokens(1024)
                .enableThinking(true)
                .apiRunId("test-api-run-id")
                .tpuidPrefix("test-user")
                .topP(0.9)
                .topK(50)
                .presencePenalty(0.1)
                .frequencyPenalty(0.2)
                .build();

        // 验证结果
        assertThat(options.getModel()).isEqualTo("test-model");
        assertThat(options.getTemperature()).isEqualTo(0.8);
        assertThat(options.getMaxTokens()).isEqualTo(1024);
        assertThat(options.getEnableThinking()).isEqualTo(true);
        assertThat(options.getApiRunId()).isEqualTo("test-api-run-id");
        assertThat(options.getTpuidPrefix()).isEqualTo("test-user");
        assertThat(options.getTopP()).isEqualTo(0.9);
        assertThat(options.getTopK()).isEqualTo(50);
        assertThat(options.getPresencePenalty()).isEqualTo(0.1);
        assertThat(options.getFrequencyPenalty()).isEqualTo(0.2);
    }

    @Test
    void testNoArgsConstructor() {
        // 执行测试
        GreatWallChatOptions options = new GreatWallChatOptions();

        // 验证结果
        assertThat(options.getModel()).isNull();
        assertThat(options.getTemperature()).isNull();
        assertThat(options.getMaxTokens()).isNull();
        assertThat(options.getEnableThinking()).isNull();
        assertThat(options.getApiRunId()).isNull();
        assertThat(options.getTpuidPrefix()).isNull();
        assertThat(options.getTopP()).isEqualTo(1.0); // 修正：topP有默认值1.0
        assertThat(options.getTopK()).isNull();
        assertThat(options.getStopSequences()).isNull();
        assertThat(options.getPresencePenalty()).isNull();
        assertThat(options.getFrequencyPenalty()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        // 执行测试
        GreatWallChatOptions options = new GreatWallChatOptions(
                "test-model",
                0.8,
                1024,
                true,
                "test-api-run-id",
                "test-user",
                0.9,
                50,
                null, // stopSequences
                0.1,
                0.2
        );

        // 验证结果
        assertThat(options.getModel()).isEqualTo("test-model");
        assertThat(options.getTemperature()).isEqualTo(0.8);
        assertThat(options.getMaxTokens()).isEqualTo(1024);
        assertThat(options.getEnableThinking()).isEqualTo(true);
        assertThat(options.getApiRunId()).isEqualTo("test-api-run-id");
        assertThat(options.getTpuidPrefix()).isEqualTo("test-user");
        assertThat(options.getTopP()).isEqualTo(0.9);
        assertThat(options.getTopK()).isEqualTo(50);
        assertThat(options.getStopSequences()).isNull();
        assertThat(options.getPresencePenalty()).isEqualTo(0.1);
        assertThat(options.getFrequencyPenalty()).isEqualTo(0.2);
    }

    @Test
    void testCreate() {
        // 执行测试
        GreatWallChatOptions options = GreatWallChatOptions.create();

        // 验证结果
        assertThat(options.getTemperature()).isEqualTo(0.7);
        assertThat(options.getMaxTokens()).isEqualTo(4096);
        assertThat(options.getEnableThinking()).isEqualTo(false);
        assertThat(options.getTpuidPrefix()).isEqualTo("guest");
        assertThat(options.getTopP()).isEqualTo(1.0);
    }

    @Test
    void testFromWithGreatWallChatOptions() {
        // 准备测试数据
        GreatWallChatOptions original = GreatWallChatOptions.builder()
                .model("test-model")
                .temperature(0.8)
                .maxTokens(1024)
                .enableThinking(true)
                .apiRunId("test-api-run-id")
                .tpuidPrefix("test-user")
                .topP(0.9)
                .topK(50)
                .presencePenalty(0.1)
                .frequencyPenalty(0.2)
                .build();

        // 执行测试
        GreatWallChatOptions options = GreatWallChatOptions.from(original);

        // 验证结果
        assertThat(options).isSameAs(original);
    }

    @Test
    void testFromWithGenericChatOptions() {
        // 准备测试数据
        ChatOptions genericOptions = new ChatOptions() {
            @Override
            public String getModel() {
                return "test-model";
            }

            @Override
            public Double getTemperature() {
                return 0.8;
            }

            @Override
            public Integer getMaxTokens() {
                return 1024;
            }

            @Override
            public Double getTopP() {
                return 0.9;
            }

            @Override
            public Integer getTopK() {
                return 50;
            }

            @Override
            public java.util.List<String> getStopSequences() {
                return null;
            }

            @Override
            public Double getPresencePenalty() {
                return 0.1;
            }

            @Override
            public Double getFrequencyPenalty() {
                return 0.2;
            }

            @Override
            public <T extends ChatOptions> T copy() {
                return (T) this;
            }
        };

        // 执行测试
        GreatWallChatOptions options = GreatWallChatOptions.from(genericOptions);

        // 验证结果
        assertThat(options.getModel()).isEqualTo("test-model");
        assertThat(options.getTemperature()).isEqualTo(0.8);
        assertThat(options.getMaxTokens()).isEqualTo(1024);
        assertThat(options.getTopP()).isEqualTo(0.9);
        assertThat(options.getTopK()).isEqualTo(50);
        assertThat(options.getStopSequences()).isNull();
        assertThat(options.getPresencePenalty()).isEqualTo(0.1);
        assertThat(options.getFrequencyPenalty()).isEqualTo(0.2);
        // 默认值
        assertThat(options.getEnableThinking()).isEqualTo(false);
        assertThat(options.getTpuidPrefix()).isEqualTo("guest");
        assertThat(options.getTopP()).isEqualTo(0.9);
    }

    @Test
    void testFromWithNullOptions() {
        // 执行测试
        GreatWallChatOptions options = GreatWallChatOptions.from(null);

        // 验证结果
        assertThat(options).isNotNull();
        // 默认值
        assertThat(options.getEnableThinking()).isEqualTo(false);
        assertThat(options.getTpuidPrefix()).isEqualTo("guest");
        assertThat(options.getTopP()).isEqualTo(1.0);
    }

    @Test
    void testCopy() {
        // 准备测试数据
        GreatWallChatOptions original = GreatWallChatOptions.builder()
                .model("test-model")
                .temperature(0.8)
                .maxTokens(1024)
                .enableThinking(true)
                .apiRunId("test-api-run-id")
                .tpuidPrefix("test-user")
                .topP(0.9)
                .topK(50)
                .presencePenalty(0.1)
                .frequencyPenalty(0.2)
                .build();

        // 执行测试
        GreatWallChatOptions copied = original.copy();

        // 验证结果
        assertThat(copied).isNotSameAs(original);
        assertThat(copied.getModel()).isEqualTo("test-model");
        assertThat(copied.getTemperature()).isEqualTo(0.8);
        assertThat(copied.getMaxTokens()).isEqualTo(1024);
        assertThat(copied.getEnableThinking()).isEqualTo(true);
        assertThat(copied.getApiRunId()).isEqualTo("test-api-run-id");
        assertThat(copied.getTpuidPrefix()).isEqualTo("test-user");
        assertThat(copied.getTopP()).isEqualTo(0.9);
        assertThat(copied.getTopK()).isEqualTo(50);
        assertThat(copied.getPresencePenalty()).isEqualTo(0.1);
        assertThat(copied.getFrequencyPenalty()).isEqualTo(0.2);
    }
}