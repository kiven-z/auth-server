package com.auth.module.message.config.properties;

import com.auth.module.message.modules.dingtalk.DingTalkMessageType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 钉钉工作通知配置（{@code auth.message.ding-talk.*}）
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.message.ding-talk")
@Validated
public class DingTalkProperties {

	/**
	 * 是否启用钉钉工作通知
	 */
	private boolean enabled = false;

	/**
	 * 应用 App ID
	 */
	private String appId;

	/**
	 * 工作通知 AgentId
	 */
	private Long agentId;

	/**
	 * Client ID（原 AppKey）
	 */
	private String clientId;

	/**
	 * Client Secret（原 AppSecret）
	 */
	private String clientSecret;

	/**
	 * 未指定消息类型时的默认值
	 */
	private DingTalkMessageType defaultMessageType = DingTalkMessageType.TEXT;

}
