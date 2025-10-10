package com.example.novel.service.rag;

import com.example.novel.dto.request.RagImportRequest;
import com.example.novel.dto.response.RagImportResponse;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.FileSystemResource;

/**
 * 文档分块服务
 * 负责文件导入、分块处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentChunkingService {

  private final DocumentSearchService searchService;
  private final VectorStoreService vectorStoreService;

  @Value("${novel.rag.chunk-size:500}")
  private int chunkSize;

  @Value("${novel.rag.chunk-overlap:50}")
  private int chunkOverlap;

  @Value("${novel.rag.chunk-tokens:0}")
  private int chunkTokens;

  @Value("${novel.rag.chunk-token-overlap:0}")
  private int chunkTokenOverlap;

  @Value("${novel.rag.material-path:./materials}")
  private String materialPath;

  /**
   * 预加载材料目录
   */
  @jakarta.annotation.PostConstruct
  public void preloadMaterials() {
    try {
      Path root = Paths.get(materialPath);
      if (Files.exists(root)) {
        RagImportRequest req = new RagImportRequest();
        req.setPath(root.toString());
        req.setRecursive(true);
        req.setFilePattern("*.txt");
        importMaterials(req).block();
        log.info("预加载RAG素材完成: {}", root);
      }
    } catch (Exception e) {
      log.warn("预加载RAG素材失败: {}", e.getMessage());
    }
  }

  /**
   * 导入材料
   */
  public Mono<RagImportResponse> importMaterials(RagImportRequest request) {
    return Mono.fromCallable(() -> {
      RagImportResponse response = new RagImportResponse();
      List<String> errors = new ArrayList<>();
      AtomicInteger totalFiles = new AtomicInteger(0);
      AtomicInteger processedFiles = new AtomicInteger(0);
      AtomicInteger totalChunks = new AtomicInteger(0);

      try {
        Path rootPath = Paths.get(request.getPath());
        if (!Files.exists(rootPath)) {
          response.setSuccess(false);
          response.setMessage("指定路径不存在: " + request.getPath());
          return response;
        }

        List<String> patterns = Arrays.asList(request.getFilePattern().split(","));

        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (shouldProcessFile(file, patterns)) {
              totalFiles.incrementAndGet();
              try {
                List<DocumentSearchService.DocumentChunk> chunks = processFile(file);
                searchService.indexDocument(file.toString(), chunks);
                // 向量化入库（若已配置 VectorStore）
                vectorStoreService.upsert(file.toString(), chunks);
                totalChunks.addAndGet(chunks.size());
                processedFiles.incrementAndGet();
                log.debug("处理文件: {}, 分块数: {}", file, chunks.size());
              } catch (Exception e) {
                errors.add("处理文件失败 " + file + ": " + e.getMessage());
                log.error("处理文件失败: {}", file, e);
              }
            }
            return request.getRecursive() ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
          }
        });

        response.setSuccess(true);
        response.setMessage("素材导入完成");
        response.setTotalFiles(totalFiles.get());
        response.setProcessedFiles(processedFiles.get());
        response.setTotalChunks(totalChunks.get());
        response.setErrors(errors);

      } catch (Exception e) {
        log.error("导入素材失败", e);
        response.setSuccess(false);
        response.setMessage("导入失败: " + e.getMessage());
        response.setErrors(Arrays.asList(e.getMessage()));
      }

      return response;
    });
  }

  /**
   * 判断是否应处理文件
   */
  private boolean shouldProcessFile(Path file, List<String> patterns) {
    String fileName = file.getFileName().toString().toLowerCase();
    return patterns.stream().anyMatch(pattern -> {
      String cleanPattern = pattern.trim().toLowerCase().replace("*", "");
      return fileName.endsWith(cleanPattern);
    });
  }

  /**
   * 处理单个文件
   */
  private List<DocumentSearchService.DocumentChunk> processFile(Path file) throws IOException {
    String content = extractText(file);
    String fileName = file.getFileName().toString();

    if (content == null) content = "";
    content = content.replaceAll("\r\n", "\n").replaceAll("\t", " ");

    List<DocumentSearchService.DocumentChunk> chunks = new ArrayList<>();
    if (chunkTokens > 0) {
      String[] tokens = content.split("\\s+");
      for (int i = 0, idx = 0; i < tokens.length; i += Math.max(1, chunkTokens - Math.max(0, chunkTokenOverlap))) {
        int end = Math.min(i + chunkTokens, tokens.length);
        StringBuilder sb = new StringBuilder();
        for (int t = i; t < end; t++) {
          sb.append(tokens[t]).append(' ');
        }
        String chunkContent = sb.toString().trim();
        DocumentSearchService.DocumentChunk chunk = new DocumentSearchService.DocumentChunk();
        chunk.setContent(chunkContent);
        chunk.setSource(file.toString());
        chunk.setTitle(fileName);
        chunk.setChunkIndex(idx++);
        chunks.add(chunk);
        if (end >= tokens.length) break;
      }
    } else {
      for (int i = 0; i < content.length(); i += Math.max(1, chunkSize - Math.max(0, chunkOverlap))) {
        int end = Math.min(i + chunkSize, content.length());
        String chunkContent = content.substring(i, end);

        DocumentSearchService.DocumentChunk chunk = new DocumentSearchService.DocumentChunk();
        chunk.setContent(chunkContent);
        chunk.setSource(file.toString());
        chunk.setTitle(fileName);
        chunk.setChunkIndex(chunks.size());
        chunks.add(chunk);
        if (end >= content.length()) break;
      }
    }

    return chunks;
  }

  private String extractText(Path file) throws IOException {
    String name = file.getFileName().toString().toLowerCase();
    FileSystemResource res = new FileSystemResource(file.toFile());
    try {
      if (name.endsWith(".md") || name.endsWith(".markdown")) {
        var reader = new MarkdownDocumentReader(res, MarkdownDocumentReaderConfig.defaultConfig());
        java.util.List<?> docs = reader.get();
        return docs.isEmpty() ? Files.readString(file) : reflectDocText(docs.get(0));
      }
      if (name.endsWith(".html") || name.endsWith(".htm")) {
        var reader = new JsoupDocumentReader(res);
        java.util.List<?> docs = reader.get();
        return docs.isEmpty() ? Files.readString(file) : reflectDocText(docs.get(0));
      }
      if (name.endsWith(".pdf")) {
        var reader = new PagePdfDocumentReader(res);
        java.util.List<?> docs = reader.get();
        if (!docs.isEmpty()) {
          StringBuilder sb = new StringBuilder();
          for (Object d : docs) sb.append(reflectDocText(d)).append('\n');
          return sb.toString();
        }
        return Files.readString(file);
      }
      var reader = new TikaDocumentReader(res);
      java.util.List<?> docs = reader.get();
      return docs.isEmpty() ? Files.readString(file) : reflectDocText(docs.get(0));
    } catch (Throwable e) {
      log.warn("文档读取失败，回退到直接读取: {}", e.getMessage());
      return Files.readString(file);
    }
  }

  private String reflectDocText(Object doc) {
    if (doc == null) return "";
    try {
      var m = doc.getClass().getMethod("getContent");
      Object v = m.invoke(doc);
      return v != null ? v.toString() : "";
    } catch (Exception ignore) {}
    try {
      var m = doc.getClass().getMethod("getText");
      Object v = m.invoke(doc);
      return v != null ? v.toString() : "";
    } catch (Exception ignore) {}
    return String.valueOf(doc);
  }
}
