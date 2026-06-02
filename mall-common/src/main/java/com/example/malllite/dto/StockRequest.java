package com.example.malllite.dto;

import lombok.Data;

@Data
public class StockRequest {

    private Long productId;

    private Integer quantity;
}