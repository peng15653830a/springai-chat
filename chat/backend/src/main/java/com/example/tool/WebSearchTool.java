package com.example.tool;

import com.example.service.SearchService;
import com.example.service.SseEventPublisher;
import com.example.service.MessageToolResultService;
import com.example.dto.response.SearchResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * Spring AI Tool for web search functionality
 * 按照Spring AI 1.0.0标准实现，使用ToolContext获取上下文信息
 *
 * @author xupeng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSearchTool {

    private final SearchService searchService;
    private final SseEventPublisher sseEventPublisher;
    private final MessageToolResultService messageToolResultService;
    private final ObjectMapper objectMapper;


    @Tool(description = "执行网络搜索获取最新信息")
    public java.util.List<SearchResult> searchWeb(
            @ToolParam(description = "搜索查询内容，用于查找相关信息") String query,
            ToolContext toolContext
    ) {
        log.info("🔍 Spring AI Tool调用搜索，查询: {}", query);

        Long toolResultId = null;
        try {
            // 从ToolContext获取上下文信息（Spring AI 1.0标准做法）
            Map<String, Object> context = toolContext.getContext();
            log.info("🔧 ToolContext内容: {}", context);
            Long conversationId = (Long) context.get("conversationId");
            Long messageId = (Long) context.get("messageId");
            log.info("🔧 WebSearchTool从ToolContext获取到会话ID: {}, 消息ID: {}", conversationId, messageId);

            // 开始工具调用记录（消息级别存储）
            if (messageId != null && messageId > 0) {
                try {
                    toolResultId = messageToolResultService.startToolCall(messageId, "webSearch", query);
                    log.debug("🔧 开始工具调用记录，ID: {}", toolResultId);
                } catch (Exception e) {
                    log.warn("⚠️ 无法创建工具调用记录，messageId可能不存在: {}, 错误: {}", messageId, e.getMessage());
                    // 继续执行搜索，但不保存工具调用记录
                }
            }

            // 发布搜索开始事件
            sseEventPublisher.publishSearchStart(conversationId);

            // 执行搜索
            List<SearchResult> results = searchService.search(query);

            // 注意：ToolContext的context map是不可修改的，不能直接put
            // 我们通过ThreadLocal传递搜索结果（针对Spring AI框架限制的合理工作区域）
            // 将搜索结果存储到ThreadLocal，供DatabaseChatMemory使用
            if (results != null && !results.isEmpty()) {
                // 通过SseEventPublisher存储到ThreadLocal（已有publishSearchResults方法会存储）
                log.debug("🔧 搜索结果将通过SseEventPublisher存储到ThreadLocal");
            }

            // 完成工具调用记录
            if (messageId != null && toolResultId != null) {
                try {
                    messageToolResultService.completeToolCall(toolResultId, objectMapper.writeValueAsString(results));
                    log.debug("✅ 工具调用记录已完成，ID: {}", toolResultId);
                } catch (Exception e) {
                    log.warn("⚠️ 无法保存搜索结果到工具调用记录: {}", e.getMessage());
                }
            }

            // 发布搜索结果事件到前端（仅发送可作为引用的来源：带有效URL的项）
            if (results != null && !results.isEmpty()) {
                java.util.List<SearchResult> displayResults = results.stream()
                        .filter(r -> r != null && r.getUrl() != null && !r.getUrl().isBlank())
                        .filter(r -> {
                            String u = r.getUrl();
                            return u.startsWith("http://") || u.startsWith("https://");
                        })
                        .toList();
                sseEventPublisher.publishSearchResults(conversationId, displayResults);
                log.info("📤 搜索结果已发送到前端：可引用来源 {} 条（原始 {} 条）", displayResults.size(), results.size());
            }

            // 发布搜索完成事件
            sseEventPublisher.publishSearchComplete(conversationId);

            // 格式化搜索结果返回给AI模型
            log.info("✅ 搜索完成，返回{}条结果给AI模型（结构化）", results.size());
            return results;

        } catch (Exception e) {
            log.error("❌ 搜索执行失败，异常类型: {}, 异常信息: {}", e.getClass().getSimpleName(), e.getMessage(), e);

            // 记录工具调用失败
            if (toolResultId != null) {
                messageToolResultService.failToolCall(toolResultId, "搜索服务暂时不可用: " + e.getMessage());
            }

            // 从ToolContext获取会话ID并发布搜索错误事件
            Map<String, Object> context = toolContext.getContext();
            Long conversationId = (Long) context.get("conversationId");
            if (conversationId != null) {
                sseEventPublisher.publishSearchError(conversationId, "搜索服务暂时不可用，请稍后重试");
            }
            return java.util.Collections.emptyList();
        }
    }

}
