package com.auth.module.message.modules.sms.aliyun;

import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.modules.sms.SmsSender;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 阿里云 Dypns 短信发送自动配置
 *
 * @author Bunny
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "auth.message.sms", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SmsProperties.class)
public class AliyunDypnsAutoConfiguration {

	/**
	 * Dypns 验证码客户端
	 * @param smsProperties 短信配置
	 * @return 客户端 Bean
	 */
	@ConditionalOnMissingBean(DypnsVerifyCodeClient.class)
	@Bean(destroyMethod = "close")
	DypnsVerifyCodeClient aliyunDypnsVerifyCodeClient(SmsProperties smsProperties) {
		return new AliyunDypnsVerifyCodeClient(smsProperties);
	}

	/**
	 * 短信发送 SPI 实现
	 * @param dypnsClient Dypns 客户端
	 * @return {@link SmsSender}
	 */
	@ConditionalOnMissingBean(SmsSender.class)
	@Bean(destroyMethod = "close")
	SmsSender aliyunDypnsSmsSender(DypnsVerifyCodeClient dypnsClient) {
		return new AliyunDypnsSmsSender(dypnsClient);
	}

}
