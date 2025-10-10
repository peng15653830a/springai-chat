package com.example.novel.service;

import com.example.novel.dto.request.NovelStreamRequest;
import com.example.novel.dto.response.ModelListResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NovelService {
    Mono<ModelListResponse> getAvailableModels();
    Flux<Object> streamGenerate(NovelStreamRequest request);
}