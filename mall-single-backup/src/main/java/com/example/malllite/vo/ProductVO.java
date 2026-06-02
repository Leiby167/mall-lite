package com.example.malllite.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {

    private Long id;

    private String productName;

    private BigDecimal price;

    private Integer stock;

    private Integer status;

    private String statusText;

    private LocalDateTime createTime;
}