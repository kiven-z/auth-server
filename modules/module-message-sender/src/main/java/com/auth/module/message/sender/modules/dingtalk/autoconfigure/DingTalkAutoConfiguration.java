package com.auth.module.message.sender.modules.dingtalk.autoconfigure;

import com.auth.module.message.sender.config.properties.DingTalkProperties;
import com.auth.module.message.sender.modules.dingtalk.client.DingTalkAccessTokenClient;
import com.auth.module.message.sender.modules.dingtalk.client.DingTalkWorkNoticeClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * DingTalk work-notice client auto-configuration.
 *
 * @author Bunny
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "auth.message.ding-talk", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(DingTalkProperties.class)
public class DingTalkAutoConfiguration {

	/**
	 * @param dingTalkProperties DingTalk config
	 * @param restTemplate HTTP client
	 * @return access-token client
	 */
	@ConditionalOnMissingBean
	@Bean
	DingTalkAccessTokenClient dingTalkAccessTokenClient(DingTalkProperties dingTalkProperties,
			RestTemplate restTemplate) {
		return new DingTalkAccessTokenClient(dingTalkProperties, restTemplate);
	}

	/**
	 * @param dingTalkProperties DingTalk config
	 * @param accessTokenClient access-token client
	 * @param restTemplate HTTP client
	 * @return work-notice client
	 */
	@ConditionalOnMissingBean
	@Bean
	DingTalkWorkNoticeClient dingTalkWorkNoticeClient(DingTalkProperties dingTalkProperties,
			DingTalkAccessTokenClient accessTokenClient, RestTemplate restTemplate) {
		return new DingTalkWorkNoticeClient(dingTalkProperties, accessTokenClient, restTemplate);
	}

}
