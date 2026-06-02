package com.example.malllite;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.malllite.mapper")
public class MallLiteApplication {

    public static void main(String[] args) {

        SpringApplication.run(MallLiteApplication.class, args);
    }

}
