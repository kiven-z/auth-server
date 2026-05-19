package com.auth.service.system.message.infrastructure.modules.dingtalk.autoconfigure;

import com.auth.service.system.message.infrastructure.config.properties.DingTalkProperties;
import com.auth.service.system.message.infrastructure.modules.dingtalk.client.DingTalkAccessTokenClient;
import com.auth.service.system.message.infrastructure.modules.dingtalk.client.DingTalkWorkNoticeClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * 钉钉工作通知客户端自动配置
 *
 * @author Bunny
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "auth.message.ding-talk", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(DingTalkProperties.class)
public class DingTalkAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	DingTalkAccessTokenClient dingTalkAccessTokenClient(DingTalkProperties dingTalkProperties,
			RestTemplate restTemplate) {
		return new DingTalkAccessTokenClient(dingTalkProperties, restTemplate);
	}

	@ConditionalOnMissingBean
	@Bean
	DingTalkWorkNoticeClient dingTalkWorkNoticeClient(DingTalkProperties dingTalkProperties,
			DingTalkAccessTokenClient accessTokenClient, RestTemplate restTemplate) {
		return new DingTalkWorkNoticeClient(dingTalkProperties, accessTokenClient, restTemplate);
	}

}
