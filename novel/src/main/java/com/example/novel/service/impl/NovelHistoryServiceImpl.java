package com.example.novel.service.impl;

import com.example.novel.dto.response.NovelMessageResponse;
import com.example.novel.dto.response.NovelSessionResponse;
import com.example.novel.dto.response.NovelReferenceResponse;
import com.example.novel.dto.response.NovelToolCallResponse;
import com.example.novel.entity.NovelMessage;
import com.example.novel.entity.NovelSession;
import com.example.novel.mapper.NovelMessageMapper;
import com.example.novel.mapper.NovelSessionMapper;
import com.example.novel.mapper.NovelReferenceMapper;
import com.example.novel.mapper.NovelToolCallMapper;
import com.example.novel.service.NovelHistoryService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class NovelHistoryServiceImpl implements NovelHistoryService {

  private final NovelSessionMapper sessionMapper;
  private final NovelMessageMapper messageMapper;
  private final NovelReferenceMapper referenceMapper;
  private final NovelToolCallMapper toolCallMapper;

  @Override
  public Mono<List<NovelSessionResponse>> getRecentSessions(int limit) {
    return Mono.fromCallable(() -> sessionMapper.selectRecent(Math.max(1, limit)))
        .map(list -> list.stream().map(this::toSessionResp).collect(Collectors.toList()));
  }

  @Override
  public Mono<List<NovelMessageResponse>> getSessionMessages(Long sessionId, int limit) {
    return Mono.fromCallable(() -> messageMapper.selectBySessionIdWithLimit(sessionId, limit))
        .map(list -> list.stream().map(this::toMessageResp).collect(Collectors.toList()));
  }

  @Override
  public Mono<List<NovelReferenceResponse>> getReferences(Long sessionId, Long messageId) {
    return Mono.fromCallable(
            () -> {
              if (messageId != null) return referenceMapper.selectByMessageId(messageId);
              if (sessionId != null) return referenceMapper.selectBySessionId(sessionId);
              return java.util.Collections.<com.example.novel.entity.NovelReference>emptyList();
            })
        .map(list -> list.stream().map(this::toReferenceResp).collect(Collectors.toList()));
  }

  @Override
  public Mono<List<NovelToolCallResponse>> getToolCalls(Long sessionId, Long messageId) {
    return Mono.fromCallable(
            () -> {
              if (messageId != null) return toolCallMapper.selectByMessageId(messageId);
              if (sessionId != null) return toolCallMapper.selectBySessionId(sessionId);
              return java.util.Collections.<com.example.novel.entity.NovelToolCall>emptyList();
            })
        .map(list -> list.stream().map(this::toToolCallResp).collect(Collectors.toList()));
  }

  private NovelSessionResponse toSessionResp(NovelSession s) {
    NovelSessionResponse r = new NovelSessionResponse();
    r.setId(s.getId());
    r.setTitle(s.getTitle());
    r.setModel(s.getModel());
    r.setTemperature(s.getTemperature());
    r.setMaxTokens(s.getMaxTokens());
    r.setTopP(s.getTopP());
    r.setCreatedAt(s.getCreatedAt());
    r.setUpdatedAt(s.getUpdatedAt());
    return r;
  }

  private NovelMessageResponse toMessageResp(NovelMessage m) {
    NovelMessageResponse r = new NovelMessageResponse();
    r.setId(m.getId());
    r.setSessionId(m.getSessionId());
    r.setRole(m.getRole());
    r.setContent(m.getContent());
    r.setCreatedAt(m.getCreatedAt());
    return r;
  }

  private NovelReferenceResponse toReferenceResp(com.example.novel.entity.NovelReference e) {
    NovelReferenceResponse r = new NovelReferenceResponse();
    r.setId(e.getId());
    r.setSessionId(e.getSessionId());
    r.setMessageId(e.getMessageId());
    r.setSource(e.getSource());
    r.setTitle(e.getTitle());
    r.setExcerpt(e.getExcerpt());
    r.setSimilarity(e.getSimilarity());
    r.setUrl(e.getUrl());
    r.setCreatedAt(e.getCreatedAt());
    return r;
  }

  private NovelToolCallResponse toToolCallResp(com.example.novel.entity.NovelToolCall e) {
    NovelToolCallResponse r = new NovelToolCallResponse();
    r.setId(e.getId());
    r.setSessionId(e.getSessionId());
    r.setMessageId(e.getMessageId());
    r.setToolName(e.getToolName());
    r.setInputJson(e.getInputJson());
    r.setResultJson(e.getResultJson());
    r.setStatus(e.getStatus());
    r.setErrorMessage(e.getErrorMessage());
    r.setCreatedAt(e.getCreatedAt());
    r.setUpdatedAt(e.getUpdatedAt());
    return r;
  }
}
