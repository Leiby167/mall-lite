package com.example.malllite.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {

    @Valid
    @NotEmpty(message = "订单商品不能为空")
    private List<OrderCreateItemRequest> items;
}