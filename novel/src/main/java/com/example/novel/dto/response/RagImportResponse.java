package com.example.novel.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class RagImportResponse {
    private Boolean success;
    private String message;
    private Integer totalFiles;
    private Integer processedFiles;
    private Integer totalChunks;
    private List<String> errors;
}