package com.example.novel.service.impl;

import static com.example.novel.constant.NovelConstants.DEFAULT_SESSION_TITLE;
import static com.example.novel.constant.NovelConstants.ROLE_USER;

import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import com.example.dto.stream.ChatEvent;
import com.example.handler.ChatErrorHandler;
import com.example.novel.converter.NovelModelResponseConverter;
import com.example.novel.dto.request.NovelStreamRequest;
import com.example.novel.dto.response.ModelListResponse;
import com.example.novel.service.NovelService;
import com.example.service.BaseChatService;
import com.example.service.catalog.ModelCatalogService;
import com.example.stream.TextStreamRequest;
import com.example.stream.springai.SpringAiTextStreamClient;
import com.example.strategy.model.ModelSelector;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class NovelServiceImpl extends BaseChatService implements NovelService {

  private final ChatErrorHandler errorHandler;
  private final ModelSelector modelSelector;
  private final ModelCatalogService modelCatalogService;
  private final com.example.novel.mapper.NovelSessionMapper novelSessionMapper;
  private final com.example.novel.mapper.NovelMessageMapper novelMessageMapper;
  private final MultiModelProperties multiModelProperties;
  private final NovelModelResponseConverter modelResponseConverter;

  public NovelServiceImpl(
      ChatErrorHandler errorHandler,
      SpringAiTextStreamClient textStreamClient,
      ModelSelector modelSelector,
      ModelCatalogService modelCatalogService,
      com.example.novel.mapper.NovelSessionMapper novelSessionMapper,
      com.example.novel.mapper.NovelMessageMapper novelMessageMapper,
      MultiModelProperties multiModelProperties,
      NovelModelResponseConverter modelResponseConverter) {
    super(textStreamClient);
    this.errorHandler = errorHandler;
    this.modelSelector = modelSelector;
    this.modelCatalogService = modelCatalogService;
    this.novelSessionMapper = novelSessionMapper;
    this.novelMessageMapper = novelMessageMapper;
    this.multiModelProperties = multiModelProperties;
    this.modelResponseConverter = modelResponseConverter;
  }

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
    List<ModelInfo> safeInfos = modelInfos != null ? modelInfos : Collections.emptyList();
    ModelListResponse response = new ModelListResponse();
    response.setModels(
        safeInfos.stream().map(modelResponseConverter::convert).collect(Collectors.toList()));
    return response;
  }

  @Override
  public Flux<Object> streamGenerate(NovelStreamRequest request) {
    String defaultProvider = multiModelProperties.getDefaultProvider();
    var selected = modelSelector.selectModelForUser(null, defaultProvider, request.getModel());

    // 创建会话并保存用户消息
    var session = new com.example.novel.entity.NovelSession();
    session.setTitle(DEFAULT_SESSION_TITLE);
    session.setModel(selected.modelName());
    session.setTemperature(request.getTemperature());
    session.setMaxTokens(request.getMaxTokens());
    session.setTopP(request.getTopP());
    novelSessionMapper.insert(session);

    var userMsg = new com.example.novel.entity.NovelMessage();
    userMsg.setSessionId(session.getId());
    userMsg.setRole(ROLE_USER);
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

    Flux<ChatEvent> streamFlux =
        streamText(
            req,
            (requestSpec, content) ->
                Mono.fromSupplier(
                    () -> {
                      // 注意：assistant消息已由MessageChatMemoryAdvisor自动保存，无需手动保存
                      return ChatEvent.end(null);
                    }));

    return Flux.concat(Flux.just(ChatEvent.start("novel-generation")), streamFlux)
        .onErrorResume(errorHandler::handleChatError)
        .cast(Object.class);
  }

}
