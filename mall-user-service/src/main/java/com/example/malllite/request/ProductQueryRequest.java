package com.example.malllite.request;

import lombok.Data;

@Data
public class ProductQueryRequest {

    private String productName;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}