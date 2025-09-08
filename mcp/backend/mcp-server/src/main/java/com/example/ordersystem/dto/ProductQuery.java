package com.example.ordersystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 产品查询请求DTO，用于Spring AI Function Calling
 */
public record ProductQuery(
    @JsonProperty("keyword") String keyword
) {
    public ProductQuery {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("查询关键词不能为空");
        }
    }
}