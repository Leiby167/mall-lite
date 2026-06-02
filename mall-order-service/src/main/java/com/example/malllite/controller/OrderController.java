package com.example.malllite.controller;

import com.example.malllite.common.BaseResponse;
import com.example.malllite.common.PageResponse;
import com.example.malllite.common.ResultUtils;
import com.example.malllite.request.OrderCreateRequest;
import com.example.malllite.request.OrderQueryRequest;
import com.example.malllite.service.OrderService;
import com.example.malllite.vo.OrderVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.example.malllite.annotation.RequirePermission;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public BaseResponse<OrderVO> createOrder(@RequestBody @Valid OrderCreateRequest request) {
        return ResultUtils.success(orderService.createOrder(request));
    }

    @GetMapping("/my/page")
    public BaseResponse<PageResponse<OrderVO>> pageMyOrders(@Valid OrderQueryRequest request) {
        return ResultUtils.success(orderService.pageMyOrders(request));
    }

    @GetMapping("/{id}")
    public BaseResponse<OrderVO> getOrderById(@PathVariable("id") Long id) {
        return ResultUtils.success(orderService.getOrderById(id));
    }

    @PostMapping("/cancel/{id}")
    public BaseResponse<Boolean> cancelOrder(@PathVariable("id") Long id) {
        return ResultUtils.success(orderService.cancelOrder(id));
    }

    @PostMapping("/pay/{id}")
    public BaseResponse<Boolean> payOrder(@PathVariable("id") Long id) {
        return ResultUtils.success(orderService.payOrder(id));
    }

    @RequirePermission(permissionKey = "order:query")
    @GetMapping("/admin/page")
    public BaseResponse<PageResponse<OrderVO>> pageAllOrders(@Valid OrderQueryRequest request) {
        return ResultUtils.success(orderService.pageAllOrders(request));
    }

    @RequirePermission(permissionKey = "order:ship")
    @PostMapping("/ship/{id}")
    public BaseResponse<Boolean> shipOrder(@PathVariable("id") Long id) {
        return ResultUtils.success(orderService.shipOrder(id));
    }

    @PostMapping("/complete/{id}")
    public BaseResponse<Boolean> completeOrder(@PathVariable("id") Long id) {
        return ResultUtils.success(orderService.completeOrder(id));
    }
}