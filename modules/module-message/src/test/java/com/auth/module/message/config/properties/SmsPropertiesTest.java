package com.auth.module.message.config.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SmsProperties} 配置校验
 */
@DisplayName("SmsProperties 配置绑定")
class SmsPropertiesTest {

	@Test
	@DisplayName("默认值验证")
	void defaultValues() {
		SmsProperties properties = new SmsProperties();

		assertThat(properties.isEnabled()).isFalse();
		assertThat(properties.getDefaultConfigId()).isNull();
		assertThat(properties.getDefaultSignName()).isNull();
	}

	@Test
	@DisplayName("可设置 defaultConfigId 与 defaultSignName")
	void businessDefaults() {
		SmsProperties properties = new SmsProperties();
		properties.setEnabled(true);
		properties.setDefaultConfigId("aliyun-main");
		properties.setDefaultSignName("测试签名");

		assertThat(properties.isEnabled()).isTrue();
		assertThat(properties.getDefaultConfigId()).isEqualTo("aliyun-main");
		assertThat(properties.getDefaultSignName()).isEqualTo("测试签名");
	}

}
