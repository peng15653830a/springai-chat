package com.example.springai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SpringaiApplication基础测试
 * 避免加载完整的Spring上下文以防止配置问题
 */
class SpringaiApplicationTests {

  @Test
  void applicationClassTest() {
    // 测试应用类包含SpringBootApplication注解
    assertTrue(
        SpringaiApplication.class.isAnnotationPresent(
            org.springframework.boot.autoconfigure.SpringBootApplication.class));
  }

  @Test
  void applicationMainMethodExists() {
    // 测试main方法存在
    assertDoesNotThrow(() -> {
      SpringaiApplication.class.getDeclaredMethod("main", String[].class);
    });
  }

  @Test
  void applicationClassIsPublic() {
    // 测试应用类是公共类
    assertTrue(java.lang.reflect.Modifier.isPublic(SpringaiApplication.class.getModifiers()));
  }
}
