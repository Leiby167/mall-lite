package com.example.malllite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class ApiTestBase {

    @Autowired
    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected String registerAndLogin() throws Exception {
        String username = "test_" + System.currentTimeMillis();
        String phone = "138" + String.valueOf(System.nanoTime()).substring(0, 8);

        String registerBody = """
                {
                  "username": "%s",
                  "password": "123456",
                  "email": "%s@qq.com",
                  "phone": "%s"
                }
                """.formatted(username, username, phone);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk());

        String loginBody = """
                {
                  "username": "%s",
                  "password": "123456"
                }
                """.formatted(username);

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(response);

        return root.path("data").path("token").asText();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}