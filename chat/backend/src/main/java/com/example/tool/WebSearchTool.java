package com.example.tool;

import com.example.config.ChatStreamingProperties;
import com.example.dto.response.SearchResult;
import com.example.service.MessageToolResultService;
import com.example.service.SearchService;
import com.example.service.SseEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Spring AI Tool for web search functionality 按照Spring AI 1.0.0标准实现，使用ToolContext获取上下文信息
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
  private final ChatStreamingProperties chatStreamingProperties;

  // 限制每条消息触发搜索工具的次数，避免模型反复调用
  private static final ConcurrentHashMap<Long, Integer> MESSAGE_TOOL_CALLS = new ConcurrentHashMap<>();
  // 每条消息的查询缓存：messageId -> (normalizedQuery -> results)
  private static final ConcurrentHashMap<Long, ConcurrentHashMap<String, List<SearchResult>>>
      MESSAGE_QUERY_CACHE = new ConcurrentHashMap<>();
  // 达到上限后标记禁用：messageId -> true
  private static final ConcurrentHashMap<Long, Boolean> MESSAGE_TOOL_DISABLED = new ConcurrentHashMap<>();

  @Tool(description = "执行网络搜索获取最新信息")
  public java.util.List<SearchResult> searchWeb(
      @ToolParam(description = "搜索查询内容，用于查找相关信息") String query, ToolContext toolContext) {
    Long toolResultId = null;
    try {
      // 从ToolContext获取上下文信息（Spring AI 1.0标准做法）
      Map<String, Object> context = toolContext.getContext();
      Long conversationId = (Long) context.get("conversationId");
      Long messageId = (Long) context.get("messageId");
      // 仅输出安全概要，避免将历史消息等大字段打印到日志
      Object history = context.get("TOOL_CALL_HISTORY");
      Integer historySize = null;
      if (history instanceof java.util.List<?> list) {
        historySize = list.size();
      }
      if (log.isDebugEnabled()) {
        log.debug(
            "🔧 ToolContext概要: keys={}, conversationId={}, messageId={}, TOOL_CALL_HISTORY.size={}",
            context != null ? context.keySet() : null,
            conversationId,
            messageId,
            historySize);
      }
      Object searchEnabledObj = context.get("searchEnabled");
      boolean searchEnabled =
          (searchEnabledObj instanceof Boolean b)
              ? b
              : (searchEnabledObj != null
                  && "true".equalsIgnoreCase(String.valueOf(searchEnabledObj)));
      if (!searchEnabled) {
        log.info("🪫 搜索开关为 false，忽略本次工具调用");
        return java.util.Collections.emptyList();
      }
      // 先做限流检查，再记录查询日志，避免超限情况下噪声
      Long counterKey = (messageId != null && messageId > 0) ? messageId : conversationId;
      if (counterKey != null) {
        int limit = Math.max(1, chatStreamingProperties.getSearch().getMaxToolCalls());
        int current = MESSAGE_TOOL_CALLS.getOrDefault(counterKey, 0);
        if (current >= limit) {
          log.warn("⛔ 已达到该消息的搜索调用上限({})，直接返回提示结果。key={}, query={}", limit, counterKey, query);
          if (messageId != null && messageId > 0) {
            MESSAGE_TOOL_DISABLED.put(messageId, Boolean.TRUE);
          }
          SearchResult sentinel =
              new SearchResult(
                  "TOOL_LIMIT_REACHED",
                  "已达到搜索次数上限，请基于现有结果作答，不要再次调用搜索工具。",
                  null,
                  null,
                  "搜索次数上限: " + limit + "。请继续给出答案，并在合适处引用已返回的来源链接。");
          return java.util.List.of(sentinel);
        }
      }
      // 通过限流检查，记录查询日志
      log.info("🔍 Spring AI Tool调用搜索，query='{}', cid={}, mid={}", query, conversationId, messageId);
      log.info("🔧 WebSearchTool从ToolContext获取到会话ID: {}, 消息ID: {}", conversationId, messageId);

      // 计算本消息的调用次数
      if (counterKey != null) {
        int limit = Math.max(1, chatStreamingProperties.getSearch().getMaxToolCalls());
        int count = MESSAGE_TOOL_CALLS.merge(counterKey, 1, Integer::sum);
        if (count > limit) {
          log.warn(
              "⛔ 已达到该消息的搜索调用上限({})，返回提示结果以终止后续工具调用。key={}, query={}",
              limit,
              counterKey,
              query);
          // 返回一个哨兵结果，提示模型不要继续调用工具
          SearchResult sentinel =
              new SearchResult(
                  "TOOL_LIMIT_REACHED",
                  "已达到搜索次数上限，请基于现有结果作答，不要再次调用搜索工具。",
                  null,
                  null,
                  "搜索次数上限: " + limit + "。请继续给出答案，并在合适处引用已返回的来源链接。");
          return java.util.List.of(sentinel);
        }
      }

      // 若该消息已被禁用工具，直接返回哨兵结果
      if (messageId != null && Boolean.TRUE.equals(MESSAGE_TOOL_DISABLED.get(messageId))) {
        SearchResult sentinel =
            new SearchResult(
                "TOOL_DISABLED",
                "已禁用搜索工具，请基于现有信息完成回答。",
                null,
                null,
                "工具已禁用（已达调用上限），请继续作答。");
        return java.util.List.of(sentinel);
      }

      // 命中缓存则直接返回
      String normQuery = normalizeQuery(query);
      if (messageId != null && messageId > 0) {
        List<SearchResult> cached =
            MESSAGE_QUERY_CACHE.getOrDefault(messageId, new ConcurrentHashMap<>()).get(normQuery);
        if (cached != null) {
          log.info("📦 命中搜索缓存，mid={}, q='{}', size={}", messageId, normQuery, cached.size());
          sseEventPublisher.publishSearchResults(conversationId, messageId, cached);
          sseEventPublisher.publishSearchComplete(conversationId);
          return cached;
        }
      }

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

      // 执行搜索（响应式转阻塞，因为Spring AI Tool框架要求同步返回）
      List<SearchResult> results = searchService.search(query).block();

      // 写入缓存
      if (messageId != null && messageId > 0) {
        MESSAGE_QUERY_CACHE
            .computeIfAbsent(messageId, k -> new ConcurrentHashMap<>())
            .put(normQuery, results != null ? results : java.util.Collections.emptyList());
      }

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
          messageToolResultService.completeToolCall(
              toolResultId, objectMapper.writeValueAsString(results));
          log.debug("✅ 工具调用记录已完成，ID: {}", toolResultId);
        } catch (Exception e) {
          log.warn("⚠️ 无法保存搜索结果到工具调用记录: {}", e.getMessage());
        }
      }

      // 发布搜索结果事件到前端（仅发送可作为引用的来源：带有效URL的项）
      if (results != null && !results.isEmpty()) {
        java.util.List<SearchResult> displayResults =
            results.stream()
                .filter(r -> r != null && r.getUrl() != null && !r.getUrl().isBlank())
                .filter(
                    r -> {
                      String u = r.getUrl();
                      return u.startsWith("http://") || u.startsWith("https://");
                    })
                .toList();
        // 更精准地携带消息ID，避免同一会话并发请求时出现混淆
        sseEventPublisher.publishSearchResults(conversationId, messageId, displayResults);
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

  private static String normalizeQuery(String q) {
    if (q == null) return "";
    return q.trim().toLowerCase();
  }
}
