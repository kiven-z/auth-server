package com.auth.module.message.config.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

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
		assertThat(properties.getBlends()).isInstanceOf(LinkedHashMap.class);
		assertThat(properties.getBlends()).isEmpty();
	}

	@Test
	@DisplayName("可设置 blends 与 defaultConfigId")
	void blendsConfiguration() {
		SmsProperties properties = new SmsProperties();
		properties.setEnabled(true);
		properties.setDefaultConfigId("aliyun-main");

		SmsBlendProperties blend = new SmsBlendProperties();
		blend.setSupplier("alibaba");
		blend.setAccessKeyId("id");
		blend.setAccessKeySecret("secret");
		blend.setSignature("sign");
		properties.getBlends().put("aliyun-main", blend);

		assertThat(properties.isEnabled()).isTrue();
		assertThat(properties.getDefaultConfigId()).isEqualTo("aliyun-main");
		assertThat(properties.getBlends()).hasSize(1);
	}

}
