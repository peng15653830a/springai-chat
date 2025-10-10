package com.example.novel.dto.request;

import lombok.Data;

@Data
public class RagImportRequest {
    private String path;
    private Boolean recursive = true;
    private String filePattern = "*.txt,*.md,*.doc,*.docx";
}