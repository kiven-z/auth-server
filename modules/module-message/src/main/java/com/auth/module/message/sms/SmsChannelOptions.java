package com.auth.module.message.sms;

import com.auth.module.message.command.ChannelOptions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 短信渠道扩展参数（仅 {@link com.auth.module.message.channel.MessageChannel#SMS} 使用）
 *
 * @author Bunny
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsChannelOptions implements ChannelOptions {

	/**
	 * Jackson 多态类型标识（{@link com.auth.module.message.command.ChannelOptions}）
	 */
	public static final String TYPE = "sms";

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 覆盖模板上的 sms4j configId（可选）
	 */
	private String smsConfigId;

}
