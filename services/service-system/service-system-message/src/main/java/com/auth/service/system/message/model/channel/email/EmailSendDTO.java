package com.auth.service.system.message.model.channel.email;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.auth.module.message.api.model.email.AttachmentDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

	@NotBlank
	private String templateCode;

	@NotEmpty
	private List<String> to;

	private List<String> cc;

	private List<String> bcc;

	private String replyTo;

	private transient Map<String, Object> variables;

	private Boolean hasHtml;

	private List<AttachmentDTO> attachments;

}
