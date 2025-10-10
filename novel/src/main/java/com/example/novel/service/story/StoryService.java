package com.example.novel.service.story;

import com.example.novel.dto.request.story.OutlineRequest;
import com.example.novel.dto.request.story.ReviseRequest;
import com.example.novel.dto.request.story.StoryInitRequest;
import com.example.novel.dto.response.story.SegmentDTO;
import com.example.novel.dto.response.story.StoryInitResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StoryService {
  Mono<StoryInitResponse> init(StoryInitRequest req);

  Mono<Boolean> submitOutline(Long sessionId, OutlineRequest outline);

  Mono<SegmentDTO> getCurrent(Long sessionId);

  Flux<String> generate(Long sessionId, int segmentIndex);

  Flux<String> revise(Long sessionId, int segmentIndex, ReviseRequest feedback);

  Mono<Boolean> approve(Long sessionId, int segmentIndex);

  Flux<SegmentDTO> listSegments(Long sessionId);
}

