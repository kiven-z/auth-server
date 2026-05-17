package com.auth.module.message.config.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link SmsProperties} 校验测试
 */
@DisplayName("SmsProperties 配置校验")
class SmsPropertiesTest {

	@Test
	@DisplayName("未启用：允许 AccessKey 为空")
	void validate_whenDisabled_allowsBlankCredentials() {
		SmsProperties properties = new SmsProperties();
		properties.setEnabled(false);

		assertThatCode(properties::validate).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("已启用但密钥为空：抛出 IllegalStateException")
	void validate_whenEnabledWithoutCredentials_throws() {
		SmsProperties properties = new SmsProperties();
		properties.setEnabled(true);

		assertThatThrownBy(properties::validate).isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("access-key-id");
	}

	@Test
	@DisplayName("已启用且密钥齐全：校验通过")
	void validate_whenEnabledWithCredentials_passes() {
		SmsProperties properties = new SmsProperties();
		properties.setEnabled(true);
		SmsProperties.Aliyun aliyun = properties.getAliyun();
		aliyun.setAccessKeyId("test-id");
		aliyun.setAccessKeySecret("test-secret");

		assertThatCode(properties::validate).doesNotThrowAnyException();
	}

}
