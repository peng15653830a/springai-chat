package com.example.springai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring Boot应用启动类
 *
 * @author xupeng
 */
@SpringBootApplication(scanBasePackages = "com.example")
@MapperScan("com.example.mapper")
@EnableAsync
@EnableConfigurationProperties
public class SpringaiApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringaiApplication.class, args);
  }
}
