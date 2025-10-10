package com.example.novel.service;

import com.example.novel.dto.response.NovelMessageResponse;
import com.example.novel.dto.response.NovelSessionResponse;
import com.example.novel.dto.response.NovelReferenceResponse;
import com.example.novel.dto.response.NovelToolCallResponse;
import java.util.List;
import reactor.core.publisher.Mono;

public interface NovelHistoryService {
  Mono<List<NovelSessionResponse>> getRecentSessions(int limit);
  Mono<List<NovelMessageResponse>> getSessionMessages(Long sessionId, int limit);
  Mono<List<NovelReferenceResponse>> getReferences(Long sessionId, Long messageId);
  Mono<List<NovelToolCallResponse>> getToolCalls(Long sessionId, Long messageId);
}
