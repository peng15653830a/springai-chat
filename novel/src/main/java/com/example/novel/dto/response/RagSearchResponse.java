package com.example.novel.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class RagSearchResponse {
    private Boolean success;
    private String message;
    private List<RagSearchResult> results;

    @Data
    public static class RagSearchResult {
        private String content;
        private String source;
        private Double similarity;
        private String title;
        private String excerpt;
    }
}