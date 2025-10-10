package com.example.novel.service.rag;

import com.example.novel.dto.request.RagImportRequest;
import com.example.novel.dto.request.RagSearchRequest;
import com.example.novel.dto.response.RagImportResponse;
import com.example.novel.dto.response.RagSearchResponse;
import com.example.novel.dto.request.RagCrawlRequest;
import com.example.novel.dto.response.RagCrawlResponse;
import com.example.novel.dto.response.RagMaterialsResponse;
import reactor.core.publisher.Mono;

public interface RagService {
    Mono<RagImportResponse> importMaterials(RagImportRequest request);
    Mono<RagSearchResponse> searchMaterials(RagSearchRequest request);
    Mono<RagCrawlResponse> crawlAndImport(RagCrawlRequest request);
    Mono<RagMaterialsResponse> listMaterials();
}
