package com.example.novel.service.story;

import com.example.dto.stream.ChatEvent;
import com.example.novel.dto.request.story.OutlineRequest;
import com.example.novel.dto.request.story.ReviseRequest;
import com.example.novel.dto.request.story.StoryInitRequest;
import com.example.novel.dto.response.story.SegmentDTO;
import com.example.novel.dto.response.story.StoryInitResponse;
import com.example.novel.service.rag.RagService;
import com.example.novel.dto.request.RagSearchRequest;
import com.example.novel.dto.response.RagSearchResponse;
import com.example.stream.TextStreamRequest;
import com.example.stream.springai.SpringAiTextStreamClient;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class InMemoryStoryService implements StoryService {

  private final SpringAiTextStreamClient textClient;
  private final RagService ragService;

  private static class Session {
    long id;
    String background;
    String style;
    String title;
    int currentIdx = 1;
    List<Segment> segments = new ArrayList<>();
  }

  private static class Segment {
    int idx;
    String title;
    String prompt;
    String starter;
    String status = "draft"; // draft/approved/needs_revision
    int version = 0;
    String latestText;
  }

  private final Map<Long, Session> store = new ConcurrentHashMap<>();
  private volatile long seq = 1000;

  @Override
  public Mono<StoryInitResponse> init(StoryInitRequest req) {
    return Mono.fromCallable(() -> {
      Session s = new Session();
      s.id = ++seq;
      s.background = req.getBackground();
      s.style = req.getStyle();
      s.title = req.getTitle();
      store.put(s.id, s);
      StoryInitResponse r = new StoryInitResponse();
      r.setSuccess(true);
      r.setSessionId(s.id);
      r.setMessage("created");
      return r;
    });
  }

  @Override
  public Mono<Boolean> submitOutline(Long sessionId, OutlineRequest outline) {
    return Mono.fromCallable(() -> {
      Session s = store.get(sessionId);
      if (s == null) throw new IllegalArgumentException("session not found");
      if (outline == null || outline.getItems() == null || outline.getItems().isEmpty()) {
        throw new IllegalArgumentException("outline empty");
      }
      s.segments.clear();
      int i = 1;
      for (OutlineRequest.Item it : outline.getItems()) {
        Segment sg = new Segment();
        sg.idx = i++;
        sg.title = it.getTitle();
        sg.prompt = it.getPrompt();
        sg.starter = it.getStarter();
        s.segments.add(sg);
      }
      s.currentIdx = 1;
      return true;
    });
  }

  @Override
  public Mono<SegmentDTO> getCurrent(Long sessionId) {
    return Mono.fromCallable(() -> toDTO(currentSeg(sessionId)));
  }

  @Override
  public Flux<String> generate(Long sessionId, int segmentIndex) {
    // 重新生成时应替换旧内容，而不是在旧内容后追加
    Segment seg = getSeg(sessionId, segmentIndex);
    seg.latestText = null;
    return generateInternal(sessionId, segmentIndex, null);
  }

  @Override
  public Flux<String> revise(Long sessionId, int segmentIndex, ReviseRequest feedback) {
    // 重写时先清空旧内容
    Segment seg = getSeg(sessionId, segmentIndex);
    seg.latestText = null;
    return generateInternal(sessionId, segmentIndex, feedback != null ? feedback.getFeedback() : null);
  }

  @Override
  public Mono<Boolean> approve(Long sessionId, int segmentIndex) {
    return Mono.fromCallable(() -> {
      Segment seg = getSeg(sessionId, segmentIndex);
      seg.status = "approved";
      Session s = store.get(sessionId);
      if (segmentIndex == s.currentIdx) {
        s.currentIdx = Math.min(segmentIndex + 1, s.segments.size());
      }
      return true;
    });
  }

  @Override
  public Flux<SegmentDTO> listSegments(Long sessionId) {
    return Flux.fromIterable(store.getOrDefault(sessionId, new Session()).segments).map(this::toDTO);
  }

  private Flux<String> generateInternal(Long sessionId, int segmentIndex, String feedback) {
    Session s = store.get(sessionId);
    if (s == null) return Flux.error(new IllegalArgumentException("session not found"));
    Segment seg = getSeg(sessionId, segmentIndex);

    StringBuilder prompt = new StringBuilder();
    if (s.title != null && !s.title.isBlank()) prompt.append("【作品】").append(s.title).append("\n");
    if (s.background != null && !s.background.isBlank()) prompt.append("【背景】").append(s.background).append("\n");
    if (s.style != null && !s.style.isBlank()) prompt.append("【风格】").append(s.style).append("\n");

    // 已通过段的简要串联
    StringBuilder approved = new StringBuilder();
    for (Segment x : s.segments) {
      if ("approved".equals(x.status) && x.latestText != null) {
        approved.append("- ").append(x.title != null ? x.title + "：" : "").append(summary(x.latestText)).append("\n");
      }
    }
    if (approved.length() > 0) {
      prompt.append("【已定稿摘要】\n").append(approved).append("\n");
    }

    prompt.append("【当前段落】").append("#").append(segmentIndex).append(" ")
        .append(seg.title != null ? seg.title : "").append("\n");
    if (seg.prompt != null) prompt.append("要点：").append(seg.prompt).append("\n");
    if (seg.starter != null) prompt.append("开头：").append(seg.starter).append("\n");
    if (feedback != null && !feedback.isBlank()) prompt.append("修订意见：").append(feedback).append("\n");

    // RAG 检索
    try {
      RagSearchRequest rq = new RagSearchRequest();
      rq.setQuery(seg.prompt != null ? seg.prompt : (seg.title != null ? seg.title : ""));
      rq.setTopK(5);
      rq.setMinSimilarity(0.2);
      RagSearchResponse rr = ragService.searchMaterials(rq).block();
      if (rr != null && Boolean.TRUE.equals(rr.getSuccess()) && rr.getResults() != null && !rr.getResults().isEmpty()) {
        StringBuilder refs = new StringBuilder();
        int i = 1;
        for (var r : rr.getResults()) {
          refs.append(i++).append(". ");
          if (r.getTitle() != null) refs.append("《").append(r.getTitle()).append("》 ");
          if (r.getExcerpt() != null) refs.append(r.getExcerpt());
          refs.append("\n");
        }
        prompt.append("【参考素材】\n").append(refs).append("\n");
      }
    } catch (Exception ignore) {}

    // 根据是否有反馈意见调整要求
    if (feedback != null && !feedback.isBlank()) {
      prompt.append("【写作要求】\n")
          .append("- ⚠️ 必须使用中文撰写！这是硬性要求！\n")
          .append("- 根据上述修订意见重新撰写【当前段落】的内容；\n")
          .append("- ⚠️ 重要：本段落字数必须达到1000-1500字。这是硬性要求，请务必通过增加细节描写、心理活动、环境渲染、对话等方式充实内容；\n")
          .append("- 段内需有完整的起承转合结构，包含：开头铺垫、情节发展、高潮冲突、结尾收束；\n")
          .append("- 增加具体的细节描写：人物表情、动作、环境氛围、心理活动等；\n")
          .append("- 仅撰写当前段落，不要剧透后续情节，不要推进到未给出的内容；\n")
          .append("- 语言自然流畅，延续既定风格；与已定稿摘要保持一致性；\n")
          .append("- 不要输出后续大纲、下一段/下一章内容，也不要总结全书；\n")
          .append("- 生成结束时不要添加额外说明。\n");
    } else {
      prompt.append("【写作要求】\n")
          .append("- ⚠️ 必须使用中文撰写！这是硬性要求！\n")
          .append("- ⚠️ 重要：本段落字数必须达到800-1200字。这是硬性要求，请务必通过增加细节描写、心理活动、环境渲染等方式充实内容；\n")
          .append("- 段内需有完整的起承转合结构，包含：开头铺垫、情节发展、结尾收束；\n")
          .append("- 增加具体的细节描写：人物表情、动作、环境氛围、心理活动等；\n")
          .append("- 仅撰写【当前段落】的内容，不要剧透、不要推进到未给出的后续情节；\n")
          .append("- 语言自然流畅，延续既定风格；与已定稿摘要保持一致性；\n")
          .append("- 不要输出后续大纲、下一段/下一章内容，也不要总结全书；\n")
          .append("- 生成结束时不要添加额外说明。\n");
    }

    seg.version += 1;
    seg.status = feedback == null ? "draft" : "needs_revision";

    // 使用 null 让系统从 application.yml 读取 spring.ai.ollama 配置
    TextStreamRequest req = TextStreamRequest.builder()
        .provider(null)  // null 表示使用默认 provider（Ollama，从配置文件读取）
        .model(null)     // null 表示使用默认 model（从 spring.ai.ollama.chat.options.model 读取）
        .prompt(prompt.toString())
        .temperature(0.7)
        .maxTokens(null)  // 不设置上限，让模型根据提示词要求生成足够长度
        .topP(0.9)
        .searchEnabled(false)
        .deepThinking(false)
        .build();

    java.util.concurrent.atomic.AtomicReference<StringBuilder> acc = new java.util.concurrent.atomic.AtomicReference<>(new StringBuilder());

    return textClient.stream(req)
        .doOnSubscribe(sub -> seg.latestText = null)
        .doOnNext(chunk -> {
          if (chunk != null && !chunk.isBlank()) {
            acc.get().append(chunk);
          }
        })
        .doOnComplete(() -> seg.latestText = acc.get().toString());
  }

  private Segment getSeg(Long sessionId, int idx) {
    Session s = store.get(sessionId);
    if (s == null || idx < 1 || idx > s.segments.size()) throw new IllegalArgumentException("segment not found");
    return s.segments.get(idx - 1);
  }

  private Segment currentSeg(Long sessionId) {
    Session s = store.get(sessionId);
    if (s == null || s.segments.isEmpty()) throw new IllegalArgumentException("no outline");
    return getSeg(sessionId, s.currentIdx);
  }

  private SegmentDTO toDTO(Segment seg) {
    SegmentDTO d = new SegmentDTO();
    d.setIndex(seg.idx);
    d.setTitle(seg.title);
    d.setPrompt(seg.prompt);
    d.setStarter(seg.starter);
    d.setStatus(seg.status);
    d.setVersion(seg.version);
    d.setLatestText(seg.latestText);
    return d;
  }

  private String summary(String text) {
    if (text == null) return "";
    String t = text.trim().replaceAll("\n+", " ");
    return t.length() > 60 ? t.substring(0, 60) + "…" : t;
  }
}
