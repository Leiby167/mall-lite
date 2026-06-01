package com.example.malllite.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_info")
public class OrderInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    /**
     * 0待支付，1已支付，2已发货，3已完成，4已取消
     */
    private Integer status;

    private LocalDateTime payTime;

    private LocalDateTime shipTime;

    private LocalDateTime completeTime;

    private LocalDateTime cancelTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
