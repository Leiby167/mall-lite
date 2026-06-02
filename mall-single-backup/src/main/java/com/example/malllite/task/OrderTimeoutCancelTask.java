package com.example.malllite.task;

import com.example.malllite.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderTimeoutCancelTask {

    private static final int TIMEOUT_MINUTES = 30;

    private static final int BATCH_SIZE = 100;

    private final OrderService orderService;

    public OrderTimeoutCancelTask(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 每 1 分钟扫描一次超时未支付订单
     */
    @Scheduled(fixedDelay = 60 * 1000)
    public void cancelTimeoutOrders() {
        try {
            Integer count = orderService.cancelTimeoutOrders(TIMEOUT_MINUTES, BATCH_SIZE);
            if (count != null && count > 0) {
                log.info("自动取消超时未支付订单成功，取消数量：{}", count);
            }
        } catch (Exception e) {
            log.error("自动取消超时未支付订单失败", e);
        }
    }
}