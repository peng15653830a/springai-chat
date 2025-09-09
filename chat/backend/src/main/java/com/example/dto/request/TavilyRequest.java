package com.example.dto.request;

import lombok.Data;
import java.util.List;

/**
 * Tavily搜索请求DTO
 *
 * @author xupeng
 */
@Data
public class TavilyRequest {
    private String apiKey;
    private String query;
    private String searchDepth;
    private Boolean includeAnswer;
    private Boolean includeRawContent;
    private int maxResults = 5;
    private List<String> includeDomains;
    private List<String> excludeDomains;

    /**
     * 创建基础搜索请求
     */
    public static TavilyRequest createBasic(String apiKey, String query) {
        TavilyRequest request = new TavilyRequest();
        request.setApiKey(apiKey);
        request.setQuery(query);
        request.setSearchDepth("basic");
        request.setIncludeAnswer(true);
        request.setIncludeRawContent(false);
        request.setMaxResults(5);
        return request;
    }
}
