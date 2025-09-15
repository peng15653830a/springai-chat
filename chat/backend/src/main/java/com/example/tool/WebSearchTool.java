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
    public String searchWeb(
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

            // 发布搜索结果事件到前端
            if (results != null && !results.isEmpty()) {
                sseEventPublisher.publishSearchResults(conversationId, results);
                log.info("📤 搜索结果已发送到前端，共{}条结果", results.size());
            }

            // 发布搜索完成事件
            sseEventPublisher.publishSearchComplete(conversationId);

            // 格式化搜索结果返回给AI模型
            String formattedResults = formatSearchResultsForAi(results);
            log.info("✅ 搜索完成，返回{}条结果给AI模型", results.size());

            return formattedResults;

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
            return "搜索服务暂时不可用，请稍后重试。";
        }
    }


    /**
     * 格式化搜索结果给AI模型使用
     */
    private String formatSearchResultsForAi(List<SearchResult> results) {
        if (results == null || results.isEmpty()) {
            log.warn("⚠️ 搜索结果为空或null");
            return "没有找到相关搜索结果。";
        }

        log.info("📋 开始格式化搜索结果，共{}条", results.size());
        StringBuilder formatted = new StringBuilder();
        formatted.append("以下是搜索到的相关信息：\n\n");

        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            log.info("📄 处理第{}条结果: 标题=[{}], 内容长度=[{}], URL=[{}]", 
                i + 1, 
                result.getTitle() != null ? result.getTitle().substring(0, Math.min(50, result.getTitle().length())) + "..." : "无",
                result.getContent() != null ? result.getContent().length() : 0,
                result.getUrl() != null ? result.getUrl() : "无");
                
            formatted.append(String.format("%d. 标题：%s\n", i + 1, 
                result.getTitle() != null ? result.getTitle() : ""));
            formatted.append(String.format("   内容：%s\n", 
                result.getContent() != null ? result.getContent() : ""));
            if (result.getUrl() != null && !result.getUrl().isEmpty()) {
                formatted.append(String.format("   来源：%s\n", result.getUrl()));
            }
            formatted.append("\n");
        }

        String formattedResult = formatted.toString();
        log.info("✅ 搜索结果格式化完成，总长度: {}", formattedResult.length());
        log.debug("🔍 格式化后的内容预览: {}", 
            formattedResult.length() > 200 ? formattedResult.substring(0, 200) + "..." : formattedResult);
        
        return formattedResult;
    }
}