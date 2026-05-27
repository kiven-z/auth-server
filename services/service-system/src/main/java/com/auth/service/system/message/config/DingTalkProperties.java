package com.auth.service.system.message.config;

import com.auth.module.message.api.model.dingtalk.DingTalkMessageType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * 钉钉工作通知配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.message.ding-talk")
@Configuration
@Validated
public class DingTalkProperties {

	/**
	 * 应用 App ID（预留，当前 Open API 未使用）
	 */
	@Nullable
	private String appId;

	/**
	 * 工作通知 AgentId
	 */
	@NotNull
	private Long agentId;

	/**
	 * Client ID（原 AppKey）
	 */
	@NotBlank
	private String clientId;

	/**
	 * Client Secret（原 AppSecret）
	 */
	@NotBlank
	private String clientSecret;

	/**
	 * 未指定消息类型时的默认值
	 */
	private DingTalkMessageType defaultMessageType = DingTalkMessageType.TEXT;

}
