package com.example.novel.service.impl;

import com.example.dto.common.ModelInfo;
import com.example.config.MultiModelProperties;
import com.example.dto.stream.ChatEvent;
import com.example.handler.ChatErrorHandler;
import com.example.novel.dto.request.NovelStreamRequest;
import com.example.novel.dto.response.ModelListResponse;
import com.example.novel.service.NovelService;
import com.example.service.catalog.ModelCatalogService;
import com.example.stream.TextStreamRequest;
import com.example.stream.springai.SpringAiTextStreamClient;
import com.example.strategy.model.ModelSelector;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class NovelServiceImpl implements NovelService {

  private final ChatErrorHandler errorHandler;
  private final SpringAiTextStreamClient textStreamClient;
  private final ModelSelector modelSelector;
  private final ModelCatalogService modelCatalogService;
  private final com.example.novel.mapper.NovelSessionMapper novelSessionMapper;
  private final com.example.novel.mapper.NovelMessageMapper novelMessageMapper;
  private final MultiModelProperties multiModelProperties;

  @Override
  public Mono<ModelListResponse> getAvailableModels() {
    return Mono.fromSupplier(
            () -> buildModelListResponse(modelCatalogService.listModels()))
        .onErrorResume(
            error -> {
              log.warn("获取模型列表失败: {}", error.getMessage());
              return Mono.just(buildModelListResponse(Collections.emptyList()));
            });
  }

  private ModelListResponse buildModelListResponse(List<ModelInfo> modelInfos) {
    List<ModelInfo> safeInfos =
        modelInfos != null ? modelInfos : Collections.emptyList();
    ModelListResponse response = new ModelListResponse();
    response.setModels(
        safeInfos.stream().map(this::toResponseModel).collect(Collectors.toList()));
    return response;
  }

  private ModelListResponse.ModelInfo toResponseModel(ModelInfo info) {
    ModelListResponse.ModelInfo dto = new ModelListResponse.ModelInfo();
    dto.setName(info.getName());
    dto.setDisplayName(
        info.getDisplayName() != null ? info.getDisplayName() : info.getName());
    dto.setAvailable(info.getAvailable() == null ? Boolean.TRUE : info.getAvailable());
    dto.setSize(null);
    dto.setModifiedAt(null);
    return dto;
  }

  @Override
  public Flux<Object> streamGenerate(NovelStreamRequest request) {
    String defaultProvider = multiModelProperties.getDefaultProvider();
    var selected = modelSelector.selectModelForUser(null, defaultProvider, request.getModel());

    // 创建会话并保存用户消息
    var session = new com.example.novel.entity.NovelSession();
    session.setTitle("创作会话");
    session.setModel(selected.modelName());
    session.setTemperature(request.getTemperature());
    session.setMaxTokens(request.getMaxTokens());
    session.setTopP(request.getTopP());
    novelSessionMapper.insert(session);

    var userMsg = new com.example.novel.entity.NovelMessage();
    userMsg.setSessionId(session.getId());
    userMsg.setRole("user");
    userMsg.setContent(request.getPrompt());
    novelMessageMapper.insert(userMsg);

    // 构建流式请求 - RAG将由NovelRagAdvisor自动处理
    TextStreamRequest req =
        TextStreamRequest.builder()
            .provider(selected.providerName())
            .model(selected.modelName())
            .prompt(request.getPrompt())
            .temperature(request.getTemperature())
            .maxTokens(request.getMaxTokens())
            .topP(request.getTopP())
            .conversationId(session.getId()) // 设置会话ID以触发Memory和RAG Advisor
            .searchEnabled(false)
            .deepThinking(false)
            .build();

    var source = textStreamClient.stream(req);
    var hot = source.replay().autoConnect(2);

    return Flux.concat(
            Flux.just(ChatEvent.start("novel-generation")),
            Flux.merge(
                hot.map(ChatEvent::chunk),
                hot.scanWith(StringBuilder::new, (sb, c) -> sb.append(c))
                    .takeLast(1)
                    .doOnNext(
                        sb -> {
                          // 注意：assistant消息已由MessageChatMemoryAdvisor自动保存
                          // 无需手动保存
                        })
                    .thenMany(Flux.just(ChatEvent.end(null)))))
        .onErrorResume(errorHandler::handleChatError)
        .cast(Object.class);
  }

}
