package com.example.springai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SpringaiApplicationTests {

	@Test
	void contextLoads() {
		// 测试Spring上下文是否能正常加载
		assertDoesNotThrow(() -> {
			// 上下文加载成功
		});
	}

	@Test
	void mainMethodTest() {
		// 测试main方法不会抛出异常
		assertDoesNotThrow(() -> {
			// 注意：这里不能直接调用main方法，因为会启动整个应用
			// 我们通过反射来测试main方法的存在性
			try {
				SpringaiApplication.class.getDeclaredMethod("main", String[].class);
			} catch (NoSuchMethodException e) {
				fail("Main method should exist");
			}
		});
	}

	@Test
	void applicationClassTest() {
		// 测试应用类的基本属性
		assertNotNull(SpringaiApplication.class);
		assertTrue(SpringaiApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class));
	}

}
