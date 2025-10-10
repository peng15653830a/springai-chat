package com.example.novel.service.rag;

import com.example.novel.dto.request.RagCrawlRequest;
import com.example.novel.dto.request.RagImportRequest;
import com.example.novel.dto.request.RagSearchRequest;
import com.example.novel.dto.response.RagCrawlResponse;
import com.example.novel.dto.response.RagImportResponse;
import com.example.novel.dto.response.RagMaterialsResponse;
import com.example.novel.dto.response.RagSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG服务门面
 * 重构后的简化版本，委托给专门的服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

  private final DocumentSearchService searchService;
  private final DocumentChunkingService chunkingService;
  private final ContentCrawlerService crawlerService;

  @Override
  public Mono<RagImportResponse> importMaterials(RagImportRequest request) {
    return chunkingService.importMaterials(request);
  }

  @Override
  public Mono<RagSearchResponse> searchMaterials(RagSearchRequest request) {
    return searchService.searchMaterials(request);
  }

  @Override
  public Mono<RagMaterialsResponse> listMaterials() {
    return Mono.fromCallable(() -> {
      RagMaterialsResponse resp = new RagMaterialsResponse();
      try {
        Map<String, List<DocumentSearchService.DocumentChunk>> docs = searchService.getAllDocuments();

        List<RagMaterialsResponse.Item> items = docs.entrySet().stream()
            .map(e -> {
              RagMaterialsResponse.Item it = new RagMaterialsResponse.Item();
              it.setSource(e.getKey());
              List<DocumentSearchService.DocumentChunk> chunks = e.getValue();
              it.setChunks(chunks != null ? chunks.size() : 0);
              if (chunks != null && !chunks.isEmpty()) {
                it.setTitle(chunks.get(0).getTitle());
              }
              return it;
            })
            .sorted(java.util.Comparator.comparing(RagMaterialsResponse.Item::getSource))
            .collect(Collectors.toList());

        int totalChunks = items.stream().mapToInt(RagMaterialsResponse.Item::getChunks).sum();

        resp.setSuccess(true);
        resp.setMessage("OK");
        resp.setItems(items);
        resp.setTotalFiles(items.size());
        resp.setTotalChunks(totalChunks);
      } catch (Exception e) {
        resp.setSuccess(false);
        resp.setMessage("列表获取失败: " + e.getMessage());
        resp.setItems(java.util.Collections.emptyList());
        resp.setTotalFiles(0);
        resp.setTotalChunks(0);
      }
      return resp;
    });
  }

  @Override
  public Mono<RagCrawlResponse> crawlAndImport(RagCrawlRequest request) {
    return crawlerService.crawlAndImport(request);
  }
}
