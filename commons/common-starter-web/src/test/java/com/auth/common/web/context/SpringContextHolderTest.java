package com.auth.common.web.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link SpringContextHolder} 单元测试
 */
class SpringContextHolderTest {

	@AfterEach
	void tearDown() {
		// 避免静态上下文泄漏到其他测试
		SpringContextHolder.registerApplicationContext(null);
	}

	@Test
	@DisplayName("未注册上下文时访问属性应抛出 IllegalStateException")
	void getProperty_throws_whenContextNotInitialized() {
		assertThrows(IllegalStateException.class, () -> SpringContextHolder.getProperty("auth.test"));
	}

}
