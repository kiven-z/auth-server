package com.auth.service.system.message.config;

import com.auth.module.message.api.model.dingtalk.DingTalkMessageType;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.auth.service.system.message.utils.MessageConfigAssertions.assertNotBlank;
import static com.auth.service.system.message.utils.MessageConfigAssertions.assertNotNull;

/**
 * 钉钉工作通知配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.message.ding-talk")
@Configuration
public class DingTalkProperties {

	@Nullable
	private String appId;

	@Nullable
	private Long agentId;

	@Nullable
	private String clientId;

	@Nullable
	private String clientSecret;

	/**
	 * 未指定消息类型时的默认值
	 */
	private DingTalkMessageType defaultMessageType = DingTalkMessageType.TEXT;

	/**
	 * 断言发送钉钉消息所需配置齐全
	 */
	public void assertSendRequiredConfigured() {
		assertNotBlank(clientId, "钉钉 ClientId");
		assertNotBlank(clientSecret, "钉钉 ClientSecret");
		assertNotNull(agentId, "钉钉 AgentId");
	}

}
