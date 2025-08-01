package com.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class SearchService {
    
    @Value("${search.metaso.api-key:}")
    private String metasoApiKey;
    
    @Value("${search.metaso.enabled:true}")
    private boolean searchEnabled;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 主搜索方法：只使用秘塔搜索API
     */
    public List<Map<String, String>> searchMetaso(String query) {
        log.info("开始搜索，查询词: {}, 搜索启用: {}", query, searchEnabled);
        
        if (!searchEnabled) {
            log.info("搜索功能已禁用，返回空结果");
            return new ArrayList<>();
        }
        
        // 只使用秘塔搜索API
        List<Map<String, String>> metasoResults = callMetasoAPI(query);
        log.info("秘塔搜索完成，返回 {} 条结果", metasoResults.size());
        return metasoResults;
    }
    
    /**
     * 调用秘塔搜索API
     */
    private List<Map<String, String>> callMetasoAPI(String query) {
        if (metasoApiKey == null || metasoApiKey.isEmpty()) {
            log.warn("秘塔搜索API密钥未配置");
            return new ArrayList<>();
        }
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://metaso.cn/api/search");
            
            // 设置请求头
            httpPost.setHeader("Authorization", "Bearer " + metasoApiKey);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("User-Agent", "SpringAI-ChatBot/1.0");
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            requestBody.put("num_results", 5);
            requestBody.put("search_type", "web");
            requestBody.put("language", "zh-CN");
            
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            httpPost.setEntity(new StringEntity(jsonRequest, StandardCharsets.UTF_8));
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    return parseMetasoResponse(responseString);
                } else {
                    log.error("秘塔搜索API调用失败，状态码: {}", statusCode);
                }
            }
        } catch (Exception e) {
            log.error("秘塔搜索API调用异常: {}", e.getMessage());
        }
        
        return new ArrayList<>();
    }
    
    /**
     * 解析秘塔API响应
     */
    private List<Map<String, String>> parseMetasoResponse(String responseString) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseString, Map.class);
            List<Map<String, Object>> searchResults = (List<Map<String, Object>>) responseMap.get("results");
            
            if (searchResults == null || searchResults.isEmpty()) {
                return new ArrayList<>();
            }
            
            List<Map<String, String>> results = new ArrayList<>();
            for (Map<String, Object> item : searchResults) {
                Map<String, String> result = new HashMap<>();
                result.put("title", (String) item.get("title"));
                result.put("snippet", (String) item.get("snippet"));
                result.put("link", (String) item.get("url"));
                results.add(result);
            }
            
            return results;
        } catch (Exception e) {
            log.error("解析秘塔API响应失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    
    /**
     * 格式化搜索结果为文本
     */
    public String formatSearchResults(List<Map<String, String>> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return "";
        }
        
        StringBuilder formatted = new StringBuilder();
        formatted.append("搜索结果：\n");
        
        for (int i = 0; i < searchResults.size(); i++) {
            Map<String, String> result = searchResults.get(i);
            formatted.append(String.format("%d. %s\n", i + 1, result.get("title")));
            formatted.append(String.format("   %s\n", result.get("snippet")));
            formatted.append(String.format("   链接: %s\n\n", result.get("link")));
        }
        
        return formatted.toString();
    }
    
    /**
     * 判断是否需要搜索
     */
    public boolean shouldSearch(String message) {
        // 只要消息不为null且不为空（去除空白字符后），就可以搜索
        return message != null && !message.trim().isEmpty();
    }
}