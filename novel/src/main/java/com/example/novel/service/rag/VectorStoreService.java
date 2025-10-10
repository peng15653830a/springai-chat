package com.example.novel.service.rag;

import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

  private final org.springframework.beans.factory.ObjectProvider<VectorStore> vectorStoreProvider;
  @org.springframework.beans.factory.annotation.Value("${novel.rag.max-topk-cap:50}")
  private int topKCap;

  public boolean available() {
    return vectorStoreProvider.getIfAvailable() != null;
  }

  public void upsert(String source, List<DocumentSearchService.DocumentChunk> chunks) {
    VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
    if (vectorStore == null || chunks == null || chunks.isEmpty()) {
      return;
    }
    try {
      List<Document> docs = new ArrayList<>(chunks.size());
      for (var c : chunks) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("source", source);
        meta.put("title", c.getTitle());
        meta.put("chunkIndex", c.getChunkIndex());
        docs.add(new Document(c.getContent(), meta));
      }
      vectorStore.add(docs);
      log.debug("Upserted {} chunks into VectorStore for source {}", chunks.size(), source);
    } catch (Exception e) {
      log.warn("VectorStore upsert failed for {}: {}", source, e.getMessage());
    }
  }

  public List<Document> similaritySearch(String query, int topK, double minScore) {
    VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
    if (vectorStore == null) return List.of();
    try {
      SearchRequest req = SearchRequest.builder()
          .query(query)
          .topK(Math.max(1, Math.min(topK, Math.max(1, topKCap))))
          .similarityThreshold(Math.max(0.0, Math.min(minScore, 1.0)))
          .build();
      return vectorStore.similaritySearch(req);
    } catch (Exception e) {
      log.warn("VectorStore search failed: {}", e.getMessage());
      return List.of();
    }
  }
}
