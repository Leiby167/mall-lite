package com.example.malllite.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_event_log")
public class OrderEventLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String eventType;

    private String eventContent;

    private LocalDateTime createTime;
}