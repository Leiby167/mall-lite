package com.example.malllite;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.malllite.entity.OrderEventLog;
import com.example.malllite.mapper.OrderEventLogMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RabbitMqOrderEventTest extends ApiTestBase {

    @Autowired
    private OrderEventLogMapper orderEventLogMapper;

    @Test
    @DisplayName("创建订单后 RabbitMQ 异步写入订单事件日志测试")
    void orderCreatedMessageShouldBeConsumed() throws Exception {
        String token = registerAndLogin();

        Long orderId = createOrder(token);

        Thread.sleep(1500);

        LambdaQueryWrapper<OrderEventLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEventLog::getOrderId, orderId)
                .eq(OrderEventLog::getEventType, "ORDER_CREATED");

        List<OrderEventLog> logs = orderEventLogMapper.selectList(queryWrapper);

        assertFalse(logs.isEmpty());
    }

    private Long createOrder(String token) throws Exception {
        String body = """
                {
                  "items": [
                    {
                      "productId": 1,
                      "quantity": 1
                    }
                  ]
                }
                """;

        MvcResult result = mockMvc.perform(post("/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearer(token))
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(response);

        return root.path("data").path("id").asLong();
    }
}