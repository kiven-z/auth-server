package com.auth.service.system.message.channel.dingtalk.model;

import com.auth.module.message.dingtalk.DingTalkMessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 钉钉工作通知手动发送表单
 *
 * @author Bunny
 */
@Getter
@Setter
public class DingTalkWorkNoticeSendForm {

	@Schema(title = "接收人钉钉 userid 列表")
	@NotEmpty
	private List<String> userIds;

	@Schema(title = "消息正文")
	@NotBlank
	private String content;

	@Schema(title = "Markdown 标题（messageType=MARKDOWN 时有效）")
	private String title;

	@Schema(title = "消息类型，默认 TEXT")
	private DingTalkMessageType messageType;

}
