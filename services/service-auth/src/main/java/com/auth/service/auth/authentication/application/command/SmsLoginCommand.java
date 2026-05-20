package com.auth.service.auth.authentication.application.command;

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

	@Override
	public AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_SMS;
	}

	@Override
	public String principalForAudit() {
		return phone;
	}

}
