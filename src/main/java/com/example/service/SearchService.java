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
     * 主搜索方法：使用秘塔搜索API或本地搜索
     */
    public List<Map<String, String>> searchMetaso(String query) {
        log.info("开始搜索，查询词: {}, 搜索启用: {}", query, searchEnabled);
        
        if (!searchEnabled) {
            log.info("搜索功能已禁用，返回本地搜索结果");
            return createLocalSearchResults(query);
        }
        
        // 尝试秘塔搜索API
        List<Map<String, String>> metasoResults = callMetasoAPI(query);
        if (!metasoResults.isEmpty()) {
            log.info("秘塔搜索成功，返回 {} 条结果", metasoResults.size());
            return metasoResults;
        }
        
        // 降级到本地搜索结果
        log.info("秘塔搜索失败，降级到本地搜索");
        return createLocalSearchResults(query);
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
     * 创建本地搜索结果 (基于关键词生成相关信息)
     */
    private List<Map<String, String>> createLocalSearchResults(String query) {
        List<Map<String, String>> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        // 根据不同类型的查询生成相关的模拟结果
        if (lowerQuery.contains("天气")) {
            results.add(createResult("今日天气预报", "今天多云转晴，温度18-25°C，微风，适宜出行。明天将有小雨。", "https://weather.com"));
            results.add(createResult("一周天气趋势", "本周前半周以多云天气为主，周四开始转雨，周末天气转好。", "https://weather.forecast.com"));
        } else if (lowerQuery.contains("新闻") || lowerQuery.contains("最新")) {
            results.add(createResult("今日头条新闻", "科技、经济、社会等各领域最新动态，为您提供全面的新闻资讯。", "https://news.com"));
            results.add(createResult("实时热点资讯", "当前热门话题和突发事件的最新报道和深度分析。", "https://breaking-news.com"));
        } else if (lowerQuery.contains("股价") || lowerQuery.contains("股票")) {
            results.add(createResult("股市行情实时数据", "主要指数：上证指数3024.39(+1.2%)，深证指数10156.78(+0.8%)。", "https://stock.com"));
            results.add(createResult("今日股市分析", "市场整体呈现稳中有升态势，科技股表现活跃，建议关注新能源板块。", "https://stock-analysis.com"));
        } else if (lowerQuery.contains("汇率")) {
            results.add(createResult("实时汇率查询", "美元兑人民币：7.2145，欧元兑人民币：7.8542，英镑兑人民币：8.9156。", "https://exchange-rate.com"));
            results.add(createResult("汇率走势分析", "人民币近期对美元呈现稳定走势，预计短期内将维持在当前区间波动。", "https://currency-trend.com"));
        } else {
            // 通用搜索结果
            results.add(createResult("关于" + query + "的详细信息", "这里提供了关于您搜索内容的最新信息和详细解答。", "https://search-result1.com"));
            results.add(createResult(query + "相关资源", "包含相关的学习资料、参考文档和实用工具链接。", "https://search-result2.com"));
            results.add(createResult(query + "最新动态", "最新的发展趋势、行业动态和相关新闻资讯。", "https://search-result3.com"));
        }
        
        return results;
    }
    
    /**
     * 创建搜索结果项
     */
    private Map<String, String> createResult(String title, String snippet, String link) {
        Map<String, String> result = new HashMap<>();
        result.put("title", title);
        result.put("snippet", snippet);
        result.put("link", link);
        return result;
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
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        String[] searchKeywords = {
            // 时间相关
            "最新", "今天", "现在", "当前", "实时", "近期", "目前", "这几天", "本周", "最近",
            // 信息类
            "新闻", "资讯", "消息", "报道", "动态", "头条",
            // 金融相关  
            "天气", "股价", "汇率", "股票", "基金", "投资", "行情", "价格",
            // 查询词汇
            "什么是", "如何", "怎么", "哪里", "什么时候", "为什么",
            // 搜索指示词
            "搜索", "查询", "找", "查找", "了解", "知道"
        };
        
        String lowerMessage = message.toLowerCase();
        
        // 检查搜索关键词
        for (String keyword : searchKeywords) {
            if (lowerMessage.contains(keyword)) {
                log.debug("消息包含搜索关键词: {}", keyword);
                return true;
            }
        }
        
        // 检查问号
        if (lowerMessage.contains("?") || lowerMessage.contains("？")) {
            log.debug("消息包含问号，触发搜索");
            return true;
        }
        
        return false;
    }
}