package com.example.malllite.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {

    @NotNull(message = "商品ID不能为空")
    private Long id;

    private String productName;

    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;

    /**
     * 1 上架，0 下架
     */
    private Integer status;
}