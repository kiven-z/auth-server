package com.auth.service.system.message.infrastructure.modules.email;

import com.auth.module.message.api.model.email.AttachmentDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 基于模板的邮件发送请求（跨服务契约）
 *
 * @author Bunny
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 模板编码，对应 sys_message_template.scene_code（channel=EMAIL）
	 */
	@NotBlank
	private String templateCode;

	/**
	 * 收件人列表
	 */
	@NotEmpty
	private List<String> to;

	/**
	 * 抄送
	 */
	private List<String> cc;

	/**
	 * 密送
	 */
	private List<String> bcc;

	/**
	 * 回复地址
	 */
	private String replyTo;

	/**
	 * FreeMarker 模板变量
	 */
	private transient Map<String, Object> variables;

	/**
	 * 正文是否为 HTML
	 */
	private Boolean hasHtml;

	/**
	 * 附件列表
	 */
	private List<AttachmentDTO> attachments;

}
