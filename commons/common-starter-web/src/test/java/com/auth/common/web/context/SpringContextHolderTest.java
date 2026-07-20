package com.auth.common.web.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * {@link SpringContextHolder} 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SpringContextHolderTest {

	@Mock
	private ApplicationContext applicationContext;

	@Mock
	private Environment environment;

	@Mock
	private MessageSource messageSource;

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

	@Test
	@DisplayName("注册上下文后应能读取配置属性")
	void getProperty_returnsValue_whenContextRegistered() {
		when(applicationContext.getEnvironment()).thenReturn(environment);
		when(environment.getProperty("auth.test")).thenReturn("enabled");
		SpringContextHolder.registerApplicationContext(applicationContext);

		assertEquals("enabled", SpringContextHolder.getProperty("auth.test"));
	}

	@Test
	@DisplayName("注册上下文后应能解析国际化消息")
	void getMessage_returnsLocalizedMessage_whenContextRegistered() {
		when(applicationContext.getBean(MessageSource.class)).thenReturn(messageSource);
		when(messageSource.getMessage("system.test", new Object[] { "Alice" }, Locale.US)).thenReturn("Hello Alice");
		SpringContextHolder.registerApplicationContext(applicationContext);

		assertEquals("Hello Alice", SpringContextHolder.getMessage("system.test", new Object[] { "Alice" }, Locale.US));
	}

}
