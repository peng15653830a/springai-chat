package com.example.springai;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SpringaiApplicationTests {

  @Test
  void contextLoads() {
    // 测试Spring上下文是否能正常加载
    assertDoesNotThrow(
        () -> {
          // 上下文加载成功
        });
  }

  @Test
  void applicationClassTest() {
    // 测试应用类包含SpringBootApplication注解
    assertTrue(
        SpringaiApplication.class.isAnnotationPresent(
            org.springframework.boot.autoconfigure.SpringBootApplication.class));
  }
}
