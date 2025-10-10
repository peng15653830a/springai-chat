package com.example.tool;

import com.example.dto.response.SearchResult;
import com.example.service.MessageToolResultService;
import com.example.service.SearchService;
import com.example.service.SseEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * WebSearchTool（简化版）
 *
 * 设计目标：
 * - 单一职责：读取上下文 → 调用搜索 → 回传结果。
 * - 无本地缓存、无全局计数器，降低心智负担。
 * - 保留基础的 SSE 通知与调用记录以便前端展示与审计。
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
  public List<SearchResult> searchWeb(
      @ToolParam(description = "搜索查询内容，用于查找相关信息") String query, ToolContext toolContext) {
    Map<String, Object> ctx = toolContext != null ? toolContext.getContext() : Map.of();
    Long conversationId = asLong(ctx.get("conversationId"));
    Long messageId = asLong(ctx.get("messageId"));

    // 开关由调用端决定（AiChatServiceImpl 按请求注入工具并传递 searchEnabled）
    if (!asBoolean(ctx.get("searchEnabled"))) {
      log.debug("search disabled by context, skip. cid={}, mid={}", conversationId, messageId);
      return Collections.emptyList();
    }

    if (query == null || query.trim().isEmpty()) {
      log.debug("empty query, skip search. cid={}, mid={}", conversationId, messageId);
      return Collections.emptyList();
    }

    Long toolResultId = null;
    try {
      // 记录工具调用（非关键路径，失败不影响主流程）
      if (messageId != null && messageId > 0) {
        try {
          toolResultId = messageToolResultService.startToolCall(messageId, "webSearch", query);
        } catch (Exception ignore) {
        }
      }

      sseEventPublisher.publishSearchStart(conversationId);

      // 执行搜索（SearchService 内部已配置超时和重试）
      List<SearchResult> results = searchService.search(query).block();
      if (results == null) results = Collections.emptyList();

      // 仅向前端发送可引用来源
      List<SearchResult> display =
          results.stream()
              .filter(r -> r != null && r.getUrl() != null && !r.getUrl().isBlank())
              .filter(r -> {
                String u = r.getUrl();
                return u.startsWith("http://") || u.startsWith("https://");
              })
              .toList();
      sseEventPublisher.publishSearchResults(conversationId, messageId, display);
      sseEventPublisher.publishSearchComplete(conversationId);

      if (toolResultId != null) {
        try {
          messageToolResultService.completeToolCall(
              toolResultId, objectMapper.writeValueAsString(results));
        } catch (Exception ignore) {
        }
      }

      return results;

    } catch (Exception e) {
      log.warn("search failed: {}", e.getMessage());
      if (toolResultId != null) {
        try {
          messageToolResultService.failToolCall(toolResultId, "搜索失败: " + e.getMessage());
        } catch (Exception ignore) {
        }
      }
      if (conversationId != null) {
        sseEventPublisher.publishSearchError(conversationId, "搜索服务暂时不可用，请稍后重试");
      }
      return Collections.emptyList();
    }
  }

  private static Long asLong(Object v) {
    if (v instanceof Long l) return l;
    if (v instanceof Number n) return n.longValue();
    try {
      return v != null ? Long.parseLong(String.valueOf(v)) : null;
    } catch (Exception ignore) {
      return null;
    }
  }

  private static boolean asBoolean(Object v) {
    if (v instanceof Boolean b) return b;
    return v != null && "true".equalsIgnoreCase(String.valueOf(v));
  }
}

