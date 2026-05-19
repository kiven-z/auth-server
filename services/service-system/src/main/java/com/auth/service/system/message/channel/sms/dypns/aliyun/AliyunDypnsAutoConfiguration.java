package com.auth.service.system.message.channel.sms.dypns.aliyun;

import com.auth.service.system.message.config.properties.SmsProperties;
import com.auth.service.system.message.channel.sms.dypns.DypnsVerifyCodeSender;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 阿里云 Dypns 短信发送自动配置
 *
 * @author Bunny
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "auth.message.sms", name = "enabled", havingValue = "true")
public class AliyunDypnsAutoConfiguration {

	/**
	 * Dypns 验证码短信发送实现
	 * @param smsProperties 短信配置
	 * @return {@link DypnsVerifyCodeSender}
	 */
	@ConditionalOnMissingBean(DypnsVerifyCodeSender.class)
	@Bean(destroyMethod = "close")
	DypnsVerifyCodeSender aliyunDypnsVerifyCodeSender(SmsProperties smsProperties) {
		return new AliyunDypnsVerifyCodeSender(smsProperties);
	}

}
