package com.example.novel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example")
@MapperScan("com.example.novel.mapper")
public class NovelApplication {
  public static void main(String[] args) {
    SpringApplication.run(NovelApplication.class, args);
  }
}
