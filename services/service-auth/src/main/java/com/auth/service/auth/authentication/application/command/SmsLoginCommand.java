package com.auth.service.auth.authentication.application.command;

import com.auth.service.auth.authentication.api.dto.request.SmsLoginRequest;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import lombok.Getter;

/**
 * 短信验证码登录命令。
 *
 * @author Bunny
 */
@Getter
public class SmsLoginCommand extends AbstractLoginCommand {

	private final String phone;

	private final String code;

	public SmsLoginCommand(String phone, String code) {
		this.phone = phone;
		this.code = code;
	}

	/**
	 * 从 HTTP 请求 DTO 构建命令。
	 * @param request 短信登录请求
	 * @return 登录命令
	 */
	public static SmsLoginCommand from(SmsLoginRequest request) {
		SmsLoginCommand command = new SmsLoginCommand(request.getPhone(), request.getCode());
		command.setRememberMe(request.getRememberMe());
		return command;
	}

	@Override
	public AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_SMS;
	}

	@Override
	public String principalForAudit() {
		return phone;
	}

}
