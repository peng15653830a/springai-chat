package com.example.novel.service.rag;

import com.example.novel.dto.request.RagCrawlRequest;
import com.example.novel.dto.request.RagImportRequest;
import com.example.novel.dto.response.RagCrawlResponse;
import com.example.novel.dto.response.RagImportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

/**
 * 内容爬取服务
 * 负责网页抓取、Feed解析等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentCrawlerService {

  private final DocumentChunkingService chunkingService;

  @Value("${novel.rag.material-path:./materials}")
  private String materialPath;
  @Value("${novel.rag.crawl.max-pages-cap:500}")
  private int maxPagesCap;
  @Value("${novel.rag.crawl.rate-limit-max-ms:2000}")
  private int rateLimitMaxMs;

  private final WebClient webClient = WebClient.builder()
      .defaultHeader(HttpHeaders.USER_AGENT,
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
      .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
      .build();

  /**
   * 爬取并导入（Jsoup 解析 + 限定上限）
   */
  public Mono<RagCrawlResponse> crawlAndImport(RagCrawlRequest request) {
    return Mono.fromCallable(() -> {
      RagCrawlResponse resp = new RagCrawlResponse();
      List<String> errors = new ArrayList<>();

      try {
        // 创建目标目录
        String baseDirName = "crawled_" + System.currentTimeMillis();
        Path baseDir = Paths.get(materialPath, baseDirName);
        Files.createDirectories(baseDir);

        int fetched = crawlWithLimit(request, baseDir, errors);

        // 导入到索引
        RagImportRequest importReq = new RagImportRequest();
        importReq.setPath(baseDir.toString());
        importReq.setRecursive(true);
        importReq.setFilePattern("*.txt");
        RagImportResponse importResp = chunkingService.importMaterials(importReq).block();

        resp.setSuccess(fetched > 0);
        resp.setMessage(fetched > 0 ? "抓取并导入完成" : "未抓取到内容");
        resp.setPagesFetched(fetched);
        resp.setTotalChunks(importResp != null ? importResp.getTotalChunks() : 0);

      } catch (Exception e) {
        log.error("网页抓取失败", e);
        resp.setSuccess(false);
        resp.setMessage("抓取失败: " + e.getMessage());
        errors.add(e.getMessage());
      }

      resp.setErrors(errors);
      return resp;
    });
  }

  private int crawlWithLimit(RagCrawlRequest request, Path baseDir, List<String> errors) {
    int reqMax = request.getMaxPages() != null ? request.getMaxPages() : 200;
    int maxPages = Math.max(1, Math.min(reqMax, Math.max(1, maxPagesCap)));
    boolean sameDomainOnly = Boolean.TRUE.equals(request.getSameDomainOnly());
    int rateMs = Math.max(0, request.getRateLimitMs() != null ? request.getRateLimitMs() : 300);
    rateMs = Math.min(rateMs, Math.max(0, rateLimitMaxMs));

    String start = request.getUrl();
    if (start == null || start.isBlank()) return 0;
    URI startUri = URI.create(start);
    String startHost = startUri.getHost();

    Set<String> visited = new HashSet<>();
    Deque<String> queue = new ArrayDeque<>();
    queue.add(start);

    int count = 0;
    int pageIndex = 1;
    while (!queue.isEmpty() && count < maxPages) {
      String url = queue.pollFirst();
      if (url == null || visited.contains(url)) continue;
      try {
        String html = webClient.get().uri(url)
            .accept(MediaType.TEXT_HTML)
            .retrieve()
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(20))
            .block();
        if (html == null || html.isBlank()) continue;

        Document doc = Jsoup.parse(html, url);
        String title = request.getTitleSelector() != null && !request.getTitleSelector().isBlank()
            ? optionalText(doc.selectFirst(request.getTitleSelector()))
            : doc.title();
        String bodyText;
        if (request.getContentSelector() != null && !request.getContentSelector().isBlank()) {
          Elements els = doc.select(request.getContentSelector());
          bodyText = els != null && !els.isEmpty() ? els.text() : doc.body().text();
        } else {
          bodyText = doc.body() != null ? doc.body().text() : "";
        }
        bodyText = bodyText.replaceAll("\u00A0", " ").replaceAll("\s+", " ").trim();

        Path file = baseDir.resolve(String.format("page_%03d.txt", pageIndex++));
        String header = (title != null ? title : "") + System.lineSeparator() + url + System.lineSeparator() + System.lineSeparator();
        Files.writeString(file, header + bodyText, StandardCharsets.UTF_8,
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        count++;
        visited.add(url);

        // 发现更多链接（限定上限、同域、包含/排除模式）
        for (Element a : doc.select("a[href]")) {
          String href = a.absUrl("href");
          if (href == null || href.isBlank()) continue;
          if (sameDomainOnly) {
            try { if (!Objects.equals(URI.create(href).getHost(), startHost)) continue; } catch (Exception ignore) {}
          }
          if (!matchIncludeExclude(href, request.getIncludePatterns(), request.getExcludePatterns())) continue;
          if (!visited.contains(href)) queue.addLast(href);
        }

        if (rateMs > 0) Thread.sleep(rateMs);
      } catch (Exception e) {
        errors.add("抓取失败 " + url + ": " + e.getMessage());
      }
    }
    return count;
  }

  private boolean matchIncludeExclude(String url, List<String> includes, List<String> excludes) {
    boolean ok = true;
    if (includes != null && !includes.isEmpty()) {
      ok = includes.stream().anyMatch(p -> url.matches(p));
    }
    if (!ok) return false;
    if (excludes != null && !excludes.isEmpty()) {
      if (excludes.stream().anyMatch(p -> url.matches(p))) return false;
    }
    return true;
  }

  private String optionalText(Element el) {
    return el != null ? el.text() : null;
  }
}
