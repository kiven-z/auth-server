package com.auth.module.message.command;

import com.auth.module.message.channel.MessageChannel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 跨渠道模板化消息发送命令（跨服务契约）
 *
 * @author Bunny
 */
@ChannelOptionsMatch
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateMessageCommand implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 发送渠道
	 */
	@NotNull
	private MessageChannel channel;

	/**
	 * 模板编码（各渠道模板表中的 template_code）
	 */
	@NotBlank
	private String templateCode;

	/**
	 * 接收目标列表（语义由渠道解释：邮箱 / 手机号 / 用户 ID 等）
	 */
	@NotEmpty
	private List<String> targets;

	/**
	 * 模板变量（FreeMarker 等渲染引擎使用）
	 */
	private transient Map<String, Object> variables;

	/**
	 * 渠道扩展参数（类型须与 {@link #channel} 一致，见 {@link ChannelOptions}）
	 */
	@Valid
	private ChannelOptions options;

}
