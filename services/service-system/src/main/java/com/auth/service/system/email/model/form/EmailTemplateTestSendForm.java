package com.auth.service.system.email.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 邮件模板测试发送参数
 *
 * @author Bunny
 */
@Schema(name = "EmailTemplateTestSendForm", title = "邮件模板测试发送")
@Data
public class EmailTemplateTestSendForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "id", title = "模板ID")
	@NotNull(message = "模板ID不能为空")
	private Long id;

	@Schema(name = "email", title = "接收邮箱")
	@Email(message = "邮箱格式不正确")
	@NotBlank(message = "邮箱不能为空")
	private String email;

}
