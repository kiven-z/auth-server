package com.auth.module.message.sms4j.config;

import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.sms.SmsSender;
import com.auth.module.message.sms4j.Sms4jSmsSender;
import com.auth.module.message.sms4j.support.AuthSms4jReadConfig;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

/**
 * sms4j 短信自动配置（{@code auth.message.sms.enabled=true}）
 *
 * @author Bunny
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(name = "org.dromara.sms4j.core.factory.SmsFactory")
@ConditionalOnProperty(prefix = "auth.message.sms", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SmsProperties.class)
public class Sms4jAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	AuthSms4jReadConfig authSms4jReadConfig(SmsProperties smsProperties) {
		return new AuthSms4jReadConfig(smsProperties);
	}

	@ConditionalOnMissingBean(SmsSender.class)
	@Bean
	SmsSender sms4jSmsSender(SmsProperties smsProperties) {
		return new Sms4jSmsSender(smsProperties);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onReady(AuthSms4jReadConfig readConfig) {
		readConfig.forEachEnabledBlend((configId, blend) -> {
			SmsFactory.createSmsBlend(readConfig, configId);
			log.info("SMS4J blend registered, configId={}, supplier={}", configId, blend.getSupplier());
		});
		log.info("SMS4J blends registered from auth.message.sms");
	}

}
