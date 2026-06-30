package com.auth.service.auth.model.value.login.command;

import java.io.Serial;

import com.auth.service.auth.model.enums.AuthLoginLogType;
import lombok.Getter;
import lombok.Setter;

/**
 * 短信验证码登录命令。
 *
 * @author Bunny
 */
@Getter
@Setter
public class SmsCommand extends BaseLoginCommand {

	@Serial
	private static final long serialVersionUID = 1L;

	private String phone;

	private String code;

	@Override
	public AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_SMS;
	}

	@Override
	public String principalForAudit() {
		return phone;
	}

}
