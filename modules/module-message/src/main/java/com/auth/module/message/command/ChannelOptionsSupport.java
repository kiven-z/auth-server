package com.auth.module.message.command;

import com.auth.module.message.modules.email.EmailChannelOptions;
import lombok.experimental.UtilityClass;

/**
 * {@link ChannelOptions} 类型安全访问
 *
 * @author Bunny
 */
@UtilityClass
public class ChannelOptionsSupport {

	/**
	 * 转为邮件扩展参数
	 * @param options 渠道扩展参数
	 * @return 邮件参数，类型不匹配时返回 {@code null}
	 */
	public static EmailChannelOptions asEmail(ChannelOptions options) {
		if (options instanceof EmailChannelOptions email) {
			return email;
		}
		return null;
	}

}
