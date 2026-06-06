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

	@Nullable
	private String appId;

	@NotNull
	private Long agentId;

	@NotBlank
	private String clientId;

	@NotBlank
	private String clientSecret;

	/** 未指定消息类型时的默认值 */
	private DingTalkMessageType defaultMessageType = DingTalkMessageType.TEXT;

}
