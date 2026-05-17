package com.auth.module.message.sms.aliyun;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信渠道扩展参数（仅 {@link com.auth.module.message.channel.MessageChannel#SMS} 使用）
 *
 * @author Bunny
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsChannelOptions implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 国家/地区码，默认 86
	 */
	@Builder.Default
	private String countryCode = "86";

	/**
	 * 阿里云 Dypnsapi 方案名称（可选，未设置时使用配置默认值）
	 */
	private String schemeName;

}
