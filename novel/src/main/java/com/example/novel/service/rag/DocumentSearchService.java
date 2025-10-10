package com.example.novel.service.rag;

import com.example.novel.dto.request.RagSearchRequest;
import com.example.novel.dto.response.RagSearchResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 文档检索服务
 * 负责基于相似度的素材检索
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentSearchService {

  private final Map<String, List<DocumentChunk>> documentStore = new ConcurrentHashMap<>();
  private final com.example.novel.mapper.NovelReferenceMapper novelReferenceMapper;
  private final VectorStoreService vectorStoreService;

  /**
   * 搜索相关素材
   */
  public Mono<RagSearchResponse> searchMaterials(RagSearchRequest request) {
    return Mono.fromCallable(() -> {
      RagSearchResponse response = new RagSearchResponse();
      try {
        List<RagSearchResponse.RagSearchResult> results;

        // 混合检索：向量 + 关键词，并做简单重排
        List<RagSearchResponse.RagSearchResult> merged = new ArrayList<>();
        Map<String, RagSearchResponse.RagSearchResult> uniq = new LinkedHashMap<>();

        if (vectorStoreService != null && vectorStoreService.available()) {
          var docs = vectorStoreService.similaritySearch(request.getQuery(), request.getTopK(), request.getMinSimilarity());
          for (var d : docs) {
            var r = toSearchResult(d);
            String key = (r.getSource() + "|" + r.getTitle() + "|" + r.getExcerpt()).intern();
            uniq.putIfAbsent(key, r);
          }
        }

        // 关键词检索（补充/回退）
        var keywordHits = documentStore.values().stream()
            .flatMap(List::stream)
            .filter(chunk -> calculateSimilarity(request.getQuery(), chunk.getContent()) >= request.getMinSimilarity())
            .sorted((a, b) -> Double.compare(
                calculateSimilarity(request.getQuery(), b.getContent()),
                calculateSimilarity(request.getQuery(), a.getContent())
            ))
            .limit(Math.max(1, request.getTopK()))
            .map(this::toSearchResult)
            .collect(Collectors.toList());

        for (var r : keywordHits) {
          String key = (r.getSource() + "|" + r.getTitle() + "|" + r.getExcerpt()).intern();
          uniq.putIfAbsent(key, r);
        }

        // 简单重排：按关键词相似度排序（向量结果无分数时的合理近似）
        merged.addAll(uniq.values());
        merged.sort((x, y) -> Double.compare(
            calculateSimilarity(request.getQuery(), y.getContent()),
            calculateSimilarity(request.getQuery(), x.getContent())));
        results = merged.stream().limit(Math.max(1, request.getTopK())).collect(Collectors.toList());

        response.setSuccess(true);
        response.setMessage("搜索完成");
        response.setResults(results);
        saveReferences(request, results);
      } catch (Exception e) {
        log.error("搜索素材失败", e);
        response.setSuccess(false);
        response.setMessage("搜索失败: " + e.getMessage());
        response.setResults(new ArrayList<>());
      }
      return response;
    });
  }

  /**
   * 添加文档到索引
   */
  public void indexDocument(String source, List<DocumentChunk> chunks) {
    documentStore.put(source, chunks);
    log.debug("Indexed document: {}, chunks: {}", source, chunks.size());
  }

  /**
   * 获取所有文档
   */
  public Map<String, List<DocumentChunk>> getAllDocuments() {
    return new HashMap<>(documentStore);
  }

  /**
   * 清空索引
   */
  public void clearIndex() {
    documentStore.clear();
    log.info("Document index cleared");
  }

  /**
   * 计算相似度（简单关键词匹配）
   */
  private double calculateSimilarity(String query, String content) {
    String normalizedQuery = query.toLowerCase();
    String normalizedContent = content.toLowerCase();

    if (normalizedContent.contains(normalizedQuery)) {
      return 1.0;
    }

    String[] queryWords = normalizedQuery.split("\\s+");
    long matchingWords = Arrays.stream(queryWords)
        .mapToLong(word -> normalizedContent.contains(word) ? 1 : 0)
        .sum();

    return (double) matchingWords / Math.max(1, queryWords.length);
  }

  /**
   * 转换为搜索结果
   */
  private RagSearchResponse.RagSearchResult toSearchResult(DocumentChunk chunk) {
    RagSearchResponse.RagSearchResult result = new RagSearchResponse.RagSearchResult();
    result.setContent(chunk.getContent());
    result.setSource(chunk.getSource());
    result.setTitle(chunk.getTitle());
    result.setExcerpt(chunk.getContent().length() > 200 ?
        chunk.getContent().substring(0, 200) + "..." : chunk.getContent());
    result.setSimilarity(0.8);
    return result;
  }

  private RagSearchResponse.RagSearchResult toSearchResult(org.springframework.ai.document.Document doc) {
    RagSearchResponse.RagSearchResult result = new RagSearchResponse.RagSearchResult();
    String content;
    try {
      // Spring AI 1.0.x Document typically exposes 'getContent'.
      content = (String) doc.getClass().getMethod("getContent").invoke(doc);
    } catch (Exception e) {
      try {
        content = (String) doc.getClass().getMethod("getText").invoke(doc);
      } catch (Exception ex) {
        content = null;
      }
    }
    Map<String, Object> meta = doc.getMetadata();
    String source = meta != null ? String.valueOf(meta.getOrDefault("source", "")) : "";
    String title = meta != null ? String.valueOf(meta.getOrDefault("title", "")) : "";
    result.setContent(content);
    result.setSource(source);
    result.setTitle(title);
    result.setExcerpt(content != null && content.length() > 200 ? content.substring(0, 200) + "..." : content);
    // 相似度由底层返回的score不一定暴露，这里先用占位或由上层计算
    result.setSimilarity(0.0);
    return result;
  }

  /**
   * 保存检索结果引用
   */
  private void saveReferences(RagSearchRequest request, List<RagSearchResponse.RagSearchResult> results) {
    if (novelReferenceMapper != null && (request.getSessionId() != null || request.getMessageId() != null)) {
      for (RagSearchResponse.RagSearchResult r : results) {
        com.example.novel.entity.NovelReference ref = new com.example.novel.entity.NovelReference();
        ref.setSessionId(request.getSessionId());
        ref.setMessageId(request.getMessageId());
        ref.setSource(r.getSource());
        ref.setTitle(r.getTitle());
        ref.setExcerpt(r.getExcerpt());
        ref.setSimilarity(r.getSimilarity());
        ref.setUrl(r.getSource());
        try {
          novelReferenceMapper.insert(ref);
        } catch (Exception ignore) {
        }
      }
    }
  }

  /**
   * 文档块数据类
   */
  public static class DocumentChunk {
    private String content;
    private String source;
    private String title;
    private int chunkIndex;

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }

    public String getSource() {
      return source;
    }

    public void setSource(String source) {
      this.source = source;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public int getChunkIndex() {
      return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
      this.chunkIndex = chunkIndex;
    }
  }
}
