package com.example.malllite.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderMessageDTO implements Serializable {

    private Long orderId;

    private Long userId;

    private BigDecimal totalAmount;

    private String eventType;

    private LocalDateTime createTime;
}