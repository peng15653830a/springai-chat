package com.example.novel.controller;

import com.example.dto.stream.ChatEvent;
import com.example.novel.dto.request.story.OutlineRequest;
import com.example.novel.dto.request.story.ReviseRequest;
import com.example.novel.dto.request.story.StoryInitRequest;
import com.example.novel.dto.response.story.SegmentDTO;
import com.example.novel.dto.response.story.StoryInitResponse;
import com.example.novel.service.story.StoryService;
import com.example.sse.SseEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/novel/story")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StoryController {

  private final StoryService storyService;

  @PostMapping("/init")
  public Mono<StoryInitResponse> init(@RequestBody StoryInitRequest request) {
    log.info("init story");
    return storyService.init(request);
  }

  @PostMapping("/{sessionId}/outline")
  public Mono<Boolean> outline(@PathVariable Long sessionId, @RequestBody OutlineRequest req) {
    log.info("submit outline for session {}", sessionId);
    return storyService.submitOutline(sessionId, req);
  }

  @GetMapping("/{sessionId}/current")
  public Mono<SegmentDTO> current(@PathVariable Long sessionId) {
    return storyService.getCurrent(sessionId);
  }

  // EventSource 使用 GET，这里改为 GET 以兼容前端
  @GetMapping(value = "/{sessionId}/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<Object>> generate(@PathVariable Long sessionId, @RequestParam int segment) {
    log.info("enter generate session={} segment={}", sessionId, segment);
    Flux<ChatEvent> stream =
        Flux.concat(
            Flux.just(ChatEvent.start("story-generate")),
            storyService.generate(sessionId, segment).map(ChatEvent::chunk),
            Flux.just(ChatEvent.end(null))
        );
    return stream.map(SseEventMapper::toSseEvent)
        .doOnComplete(() -> log.info("generate done for session {} seg {}", sessionId, segment));
  }

  @PostMapping(value = "/{sessionId}/revise", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<Object>> revise(@PathVariable Long sessionId, @RequestParam int segment, @RequestBody ReviseRequest feedback) {
    return storyService.revise(sessionId, segment, feedback)
        .map(text -> ChatEvent.chunk(text))
        .map(SseEventMapper::toSseEvent)
        .doOnComplete(() -> log.info("revise done for session {} seg {}", sessionId, segment));
  }

  @PostMapping("/{sessionId}/approve")
  public Mono<Boolean> approve(@PathVariable Long sessionId, @RequestParam int segment) {
    return storyService.approve(sessionId, segment);
  }

  @GetMapping("/{sessionId}/segments")
  public Flux<SegmentDTO> list(@PathVariable Long sessionId) {
    return storyService.listSegments(sessionId);
  }
}
