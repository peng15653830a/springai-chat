package com.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Tavily搜索响应DTO
 *
 * @author xupeng
 */
@Data
public class TavilyResponse {
    private String answer;
    private String query;
    private double responseTime;
    private List<TavilySearchResult> results;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TavilySearchResult {
        private String title;
        private String url;
        private String content;
        private String rawContent;
        private String publishedDate;
        private Double score;

        /**
         * 转换为SearchResult
         */
        public SearchResult toSearchResult() {
            return SearchResult.create(title, url, content, content);
        }
    }
}
