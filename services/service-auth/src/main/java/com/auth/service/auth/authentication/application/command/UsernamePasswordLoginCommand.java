package com.auth.service.auth.authentication.application.command;

import com.auth.service.auth.authentication.api.dto.request.UsernamePasswordLoginRequest;
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

	/**
	 * 从 HTTP 请求 DTO 构建命令。
	 * @param request 用户名密码登录请求
	 * @return 登录命令
	 */
	public static UsernamePasswordLoginCommand from(UsernamePasswordLoginRequest request) {
		UsernamePasswordLoginCommand command = new UsernamePasswordLoginCommand(request.getUsername(),
				request.getPassword());
		command.setRememberMe(request.getRememberMe());
		return command;
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
