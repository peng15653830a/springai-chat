package com.example.advisor;

import com.example.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 简化的Spring AI Advisor - 自动保存聊天记录
 *
 * 根据阿里巴巴开发规范，采用简洁实用的设计：
 * 1. 不增加不必要的复杂度
 * 2. 专注核心功能
 * 3. 保持代码可读性和可维护性
 *
 * 该Advisor主要用于演示Spring AI的Advisor机制，
 * 实际的消息保存逻辑由现有的AiChatServiceImpl处理
 *
 * @author xupeng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimplifiedMessageHistoryAdvisor implements CallAdvisor, StreamAdvisor {

    public static final String ADVISOR_NAME = "SIMPLIFIED_MESSAGE_HISTORY_ADVISOR";
    public static final String CONVERSATION_ID_KEY = "conversationId";
    public static final String MESSAGE_ID_KEY = "messageId";

    private final MessageService messageService;

    @Override
    public String getName() {
        return ADVISOR_NAME;
    }

    @Override
    public int getOrder() {
        return 100; // 较低优先级，让其他advisor先执行
    }

    /**
     * 非流式调用处理 - 主要用于日志记录和监控
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        log.debug("📝 SimplifiedMessageHistoryAdvisor - 开始处理非流式调用");

        // 记录请求信息
        logRequestInfo(request);

        // 调用下一个advisor
        ChatClientResponse response = chain.nextCall(request);

        // 记录响应信息
        logResponseInfo(response);

        return response;
    }

    /**
     * 流式调用处理 - 主要用于日志记录和监控
     */
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        log.debug("📝 SimplifiedMessageHistoryAdvisor - 开始处理流式调用");

        // 记录请求信息
        logRequestInfo(request);

        // 获取响应流并添加日志
        return chain.nextStream(request)
                .doOnNext(response -> log.trace("收到流式响应片段"))
                .doOnComplete(() -> log.debug("流式响应完成"))
                .doOnError(error -> log.error("流式响应出错: {}", error.getMessage()));
    }

    /**
     * 记录请求信息
     */
    private void logRequestInfo(ChatClientRequest request) {
        try {
            log.info("🔍 Spring AI Advisor处理聊天请求");

            // 可以在此处添加更多监控逻辑，如计数器、指标收集等
            // 实际的消息保存由AiChatServiceImpl处理

        } catch (Exception e) {
            log.warn("记录请求信息时出错: {}", e.getMessage());
        }
    }

    /**
     * 记录响应信息
     */
    private void logResponseInfo(ChatClientResponse response) {
        try {
            log.debug("✅ Advisor处理响应完成");

            // 可以在此处添加更多监控逻辑，如响应时间统计、成功率统计等

        } catch (Exception e) {
            log.warn("记录响应信息时出错: {}", e.getMessage());
        }
    }
}