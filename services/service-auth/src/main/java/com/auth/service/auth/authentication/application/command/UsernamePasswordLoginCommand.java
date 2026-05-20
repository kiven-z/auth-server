package com.auth.service.auth.authentication.application.command;

import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import lombok.Getter;

/**
 * 用户名密码登录命令。
 *
 * @author Bunny
 */
@Getter
public class UsernamePasswordLoginCommand extends AbstractLoginCommand {

	private final String username;

	private final String password;

	public UsernamePasswordLoginCommand(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	@Override
	public String principalForAudit() {
		return username;
	}

}
