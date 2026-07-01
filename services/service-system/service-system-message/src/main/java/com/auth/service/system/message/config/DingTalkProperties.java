package com.auth.service.system.message.config;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.model.dingtalk.DingTalkMessageType;
import com.auth.service.system.common.exception.SystemBusinessException;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;

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
	 * 断言发送钉钉消息所需配置齐全 发送时进行校验，防止发送未配置的短信
	 */
	public void assertSendRequiredConfigured() {
		assertNotBlank(clientId, "钉钉 ClientId");
		assertNotBlank(clientSecret, "钉钉 ClientSecret");
		if (agentId == null) {
			throw new SystemBusinessException(PARAM_REQUIRED, "钉钉 AgentId");
		}
	}

	/**
	 * 断言字段不能为空
	 * @param value 字段值
	 * @param fieldName 字段名称
	 */
	private void assertNotBlank(String value, String fieldName) {
		if (CharSequenceUtil.isBlank(value)) {
			throw new SystemBusinessException(PARAM_REQUIRED, fieldName);
		}
	}

}
