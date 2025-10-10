package com.example.novel.controller;

import com.example.novel.dto.response.NovelMessageResponse;
import com.example.novel.dto.response.NovelReferenceResponse;
import com.example.novel.dto.response.NovelSessionResponse;
import com.example.novel.dto.response.NovelToolCallResponse;
import com.example.novel.service.NovelHistoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/novel/history")
@CrossOrigin(origins = "*")
public class NovelHistoryController {

  private final NovelHistoryService historyService;

  @GetMapping(value = "/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<List<NovelSessionResponse>> listRecentSessions(
      @RequestParam(name = "limit", defaultValue = "20") int limit) {
    log.info("List recent novel sessions, limit={}", limit);
    return historyService.getRecentSessions(limit);
  }

  @GetMapping(value = "/messages", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<List<NovelMessageResponse>> listSessionMessages(
      @RequestParam(name = "sessionId") Long sessionId,
      @RequestParam(name = "limit", defaultValue = "100") int limit) {
    log.info("List messages of session={}, limit={}", sessionId, limit);
    return historyService.getSessionMessages(sessionId, limit);
  }

  @GetMapping(value = "/references", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<List<NovelReferenceResponse>> listReferences(
      @RequestParam(name = "sessionId", required = false) Long sessionId,
      @RequestParam(name = "messageId", required = false) Long messageId) {
    log.info("List references sessionId={}, messageId={}", sessionId, messageId);
    return historyService.getReferences(sessionId, messageId);
  }

  @GetMapping(value = "/tool-calls", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<List<NovelToolCallResponse>> listToolCalls(
      @RequestParam(name = "sessionId", required = false) Long sessionId,
      @RequestParam(name = "messageId", required = false) Long messageId) {
    log.info("List tool-calls sessionId={}, messageId={}", sessionId, messageId);
    return historyService.getToolCalls(sessionId, messageId);
  }
}

