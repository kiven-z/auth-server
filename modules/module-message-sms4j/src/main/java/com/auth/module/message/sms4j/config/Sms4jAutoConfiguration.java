package com.auth.module.message.sms4j.config;

import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.sms.SmsSender;
import com.auth.module.message.sms4j.Sms4jSmsSender;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * sms4j 短信发送 Bean（厂商 blend 由 sms4j Starter 根据 {@code sms.blends} 自动注册）
 *
 * @author Bunny
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.dromara.sms4j.core.factory.SmsFactory")
@ConditionalOnProperty(prefix = "auth.message.sms", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SmsProperties.class)
public class Sms4jAutoConfiguration {

	@ConditionalOnMissingBean(SmsSender.class)
	@Bean
	SmsSender sms4jSmsSender(SmsProperties smsProperties) {
		return new Sms4jSmsSender(smsProperties);
	}

}
