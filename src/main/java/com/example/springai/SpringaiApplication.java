package com.example.springai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.example")
@MapperScan("com.example.mapper")
@EnableAsync
public class SpringaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringaiApplication.class, args);
    }

}
