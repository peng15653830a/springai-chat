package com.example.novel.controller;

import com.example.dto.stream.ChatEvent;
import com.example.novel.dto.request.NovelStreamRequest;
import com.example.novel.dto.request.RagImportRequest;
import com.example.novel.dto.request.RagSearchRequest;
import com.example.novel.dto.request.McpExecuteRequest;
import com.example.novel.dto.response.ModelListResponse;
import com.example.novel.dto.response.RagImportResponse;
import com.example.novel.dto.response.RagSearchResponse;
import com.example.novel.dto.response.RagMaterialsResponse;
import com.example.novel.dto.response.McpToolListResponse;
import com.example.novel.dto.response.McpExecuteResponse;
import com.example.novel.service.NovelService;
import com.example.novel.service.rag.RagService;
import com.example.novel.service.mcp.McpService;
import lombok.extern.slf4j.Slf4j;
import com.example.sse.SseEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/novel")
@CrossOrigin(origins = "*")
public class NovelController {

    @Autowired
    private NovelService novelService;

    @Autowired
    private RagService ragService;

    @Autowired
    private McpService mcpService;

    @GetMapping("/models")
    public Mono<ModelListResponse> getModels() {
        return novelService.getAvailableModels();
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> streamGenerate(@RequestBody NovelStreamRequest request) {
        log.info("Novel stream request: model={}, temperature={}", request.getModel(), request.getTemperature());
        return novelService.streamGenerate(request)
                .map(obj -> (ChatEvent) obj)
                .map(SseEventMapper::toSseEvent)
                .doOnNext(event -> log.debug("发送SSE事件: {}", event.event()))
                .doOnError(error -> log.error("Novel流式生成发生错误", error))
                .doOnComplete(() -> log.info("Novel流式生成完成"));
    }

    @PostMapping("/rag/import")
    public Mono<RagImportResponse> importMaterials(@RequestBody RagImportRequest request) {
        log.info("RAG import request: path={}", request.getPath());
        return ragService.importMaterials(request);
    }

    @PostMapping("/rag/search")
    public Mono<RagSearchResponse> searchMaterials(@RequestBody RagSearchRequest request) {
        log.info("RAG search request: query={}, topK={}", request.getQuery(), request.getTopK());
        return ragService.searchMaterials(request);
    }

    @GetMapping("/rag/materials")
    public Mono<RagMaterialsResponse> listMaterials() {
        log.info("RAG list materials request");
        return ragService.listMaterials();
    }

    @PostMapping("/rag/crawl")
    public Mono<com.example.novel.dto.response.RagCrawlResponse> crawl(@RequestBody com.example.novel.dto.request.RagCrawlRequest request) {
        log.info("RAG crawl request: url={} maxPages={}", request.getUrl(), request.getMaxPages());
        return ragService.crawlAndImport(request);
    }

    @GetMapping("/mcp/tools")
    public Mono<McpToolListResponse> getTools() {
        log.info("MCP tools list request");
        return mcpService.getAvailableTools();
    }

    @PostMapping("/mcp/execute")
    public Mono<McpExecuteResponse> executeTool(@RequestBody McpExecuteRequest request) {
        log.info("MCP execute request: tool={}", request.getToolName());
        return mcpService.executeTool(request);
    }
}
