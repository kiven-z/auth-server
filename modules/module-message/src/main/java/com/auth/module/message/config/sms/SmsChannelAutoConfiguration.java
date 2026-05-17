package com.auth.module.message.config.sms;

import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.config.sms.aliyun.AliyunSmsAsyncClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 短信渠道基础设施自动配置（阿里云 AsyncClient）
 *
 * @author Bunny
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(AsyncClient.class)
@ConditionalOnProperty(prefix = "auth.message.sms", name = "enabled", havingValue = "true")
public class SmsChannelAutoConfiguration {

	/**
	 * 阿里云 Dypnsapi 异步客户端
	 * @param smsProperties 短信配置
	 * @return 可关闭的 AsyncClient
	 */
	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean(name = "aliyunSmsAsyncClient")
	AsyncClient aliyunSmsAsyncClient(SmsProperties smsProperties) {
		AsyncClient client = AliyunSmsAsyncClientFactory.create(smsProperties);
		log.info("Aliyun SMS AsyncClient initialized, region={}, endpoint={}", smsProperties.getAliyun().getRegion(),
				smsProperties.getAliyun().getEndpoint());
		return client;
	}

}
