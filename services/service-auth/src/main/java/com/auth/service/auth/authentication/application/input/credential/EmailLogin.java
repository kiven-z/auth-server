package com.auth.service.auth.authentication.application.input.credential;

import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 邮箱验证码登录入站模型。
 *
 * @author Bunny
 */
@Getter
@Setter
public class EmailLogin extends AbstractCredentialLogin {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "email", title = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
	@Email(message = "邮箱格式不正确")
	@NotBlank(message = "邮箱不能为空")
	private String email;

	@Schema(name = "code", title = "验证码或密码", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "验证码或密码不能为空")
	private String code;

	@Override
	public AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_EMAIL;
	}

	@Override
	public String principalForAudit() {
		return email;
	}

}
