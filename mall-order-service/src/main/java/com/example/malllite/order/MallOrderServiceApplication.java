package com.example.malllite.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.example.malllite.mapper")
@EnableFeignClients(basePackages = "com.example.malllite.feign")
@SpringBootApplication(scanBasePackages = "com.example.malllite")
public class MallOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallOrderServiceApplication.class, args);
    }
}