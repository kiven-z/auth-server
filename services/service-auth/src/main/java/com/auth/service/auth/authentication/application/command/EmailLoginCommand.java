package com.auth.service.auth.authentication.application.command;

import com.auth.service.auth.authentication.api.dto.request.EmailLoginRequest;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import lombok.Getter;

/**
 * 邮箱验证码/密码登录命令。
 *
 * @author Bunny
 */
@Getter
public class EmailLoginCommand extends AbstractLoginCommand {

	private final String email;

	private final String code;

	public EmailLoginCommand(String email, String code) {
		this.email = email;
		this.code = code;
	}

	/**
	 * 从 HTTP 请求 DTO 构建命令。
	 * @param request 邮箱登录请求
	 * @return 登录命令
	 */
	public static EmailLoginCommand from(EmailLoginRequest request) {
		EmailLoginCommand command = new EmailLoginCommand(request.getEmail(), request.getCode());
		command.setRememberMe(request.getRememberMe());
		return command;
	}

	@Override
	public AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_EMAIL;
	}

	@Override
	public String principalForAudit() {
		return email;
	}

}
