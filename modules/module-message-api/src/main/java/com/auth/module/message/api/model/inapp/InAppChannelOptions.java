package com.auth.module.message.api.model.inapp;

import com.auth.module.message.api.command.ChannelOptions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 站内信渠道扩展参数
 *
 * @author Bunny
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InAppChannelOptions implements ChannelOptions {

	/**
	 * Jackson 多态类型标识
	 */
	public static final String TYPE = "in_app";

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 标题（customBody 时可选覆盖；模板模式一般由模板渲染）
	 */
	private String title;

	/**
	 * 正文类型覆盖：TEXT / MARKDOWN
	 */
	private String contentType;

	/**
	 * 业务小类 ID
	 */
	private Long categoryId;

	/**
	 * 跳转链接
	 */
	private String linkUrl;

}
