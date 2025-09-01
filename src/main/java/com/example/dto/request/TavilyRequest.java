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
    private String api_key;
    private String query;
    private String search_depth;
    private Boolean include_answer;
    private Boolean include_raw_content;
    private int max_results = 5;
    private List<String> include_domains;
    private List<String> exclude_domains;

    /**
     * 创建基础搜索请求
     */
    public static TavilyRequest createBasic(String apiKey, String query) {
        TavilyRequest request = new TavilyRequest();
        request.setApi_key(apiKey);
        request.setQuery(query);
        request.setSearch_depth("basic");
        request.setInclude_answer(true);
        request.setInclude_raw_content(false);
        request.setMax_results(5);
        return request;
    }
}