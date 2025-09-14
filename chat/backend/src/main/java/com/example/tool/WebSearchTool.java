package com.example.tool;

import com.example.service.SearchService;
import com.example.service.SseEventPublisher;
import com.example.service.MessageToolResultService;
import com.example.dto.response.SearchResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Spring AI Tool for web search functionality
 * 按照Spring AI 1.0.0标准实现
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

    // 线程本地存储，保存当前搜索结果供后续使用
    private static final ThreadLocal<List<SearchResult>> currentSearchResults = new ThreadLocal<>();

    // 线程本地存储，保存当前会话ID
    private static final ThreadLocal<Long> toolConversationId = new ThreadLocal<>();

    // 线程本地存储，保存当前消息ID
    private static final ThreadLocal<Long> toolMessageId = new ThreadLocal<>();

    @Tool(description = "执行网络搜索获取最新信息")
    public String searchWeb(
            @ToolParam(description = "搜索查询内容，用于查找相关信息") String query
    ) {
        log.info("🔍 Spring AI Tool调用搜索，查询: {}", query);

        Long toolResultId = null;
        try {
            // 获取当前会话ID和消息ID
            Long conversationId = getCurrentConversationId();
            Long messageId = getCurrentMessageId();
            log.info("🔧 WebSearchTool获取到会话ID: {}, 消息ID: {}", conversationId, messageId);

            // 开始工具调用记录（消息级别存储）
            if (messageId != null) {
                toolResultId = messageToolResultService.startToolCall(messageId, "webSearch", query);
                log.debug("🔧 开始工具调用记录，ID: {}", toolResultId);
            }

            // 发布搜索开始事件
            sseEventPublisher.publishSearchStart(conversationId);

            // 执行搜索
            List<SearchResult> results = searchService.search(query);

            // 保存搜索结果到线程本地存储
            currentSearchResults.set(results);

            // 完成工具调用记录
            if (messageId != null && toolResultId != null) {
                messageToolResultService.saveSearchResults(messageId, query, results);
                log.debug("✅ 工具调用记录已完成，ID: {}", toolResultId);
            }

            // 发布搜索结果事件到前端
            if (results != null && !results.isEmpty()) {
                sseEventPublisher.publishSearchResults(conversationId, results);
                log.info("📤 搜索结果已发送到前端，共{}条结果", results.size());
            }

            // 发布搜索完成事件
            sseEventPublisher.publishSearchComplete(conversationId);

            // 格式化搜索结果返回给AI模型
            String formattedResults = formatSearchResultsForAI(results);
            log.info("✅ 搜索完成，返回{}条结果给AI模型", results.size());

            return formattedResults;

        } catch (Exception e) {
            log.error("❌ 搜索执行失败: {}", e.getMessage(), e);

            // 记录工具调用失败
            if (toolResultId != null) {
                messageToolResultService.failToolCall(toolResultId, "搜索服务暂时不可用: " + e.getMessage());
            }

            // 获取当前会话ID并发布搜索错误事件
            Long conversationId = getCurrentConversationId();
            sseEventPublisher.publishSearchError(conversationId, "搜索服务暂时不可用，请稍后重试");
            return "搜索服务暂时不可用，请稍后重试。";
        }
    }
    
    /**
     * 设置当前会话ID到工具线程本地存储
     */
    public static void setToolConversationId(Long conversationId) {
        toolConversationId.set(conversationId);
    }

    /**
     * 设置当前消息ID到工具线程本地存储
     */
    public static void setToolMessageId(Long messageId) {
        toolMessageId.set(messageId);
    }

    /**
     * 获取当前会话ID
     */
    private Long getCurrentConversationId() {
        return toolConversationId.get();
    }

    /**
     * 获取当前消息ID
     */
    private Long getCurrentMessageId() {
        return toolMessageId.get();
    }

    /**
     * 获取当前线程的搜索结果
     */
    public static List<SearchResult> getCurrentSearchResults() {
        return currentSearchResults.get();
    }

    /**
     * 清理当前线程的搜索结果、会话ID和消息ID
     */
    public static void clearCurrentSearchResults() {
        currentSearchResults.remove();
        toolConversationId.remove();
        toolMessageId.remove();
    }

    /**
     * 格式化搜索结果给AI模型使用
     */
    private String formatSearchResultsForAI(List<SearchResult> results) {
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