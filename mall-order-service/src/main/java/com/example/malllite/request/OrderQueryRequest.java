package com.example.malllite.request;

import lombok.Data;

@Data
public class OrderQueryRequest {

    /**
     * 订单编号，支持模糊查询
     */
    private String orderNo;

    /**
     * 用户ID，管理员查询时可用
     */
    private Long userId;

    /**
     * 订单状态：0待支付，1已支付，2已发货，3已完成，4已取消
     */
    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}