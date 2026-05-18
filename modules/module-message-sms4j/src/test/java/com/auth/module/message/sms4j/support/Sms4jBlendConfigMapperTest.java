package com.auth.module.message.sms4j.support;

import com.auth.module.message.config.properties.SmsBlendProperties;
import org.dromara.sms4j.aliyun.config.AlibabaConfig;
import org.dromara.sms4j.tencent.config.TencentConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link Sms4jBlendConfigMapper} 单元测试
 */
@DisplayName("Sms4jBlendConfigMapper 配置映射")
class Sms4jBlendConfigMapperTest {

	@Test
	@DisplayName("alibaba 厂商映射为 AlibabaConfig")
	void toSmsConfig_alibaba_mapsFields() {
		SmsBlendProperties blend = new SmsBlendProperties();
		blend.setEnabled(true);
		blend.setSupplier("alibaba");
		blend.setAccessKeyId("ak");
		blend.setAccessKeySecret("sk");
		blend.setSignature("签名");

		AlibabaConfig config = (AlibabaConfig) Sms4jBlendConfigMapper.toBaseConfig("aliyun-main", blend);

		assertThat(config.getConfigId()).isEqualTo("aliyun-main");
		assertThat(config.getAccessKeyId()).isEqualTo("ak");
		assertThat(config.getSignature()).isEqualTo("签名");
	}

	@Test
	@DisplayName("tencent 厂商映射为 TencentConfig")
	void toSmsConfig_tencent_mapsFields() {
		SmsBlendProperties blend = new SmsBlendProperties();
		blend.setEnabled(true);
		blend.setSupplier("tencent");
		blend.setAccessKeyId("ak");
		blend.setAccessKeySecret("sk");
		blend.setSignature("签名");
		blend.setSdkAppId("1400000000");

		TencentConfig config = (TencentConfig) Sms4jBlendConfigMapper.toBaseConfig("tx1", blend);

		assertThat(config.getConfigId()).isEqualTo("tx1");
		assertThat(config.getSdkAppId()).isEqualTo("1400000000");
	}

	@Test
	@DisplayName("不支持的厂商：抛出 IllegalStateException")
	void toSmsConfig_unsupportedSupplier_throws() {
		SmsBlendProperties blend = new SmsBlendProperties();
		blend.setEnabled(true);
		blend.setSupplier("unknown");

		assertThatThrownBy(() -> Sms4jBlendConfigMapper.toBaseConfig("x", blend))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Unsupported");
	}

}
