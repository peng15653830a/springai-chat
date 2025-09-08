package com.example.ordersystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * 产品信息DTO，用于Spring AI Function Calling
 */
public record ProductInfo(
    @JsonProperty("name") String name,
    @JsonProperty("price") BigDecimal price,
    @JsonProperty("stock") Integer stock,
    @JsonProperty("status") String status
) {}