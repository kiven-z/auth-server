package com.auth.module.message.config.sms;

import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.auth.module.message.config.MessageAutoConfiguration;
import com.auth.module.message.config.properties.SmsProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SmsChannelAutoConfiguration} 条件装配测试
 */
@DisplayName("SmsChannelAutoConfiguration 条件装配")
class SmsChannelAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(MessageAutoConfiguration.class, SmsChannelAutoConfiguration.class));

	@Test
	@DisplayName("默认未启用：不注册 AsyncClient")
	void whenDisabled_doesNotRegisterClient() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(SmsProperties.class);
			assertThat(context).doesNotHaveBean(AsyncClient.class);
		});
	}

	@Test
	@DisplayName("enabled=true 且密钥齐全：注册 aliyunSmsAsyncClient")
	void whenEnabled_registersAsyncClient() {
		contextRunner
			.withPropertyValues("auth.message.sms.enabled=true", "auth.message.sms.aliyun.access-key-id=test-id",
					"auth.message.sms.aliyun.access-key-secret=test-secret")
			.run(context -> {
				assertThat(context).hasBean("aliyunSmsAsyncClient");
				assertThat(context.getBean("aliyunSmsAsyncClient")).isInstanceOf(AsyncClient.class);
			});
	}

}
