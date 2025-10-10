package com.example.novel.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class ModelListResponse {
    private List<ModelInfo> models;

    @Data
    public static class ModelInfo {
        private String name;
        private String displayName;
        private Boolean available;
        private Long size;
        private String modifiedAt;
    }
}