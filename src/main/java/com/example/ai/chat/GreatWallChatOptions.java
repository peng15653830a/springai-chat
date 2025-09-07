package com.example.ai.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.prompt.ChatOptions;

/**
 * 长城大模型聊天选项配置
 * 支持长城大模型特有的参数配置
 *
 * @author xupeng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GreatWallChatOptions implements ChatOptions {

    /**
     * 模型名称
     */
    private String model;

    /**
     * 温度参数 (0.0 - 1.0)
     */
    private Double temperature;

    /**
     * 最大token数
     */
    private Integer maxTokens;

    /**
     * 是否启用推理模式（长城大模型特有）
     */
    private Boolean enableThinking;

    /**
     * API运行ID（长城大模型特有）
     */
    private String apiRunId;

    /**
     * 用户ID前缀（长城大模型特有）
     */
    private String tpuidPrefix;

    /**
     * Top-P采样参数 (Spring AI ChatOptions必需)
     */
    @Builder.Default
    private Double topP = 1.0;

    /**
     * Top-K采样参数 (Spring AI ChatOptions必需)
     */
    private Integer topK;

    /**
     * 停止序列 (Spring AI ChatOptions必需)
     */
    private java.util.List<String> stopSequences;

    /**
     * 出现惩罚参数 (Spring AI ChatOptions必需)
     */
    private Double presencePenalty;

    /**
     * 频率惩罚参数 (Spring AI ChatOptions必需)
     */
    private Double frequencyPenalty;

    /**
     * 创建默认选项
     */
    public static GreatWallChatOptions create() {
        return builder()
                .temperature(0.7)
                .maxTokens(4096)
                .enableThinking(false)
                .tpuidPrefix("guest")
                .build();
    }

    /**
     * 从通用ChatOptions转换
     */
    public static GreatWallChatOptions from(ChatOptions options) {
        if (options instanceof GreatWallChatOptions) {
            return (GreatWallChatOptions) options;
        }

        GreatWallChatOptions newOptions = new GreatWallChatOptions();
        if (options != null) {
            newOptions.setModel(options.getModel());
            newOptions.setTemperature(options.getTemperature());
            newOptions.setMaxTokens(options.getMaxTokens());
            newOptions.setTopP(options.getTopP());
            newOptions.setTopK(options.getTopK());
            newOptions.setStopSequences(options.getStopSequences());
            newOptions.setPresencePenalty(options.getPresencePenalty());
            newOptions.setFrequencyPenalty(options.getFrequencyPenalty());
        }
        
        // 设置默认值
        if (newOptions.enableThinking == null) {
            newOptions.enableThinking = false;
        }
        if (newOptions.tpuidPrefix == null) {
            newOptions.tpuidPrefix = "guest";
        }
        if (newOptions.topP == null) {
            newOptions.topP = 1.0;
        }

        return newOptions;
    }

    /**
     * 复制并修改选项
     */
    public GreatWallChatOptions copy() {
        return builder()
                .model(this.model)
                .temperature(this.temperature)
                .maxTokens(this.maxTokens)
                .enableThinking(this.enableThinking)
                .apiRunId(this.apiRunId)
                .tpuidPrefix(this.tpuidPrefix)
                .topP(this.topP)
                .topK(this.topK)
                .stopSequences(this.stopSequences)
                .presencePenalty(this.presencePenalty)
                .frequencyPenalty(this.frequencyPenalty)
                .build();
    }
}