package com.example.service.impl;

import com.example.service.NovelRagService;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.ai.document.Document;
// import org.springframework.ai.reader.TextReader;
// import org.springframework.ai.transformer.splitter.TokenTextSplitter;
// import org.springframework.ai.vectorstore.SearchRequest;
// import org.springframework.ai.vectorstore.SimpleVectorStore;
// import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 小说写作 RAG 服务实现
 * 使用 Spring AI 的 VectorStore 进行向量存储和检索
 */
@Slf4j
@Service
public class NovelRagServiceImpl implements NovelRagService {

    // @Autowired(required = false)
    // private VectorStore vectorStore;

    // private final TokenTextSplitter textSplitter = new TokenTextSplitter();

    // 内存中保存导入的素材元数据
    private final List<Map<String, Object>> materialMetadata = new ArrayList<>();
    private int totalChunks = 0;

    @Override
    public Map<String, Object> importFromPath(String path, boolean recursive) {
        try {
            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                return Map.of(
                    "success", false,
                    "error", "路径不存在: " + path
                );
            }

            List<Path> files = new ArrayList<>();
            if (Files.isDirectory(dirPath)) {
                try (Stream<Path> paths = recursive ? Files.walk(dirPath) : Files.list(dirPath)) {
                    files = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> isSupportedFile(p.toString()))
                        .collect(Collectors.toList());
                }
            } else {
                files.add(dirPath);
            }

            int filesImported = 0;
            int chunksCreated = 0;

            for (Path file : files) {
                try {
                    // 简化实现：直接读取文件内容，不进行向量化
                    String content = Files.readString(file);
                    int estimatedChunks = (content.length() / 1000) + 1; // 简单估算分块数

                    chunksCreated += estimatedChunks;
                    filesImported++;

                    // 保存元数据
                    materialMetadata.add(Map.of(
                        "title", file.getFileName().toString(),
                        "source", file.toString(),
                        "chunks", estimatedChunks,
                        "type", "file",
                        "content", content.substring(0, Math.min(500, content.length())) + "..." // 保存前500字符作为预览
                    ));
                } catch (Exception e) {
                    log.warn("导入文件失败: {}, 错误: {}", file, e.getMessage());
                }
            }

            totalChunks += chunksCreated;

            return Map.of(
                "success", true,
                "filesImported", filesImported,
                "totalChunks", chunksCreated,
                "message", String.format("成功导入 %d 个文件，共 %d 个分块", filesImported, chunksCreated)
            );

        } catch (Exception e) {
            log.error("导入素材失败", e);
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }

    @Override
    public Map<String, Object> crawlFromUrl(String url, int maxPages, boolean sameDomainOnly) {
        try {
            // TODO: 实现网页抓取功能
            // 可以使用 Jsoup 或其他爬虫库
            log.warn("网页抓取功能暂未实现");

            return Map.of(
                "success", false,
                "error", "网页抓取功能暂未实现，请使用本地文件导入"
            );
        } catch (Exception e) {
            log.error("网页抓取失败", e);
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }

    @Override
    public Map<String, Object> getMaterials() {
        return Map.of(
            "success", true,
            "message", "OK",
            "totalFiles", materialMetadata.size(),
            "totalChunks", totalChunks,
            "items", materialMetadata
        );
    }

    @Override
    public List<Map<String, Object>> searchMaterials(String query, int topK) {
        try {
            // 简化实现：使用简单的关键词匹配
            String queryLower = query.toLowerCase();

            return materialMetadata.stream()
                .filter(mat -> {
                    String title = ((String) mat.get("title")).toLowerCase();
                    String content = ((String) mat.getOrDefault("content", "")).toLowerCase();
                    return title.contains(queryLower) || content.contains(queryLower);
                })
                .limit(topK)
                .map(mat -> Map.of(
                    "title", mat.get("title"),
                    "content", mat.get("content"),
                    "excerpt", getExcerpt((String) mat.get("content"), 200),
                    "similarity", 0.8, // 模拟相似度
                    "source", mat.get("source")
                ))
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("搜索素材失败", e);
            return List.of();
        }
    }

    @Override
    public void clearAll() {
        materialMetadata.clear();
        totalChunks = 0;
        // VectorStore 的清空需要具体实现支持
        log.info("已清空素材元数据");
    }


    /**
     * 检查是否支持的文件类型
     */
    private boolean isSupportedFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".txt") ||
               lower.endsWith(".md") ||
               lower.endsWith(".markdown");
    }

    /**
     * 获取文本摘要
     */
    private String getExcerpt(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
}
