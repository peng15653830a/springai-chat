package com.example.novel.tool;

import com.example.dto.response.SearchResult;
import com.example.novel.dto.request.RagSearchRequest;
import com.example.novel.dto.response.RagSearchResponse;
import com.example.novel.service.rag.RagService;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NovelRagTool {

  private final RagService ragService;

  @Tool(description = "检索小说素材库，返回最相关的片段")
  public List<SearchResult> searchNovelMaterials(
      @ToolParam(description = "检索关键词") String query, ToolContext toolContext) {
    if (query == null || query.isBlank()) {
      return Collections.emptyList();
    }

    RagSearchRequest request = new RagSearchRequest();
    request.setQuery(query);
    request.setTopK(5);
    Map<String, Object> ctx = toolContext != null ? toolContext.getContext() : Map.of();
    Long sessionId = asLong(ctx.get("conversationId"));
    Long messageId = asLong(ctx.get("messageId"));
    request.setSessionId(sessionId);
    request.setMessageId(messageId);

    try {
      RagSearchResponse response =
          ragService.searchMaterials(request).block(Duration.ofSeconds(8));
      if (response == null
          || !Boolean.TRUE.equals(response.getSuccess())
          || response.getResults() == null) {
        return Collections.emptyList();
      }

      return response.getResults().stream()
          .map(this::mapResult)
          .filter(java.util.Objects::nonNull)
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.warn("RAG检索失败: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  private SearchResult mapResult(RagSearchResponse.RagSearchResult result) {
    if (result == null) {
      return null;
    }
    SearchResult searchResult = new SearchResult();
    searchResult.setTitle(result.getTitle() != null ? result.getTitle() : result.getSource());
    searchResult.setSnippet(result.getExcerpt());
    searchResult.setUrl(result.getSource());
    searchResult.setScore(result.getSimilarity());
    searchResult.setContent(result.getContent());
    return searchResult;
  }

  private static Long asLong(Object value) {
    if (value instanceof Long l) {
      return l;
    }
    if (value instanceof Number n) {
      return n.longValue();
    }
    try {
      return value != null ? Long.parseLong(String.valueOf(value)) : null;
    } catch (Exception ignore) {
      return null;
    }
  }
}
