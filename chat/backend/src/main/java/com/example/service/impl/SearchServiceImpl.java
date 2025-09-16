package com.example.service.impl;

import com.example.config.SearchProperties;
import com.example.dto.request.TavilyRequest;
import com.example.dto.response.SearchResult;
import com.example.dto.response.TavilyResponse;
import com.example.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.example.constant.AiChatConstants.HTTP_STATUS_OK;

/**
 * 搜索服务实现类 - 统一搜索服务，支持Tool Calling
 * 
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchProperties searchProperties;
    private final ObjectMapper objectMapper;

    @Override
    public List<SearchResult> search(String query) {
        log.info("开始搜索，查询: {}", query);
        
        if (!isAvailable()) {
            log.warn("搜索服务不可用");
            return new ArrayList<>();
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(searchProperties.getTavily().getBaseUrl());

            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json");

            // 构建请求体
            TavilyRequest request = TavilyRequest.createBasic(
                searchProperties.getTavily().getApiKey(), query);

            String jsonRequest = objectMapper.writeValueAsString(request);
            httpPost.setEntity(new StringEntity(jsonRequest, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode == HTTP_STATUS_OK) {
                    return parseTavilyResponse(responseString);
                } else {
                    log.error("Tavily搜索API调用失败，状态码: {}, 响应: {}", statusCode, responseString);
                    return new ArrayList<>();
                }
            }
        } catch (Exception e) {
            log.error("Tavily搜索API调用异常: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean isAvailable() {
        return searchProperties.isEnabled() && 
               searchProperties.getTavily() != null &&
               searchProperties.getTavily().getApiKey() != null &&
               !searchProperties.getTavily().getApiKey().isEmpty();
    }

    /**
     * 解析Tavily API响应
     */
    private List<SearchResult> parseTavilyResponse(String responseString) {
        try {
            TavilyResponse tavilyResponse = objectMapper.readValue(responseString, TavilyResponse.class);

            List<SearchResult> results = new ArrayList<>();

            // 首先添加AI生成的答案（如果有）
            // 注意：AI 摘要不应作为“可点击来源”，因此不设置URL
            if (tavilyResponse.getAnswer() != null && !tavilyResponse.getAnswer().isEmpty()) {
                SearchResult answerResult = SearchResult.create(
                    "AI 摘要", null, null, tavilyResponse.getAnswer());
                results.add(answerResult);
            }

            // 解析搜索结果
            if (tavilyResponse.getResults() != null && !tavilyResponse.getResults().isEmpty()) {
                for (TavilyResponse.TavilySearchResult item : tavilyResponse.getResults()) {
                    SearchResult result = item.toSearchResult();
                    results.add(result);
                }
            }

            log.info("搜索完成，返回 {} 条结果", results.size());
            return results;
        } catch (Exception e) {
            log.error("解析Tavily API响应失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
