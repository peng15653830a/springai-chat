package com.example.ai.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.prompt.ChatOptions;

/**
 * DeepSeek推理模型聊天选项配置
 * 支持推理模式和推理预算配置
 *
 * @author xupeng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeepSeekChatOptions implements ChatOptions {

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
     * 是否启用推理模式
     */
    private Boolean enableThinking;

    /**
     * 推理预算（推理模式下的token限制）
     */
    private Integer thinkingBudget;

    /**
     * 是否流式输出
     */
    @Builder.Default
    private Boolean stream = true;

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
    public static DeepSeekChatOptions create() {
        return builder()
                .temperature(0.7)
                .maxTokens(4192)
                .enableThinking(false)
                .stream(true)
                .build();
    }

    /**
     * 创建推理模式选项
     */
    public static DeepSeekChatOptions createWithThinking(Integer thinkingBudget) {
        return builder()
                .temperature(0.7)
                .maxTokens(4192)
                .enableThinking(true)
                .thinkingBudget(thinkingBudget != null ? thinkingBudget : 50000)
                .stream(true)
                .build();
    }

    /**
     * 从通用ChatOptions转换
     */
    public static DeepSeekChatOptions from(ChatOptions options) {
        if (options instanceof DeepSeekChatOptions) {
            return (DeepSeekChatOptions) options;
        }

        DeepSeekChatOptions newOptions = new DeepSeekChatOptions();
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
        if (newOptions.stream == null) {
            newOptions.stream = true;
        }
        if (newOptions.topP == null) {
            newOptions.topP = 1.0;
        }

        return newOptions;
    }

    /**
     * 复制并修改选项
     */
    public DeepSeekChatOptions copy() {
        return builder()
                .model(this.model)
                .temperature(this.temperature)
                .maxTokens(this.maxTokens)
                .enableThinking(this.enableThinking)
                .thinkingBudget(this.thinkingBudget)
                .stream(this.stream)
                .topP(this.topP)
                .topK(this.topK)
                .stopSequences(this.stopSequences)
                .presencePenalty(this.presencePenalty)
                .frequencyPenalty(this.frequencyPenalty)
                .build();
    }

    /**
     * 启用推理模式
     */
    public DeepSeekChatOptions withThinking(Integer budget) {
        DeepSeekChatOptions newOptions = copy();
        newOptions.enableThinking = true;
        newOptions.thinkingBudget = budget;
        return newOptions;
    }

    /**
     * 禁用推理模式
     */
    public DeepSeekChatOptions withoutThinking() {
        DeepSeekChatOptions newOptions = copy();
        newOptions.enableThinking = false;
        newOptions.thinkingBudget = null;
        return newOptions;
    }
}