package com.example.malllite;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiTest extends ApiTestBase {

    @Test
    @DisplayName("创建订单接口测试")
    void createOrderShouldSuccess() throws Exception {
        String token = registerAndLogin();

        Long orderId = createOrder(token);

        mockMvc.perform(get("/order/" + orderId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(orderId));
    }

    @Test
    @DisplayName("订单支付接口测试")
    void payOrderShouldSuccess() throws Exception {
        String token = registerAndLogin();

        Long orderId = createOrder(token);

        mockMvc.perform(post("/order/pay/" + orderId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("取消待支付订单接口测试")
    void cancelOrderShouldSuccess() throws Exception {
        String token = registerAndLogin();

        Long orderId = createOrder(token);

        mockMvc.perform(post("/order/cancel/" + orderId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("我的订单分页查询接口测试")
    void pageMyOrdersShouldSuccess() throws Exception {
        String token = registerAndLogin();

        createOrder(token);

        mockMvc.perform(get("/order/my/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
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