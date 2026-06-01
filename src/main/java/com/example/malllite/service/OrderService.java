package com.example.malllite.service;

import com.example.malllite.common.PageResponse;
import com.example.malllite.request.OrderCreateRequest;
import com.example.malllite.request.OrderQueryRequest;
import com.example.malllite.vo.OrderVO;

public interface OrderService {

    OrderVO createOrder(OrderCreateRequest request);

    PageResponse<OrderVO> pageMyOrders(OrderQueryRequest request);

    OrderVO getOrderById(Long id);

    Boolean cancelOrder(Long id);

    Boolean payOrder(Long id);

    PageResponse<OrderVO> pageAllOrders(OrderQueryRequest request);

    Boolean shipOrder(Long id);

    Boolean completeOrder(Long id);

    Integer cancelTimeoutOrders(Integer timeoutMinutes, Integer batchSize);
}