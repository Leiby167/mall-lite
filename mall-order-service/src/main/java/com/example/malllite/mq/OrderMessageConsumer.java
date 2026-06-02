package com.example.malllite.mq;

import com.example.malllite.config.RabbitMQConfig;
import com.example.malllite.dto.OrderMessageDTO;
import com.example.malllite.entity.OrderEventLog;
import com.example.malllite.mapper.OrderEventLogMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderMessageConsumer {

    private final OrderEventLogMapper orderEventLogMapper;

    public OrderMessageConsumer(OrderEventLogMapper orderEventLogMapper) {
        this.orderEventLogMapper = orderEventLogMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreatedMessage(OrderMessageDTO message) {
        OrderEventLog log = new OrderEventLog();
        log.setOrderId(message.getOrderId());
        log.setEventType(message.getEventType());
        log.setEventContent("订单创建成功，用户ID：" + message.getUserId());
        log.setCreateTime(LocalDateTime.now());

        orderEventLogMapper.insert(log);
    }
}