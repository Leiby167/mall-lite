package com.example.malllite;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductApiTest extends ApiTestBase {

    @Test
    @DisplayName("商品分页查询接口测试")
    void pageProductShouldSuccess() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(get("/product/page")
                        .param("current", "1")
                        .param("size", "10")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("商品详情查询接口测试")
    void getProductDetailShouldSuccess() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(get("/product/1")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}