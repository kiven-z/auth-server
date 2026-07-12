package com.auth.service.system.message.config;

import com.auth.module.message.api.channel.MessageChannel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * 消息渠道启用开关配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.message.channel")
@RefreshScope
@Configuration
public class MessageChannelProperties {

	/**
	 * 邮件渠道开关
	 */
	private boolean emailEnabled = true;

	/**
	 * 短信渠道开关
	 */
	private boolean smsEnabled = true;

	/**
	 * 钉钉渠道开关
	 */
	private boolean dingTalkEnabled = true;

	/**
	 * 判断渠道是否启用
	 * @param channel 渠道
	 * @return true-启用，false-禁用
	 */
	public boolean isEnabled(MessageChannel channel) {
		if (channel == null) {
			return false;
		}
		return switch (channel) {
			case EMAIL -> emailEnabled;
			case SMS -> smsEnabled;
			case DING_TALK -> dingTalkEnabled;
			default -> true;
		};
	}

}
