package com.example.malllite.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {

    private Long id;

    private Long productId;

    private String productName;

    private BigDecimal productPrice;

    private Integer quantity;

    private BigDecimal totalPrice;
}