package com.auth.module.message.config;

import com.auth.module.message.config.properties.SmsProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 消息模块自动配置测试
 */
@DisplayName("MessageAutoConfiguration 条件装配")
class MessageAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(MessageAutoConfiguration.class));

	@Test
	@DisplayName("始终注册 SmsProperties")
	void registersSmsProperties() {
		contextRunner.run(context -> assertThat(context).hasSingleBean(SmsProperties.class));
	}

}
