package com.example.controller;

import com.example.dto.request.StreamChatRequest;
import com.example.dto.response.ApiResponse;
import com.example.dto.stream.ChatEvent;
import com.example.service.AiChatService;
import com.example.service.NovelRagService;
import com.example.sse.SseEventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 小说写作辅助控制器
 * 提供 RAG 素材管理、模型选择、流式生成等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/novel")
@CrossOrigin(origins = "*")
public class NovelController {

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private NovelRagService novelRagService;

    /**
     * 获取可用的模型列表
     */
    @GetMapping("/models")
    public ApiResponse<Map<String, Object>> getModels() {
        try {
            List<Map<String, String>> models = List.of(
                Map.of("name", "deepseek-chat", "provider", "deepseek", "displayName", "DeepSeek Chat"),
                Map.of("name", "qwen-max", "provider", "qwen", "displayName", "通义千问 Max"),
                Map.of("name", "moonshot-v1-8k", "provider", "kimi2", "displayName", "Kimi Chat"),
                Map.of("name", "greatwall-chat", "provider", "greatwall", "displayName", "长城大模型")
            );
            return ApiResponse.success(Map.of("models", models));
        } catch (Exception e) {
            log.error("获取模型列表失败", e);
            return ApiResponse.error("获取模型列表失败: " + e.getMessage());
        }
    }

    /**
     * 流式生成文本
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> streamGenerate(@RequestBody Map<String, Object> request) {
        try {
            String model = (String) request.get("model");
            String prompt = (String) request.get("prompt");
            Double temperature = request.containsKey("temperature")
                ? ((Number) request.get("temperature")).doubleValue()
                : 0.7;
            Integer maxTokens = request.containsKey("maxTokens")
                ? ((Number) request.get("maxTokens")).intValue()
                : 2000;

            // 创建临时对话请求
            StreamChatRequest chatRequest = new StreamChatRequest();
            chatRequest.setMessage(prompt);
            chatRequest.setModel(model);
            chatRequest.setConversationId(0L); // 临时对话，不保存

            return aiChatService.streamChat(chatRequest)
                .map(SseEventMapper::toSseEvent);
        } catch (Exception e) {
            log.error("流式生成失败", e);
            return Flux.error(e);
        }
    }

    /**
     * 导入本地素材文件夹
     */
    @PostMapping("/rag/import")
    public ApiResponse<Map<String, Object>> importMaterials(@RequestBody Map<String, Object> request) {
        try {
            String path = (String) request.get("path");
            boolean recursive = request.containsKey("recursive")
                ? (Boolean) request.get("recursive")
                : true;

            Map<String, Object> result = novelRagService.importFromPath(path, recursive);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("导入素材失败", e);
            return ApiResponse.error("导入素材失败: " + e.getMessage());
        }
    }

    /**
     * 从网页抓取素材
     */
    @PostMapping("/rag/crawl")
    public ApiResponse<Map<String, Object>> crawlFromUrl(@RequestBody Map<String, Object> request) {
        try {
            String url = (String) request.get("url");
            Integer maxPages = request.containsKey("maxPages")
                ? ((Number) request.get("maxPages")).intValue()
                : 200;
            Boolean sameDomainOnly = request.containsKey("sameDomainOnly")
                ? (Boolean) request.get("sameDomainOnly")
                : true;

            Map<String, Object> result = novelRagService.crawlFromUrl(url, maxPages, sameDomainOnly);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("网页抓取失败", e);
            return ApiResponse.error("网页抓取失败: " + e.getMessage());
        }
    }

    /**
     * 获取已导入的素材列表
     */
    @GetMapping("/rag/materials")
    public ApiResponse<Map<String, Object>> getMaterials() {
        try {
            Map<String, Object> materials = novelRagService.getMaterials();
            return ApiResponse.success(materials);
        } catch (Exception e) {
            log.error("获取素材列表失败", e);
            return ApiResponse.error("获取素材列表失败: " + e.getMessage());
        }
    }

    /**
     * 搜索素材
     */
    @PostMapping("/rag/search")
    public ApiResponse<Map<String, Object>> searchMaterials(@RequestBody Map<String, Object> request) {
        try {
            String query = (String) request.get("query");
            Integer topK = request.containsKey("topK")
                ? ((Number) request.get("topK")).intValue()
                : 5;

            List<Map<String, Object>> results = novelRagService.searchMaterials(query, topK);
            return ApiResponse.success(Map.of("results", results));
        } catch (Exception e) {
            log.error("搜索素材失败", e);
            return ApiResponse.error("搜索素材失败: " + e.getMessage());
        }
    }

    /**
     * 获取 MCP 工具列表（预留接口）
     */
    @GetMapping("/mcp/tools")
    public ApiResponse<Map<String, Object>> getMcpTools() {
        return ApiResponse.success(Map.of("tools", List.of()));
    }

    /**
     * 执行 MCP 工具（预留接口）
     */
    @PostMapping("/mcp/execute")
    public ApiResponse<Map<String, Object>> executeMcpTool(@RequestBody Map<String, Object> request) {
        return ApiResponse.error("MCP工具功能暂未实现");
    }
}
