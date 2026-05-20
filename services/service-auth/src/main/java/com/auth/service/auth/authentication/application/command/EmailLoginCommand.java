package com.auth.service.auth.authentication.application.command;

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

	@Override
	public AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_EMAIL;
	}

	@Override
	public String principalForAudit() {
		return email;
	}

}
