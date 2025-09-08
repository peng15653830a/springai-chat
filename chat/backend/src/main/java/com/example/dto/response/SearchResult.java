package com.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索结果DTO
 *
 * @author xupeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private String title;
    private String snippet;
    private String url;
    private Double score;
    private String content;

    /**
     * 创建搜索结果
     */
    public static SearchResult create(String title, String url, String snippet, String content) {
        SearchResult result = new SearchResult();
        result.setTitle(title);
        result.setUrl(url);
        result.setSnippet(snippet);
        result.setContent(content);
        return result;
    }
}