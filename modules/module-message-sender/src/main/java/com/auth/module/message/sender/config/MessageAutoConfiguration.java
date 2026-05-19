package com.auth.module.message.sender.config;

import com.auth.module.message.sender.modules.dingtalk.autoconfigure.DingTalkAutoConfiguration;
import com.auth.module.message.sender.modules.email.autoconfigure.EmailAutoConfiguration;
import com.auth.module.message.sender.modules.sms.dypns.aliyun.AliyunDypnsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

/**
 * 消息模块自动配置入口（渠道基础设施）
 *
 * @author Bunny
 */
@AutoConfiguration
@Import({ AliyunDypnsAutoConfiguration.class, DingTalkAutoConfiguration.class, EmailAutoConfiguration.class })
public class MessageAutoConfiguration {

	/**
	 * HTTP client for message channel adapters (DingTalk, email URL attachments).
	 * @return RestTemplate bean
	 */
	@ConditionalOnMissingBean
	@Bean
	RestTemplate messageRestTemplate() {
		return new RestTemplate();
	}

}
