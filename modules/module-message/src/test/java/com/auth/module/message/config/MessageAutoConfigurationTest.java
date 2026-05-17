package com.auth.module.message.config;

import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.auth.module.message.config.properties.SmsProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MessageAutoConfiguration} 条件装配测试
 */
@DisplayName("MessageAutoConfiguration 条件装配")
class MessageAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(MessageAutoConfiguration.class));

	@Test
	@DisplayName("默认未启用：不注册 AsyncClient")
	void whenDisabled_doesNotRegisterClient() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(SmsProperties.class);
			assertThat(context).doesNotHaveBean(AsyncClient.class);
		});
	}

	@Test
	@DisplayName("enabled=true 且 provider=aliyun：注册 aliyunSmsAsyncClient")
	void whenAliyunEnabled_registersAsyncClient() {
		contextRunner
			.withPropertyValues("auth.message.sms.enabled=true", "auth.message.sms.provider=aliyun",
					"auth.message.sms.aliyun.access-key-id=test-id",
					"auth.message.sms.aliyun.access-key-secret=test-secret")
			.run(context -> {
				assertThat(context).hasBean("aliyunSmsAsyncClient");
				assertThat(context.getBean("aliyunSmsAsyncClient")).isInstanceOf(AsyncClient.class);
			});
	}

}
