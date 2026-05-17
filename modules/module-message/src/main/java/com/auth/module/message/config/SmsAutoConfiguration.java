package com.auth.module.message.config;

import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.sms.AliyunSmsClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 短信渠道自动配置（Properties 绑定 + 按 {@code provider} 装配客户端）
 *
 * @author Bunny
 */
@Configuration
@EnableConfigurationProperties(SmsProperties.class)
public class SmsAutoConfiguration {

	/**
	 * 阿里云短信客户端（{@code auth.message.sms.provider=aliyun}）
	 */
	@Slf4j
	@ConditionalOnClass(AsyncClient.class)
	@ConditionalOnProperty(prefix = "auth.message.sms", name = "enabled", havingValue = "true")
	@ConditionalOnProperty(prefix = "auth.message.sms", name = "provider", havingValue = "aliyun")
	@Configuration(proxyBeanMethods = false)
	static class AliyunSmsClientConfiguration {

		@Bean(destroyMethod = "close")
		@ConditionalOnMissingBean(name = "aliyunSmsAsyncClient")
		AsyncClient aliyunSmsAsyncClient(SmsProperties smsProperties) {
			AsyncClient client = AliyunSmsClientFactory.create(smsProperties);
			log.info("Aliyun SMS AsyncClient initialized, region={}, endpoint={}", smsProperties.getAliyun().getRegion(),
					smsProperties.getAliyun().getEndpoint());
			return client;
		}

	}

	// 接入腾讯云时在此新增静态内部类：
	// @ConditionalOnProperty(..., name = "provider", havingValue = "tencent")
	// static class TencentSmsClientConfiguration { ... }

}
