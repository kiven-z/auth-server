package com.auth.module.message.sms.aliyun;

import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 阿里云短信自动配置（{@code auth.message.sms.provider=aliyun} 且验证码启用时生效）
 * <p>
 * 通过 {@code AutoConfiguration.imports} 注册，条件不满足时整组 Bean 均不会加载。
 * </p>
 *
 * @author Bunny
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(AsyncClient.class)
@ConditionalOnProperty(prefix = "auth.message.sms", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "auth.message.sms", name = "verify-enabled", havingValue = "true",
		matchIfMissing = true)
@ConditionalOnProperty(prefix = "auth.message.sms", name = "provider", havingValue = "aliyun")
@EnableConfigurationProperties(AliyunSmsProperties.class)
public class AliyunSmsAutoConfiguration {

	/**
	 * 阿里云 Dypnsapi 异步客户端（由 Spring 容器负责 close）
	 */
	@ConditionalOnMissingBean(name = "aliyunSmsAsyncClient")
	@Bean(destroyMethod = "close")
	AsyncClient aliyunSmsAsyncClient(AliyunSmsProperties aliyunSmsProperties) {
		AsyncClient client = AliyunSmsClientFactory.create(aliyunSmsProperties);
		log.info("Aliyun SMS AsyncClient initialized, region={}, endpoint={}", aliyunSmsProperties.getRegion(),
				aliyunSmsProperties.getEndpoint());
		return client;
	}

	/**
	 * 阿里云短信验证码发送器
	 */
	@ConditionalOnMissingBean(SmsVerifyCodeSender.class)
	@Bean
	SmsVerifyCodeSender smsVerifyCodeSender(AsyncClient aliyunSmsAsyncClient) {
		return new AliyunSmsVerifyCodeSender(aliyunSmsAsyncClient);
	}

}
