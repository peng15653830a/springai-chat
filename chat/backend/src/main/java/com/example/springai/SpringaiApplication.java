package com.example.springai;

import com.example.config.DeepSeekConfig;
import com.example.config.GreatWallConfig;
import com.example.config.OpenAiCompatibleConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
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
@Import({DeepSeekConfig.class, GreatWallConfig.class, OpenAiCompatibleConfig.class})
public class SpringaiApplication {

  public static void main(String[] args) {
    // 使用Spring Boot原生环境变量支持，不再需要dotenv
    SpringApplication.run(SpringaiApplication.class, args);
  }
}
