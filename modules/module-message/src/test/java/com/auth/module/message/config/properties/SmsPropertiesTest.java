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
		assertThat(properties.isVerifyEnabled()).isTrue();
		assertThat(properties.getProvider()).isEqualTo(SmsProperties.Provider.ALIYUN);
		assertThat(properties.getNotification()).isNotNull();
		assertThat(properties.getNotification().isEnabled()).isFalse();
	}

	@Test
	@DisplayName("通知类配置可以设置 blends")
	void notificationWithBlends() {
		SmsProperties properties = new SmsProperties();
		properties.getNotification().setEnabled(true);
		properties.getNotification().setDefaultConfigId("aliyun-main");

		SmsNotificationBlendProperties blend = new SmsNotificationBlendProperties();
		blend.setSupplier("alibaba");
		blend.setAccessKeyId("id");
		blend.setAccessKeySecret("secret");
		blend.setSignature("sign");
		properties.getNotification().getBlends().put("aliyun-main", blend);

		assertThat(properties.getNotification().isEnabled()).isTrue();
		assertThat(properties.getNotification().getDefaultConfigId()).isEqualTo("aliyun-main");
		assertThat(properties.getNotification().getBlends()).hasSize(1);
	}

	@Test
	@DisplayName("notification 为空 blends 时返回空 map")
	void notificationWithoutBlends_returnsEmptyMap() {
		SmsProperties properties = new SmsProperties();
		assertThat(properties.getNotification().getBlends()).isInstanceOf(LinkedHashMap.class);
		assertThat(properties.getNotification().getBlends()).isEmpty();
	}

}
