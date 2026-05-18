package com.auth.module.message.config;

import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.sms.aliyun.AliyunSmsProperties;
import com.auth.module.message.sms.aliyun.SmsVerifyCodeSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 消息模块自动配置条件装配测试
 */
@DisplayName("MessageAutoConfiguration 条件装配")
class MessageAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(MessageAutoConfiguration.class));

	@Test
	@DisplayName("默认未启用：注册 SmsProperties 但不注册阿里云客户端")
	void whenDisabled_doesNotRegisterClient() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(SmsProperties.class);
			assertThat(context).doesNotHaveBean(AsyncClient.class);
			assertThat(context).doesNotHaveBean(AliyunSmsProperties.class);
		});
	}

	@Test
	@DisplayName("enabled=true 且 provider=aliyun：注册 AliyunSmsProperties 与 AsyncClient")
	void whenAliyunEnabled_registersAliyunBeans() {
		contextRunner
			.withPropertyValues("auth.message.sms.enabled=true", "auth.message.sms.verify-enabled=true",
					"auth.message.sms.provider=aliyun", "auth.message.sms.aliyun.access-key-id=test-id",
					"auth.message.sms.aliyun.access-key-secret=test-secret")
			.run(context -> {
				assertThat(context).hasSingleBean(AliyunSmsProperties.class);
				assertThat(context).hasBean("aliyunSmsAsyncClient");
				assertThat(context.getBean("aliyunSmsAsyncClient")).isInstanceOf(AsyncClient.class);
				assertThat(context).hasSingleBean(SmsVerifyCodeSender.class);
			});
	}

	@Test
	@DisplayName("enabled=true 但 provider=tencent：不注册阿里云 Bean")
	void whenTencentProvider_doesNotRegisterAliyunBeans() {
		contextRunner
			.withPropertyValues("auth.message.sms.enabled=true", "auth.message.sms.verify-enabled=true",
					"auth.message.sms.provider=tencent")
			.run(context -> {
				assertThat(context).hasSingleBean(SmsProperties.class);
				assertThat(context).doesNotHaveBean(AliyunSmsProperties.class);
				assertThat(context).doesNotHaveBean(AsyncClient.class);
				assertThat(context).doesNotHaveBean(SmsVerifyCodeSender.class);
			});
	}

	@Test
	@DisplayName("enabled=true 但 verify-enabled=false：不注册阿里云客户端")
	void whenVerifyDisabled_doesNotRegisterAliyunBeans() {
		contextRunner
			.withPropertyValues("auth.message.sms.enabled=true", "auth.message.sms.verify-enabled=false",
					"auth.message.sms.provider=aliyun")
			.run(context -> {
				assertThat(context).doesNotHaveBean(AsyncClient.class);
				assertThat(context).doesNotHaveBean(SmsVerifyCodeSender.class);
			});
	}

}
