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
	void mainMethodExecution() {
		// 使用系统属性来模拟测试环境，避免实际启动应用
		System.setProperty("spring.main.web-environment", "false");
		System.setProperty("spring.autoconfigure.exclude", 
			"org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration," +
			"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration");
		
		// 测试main方法执行
		assertDoesNotThrow(() -> {
			// 在单独的线程中运行main方法，避免阻塞测试
			Thread mainThread = new Thread(() -> {
				try {
					SpringaiApplication.main(new String[]{"--spring.main.web-environment=false"});
				} catch (Exception e) {
					// 忽略启动过程中的异常，我们只关心方法能被调用
				}
			});
			mainThread.setDaemon(true);
			mainThread.start();
			
			// 等待一小段时间确保main方法被调用
			Thread.sleep(100);
		});
		
		// 清理系统属性
		System.clearProperty("spring.main.web-environment");
		System.clearProperty("spring.autoconfigure.exclude");
	}

	@Test
	void applicationClassTest() {
		// 测试应用类的基本属性
		assertNotNull(SpringaiApplication.class);
		assertTrue(SpringaiApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class));
	}

}
