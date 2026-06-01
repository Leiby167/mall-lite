package com.example.malllite;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserAuthApiTest extends ApiTestBase {

    @Test
    @DisplayName("用户注册接口测试")
    void registerShouldSuccess() throws Exception {
        String username = "register_" + System.currentTimeMillis();
        String phone = "139" + String.valueOf(System.nanoTime()).substring(0, 8);

        String body = """
                {
                  "username": "%s",
                  "password": "123456",
                  "email": "%s@qq.com",
                  "phone": "%s"
                }
                """.formatted(username, username, phone);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("用户登录并获取当前用户测试")
    void loginAndGetCurrentUserShouldSuccess() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(get("/user/current")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}