package com.auth.service.auth.authentication.api.assembler;

import com.auth.service.auth.authentication.api.dto.request.AbstractLoginRequest;
import com.auth.service.auth.authentication.api.dto.request.EmailLoginRequest;
import com.auth.service.auth.authentication.api.dto.request.SmsLoginRequest;
import com.auth.service.auth.authentication.api.dto.request.UsernamePasswordLoginRequest;
import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import com.auth.service.auth.authentication.application.command.EmailLoginCommand;
import com.auth.service.auth.authentication.application.command.SmsLoginCommand;
import com.auth.service.auth.authentication.application.command.UsernamePasswordLoginCommand;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import lombok.experimental.UtilityClass;

/**
 * HTTP 登录 DTO 与 {@link AbstractLoginCommand} 的装配器（api 层边界转换）。
 *
 * @author Bunny
 */
@UtilityClass
public class LoginCommandAssembler {

	/**
	 * 将登录请求 DTO 转为应用层命令。
	 * @param request 登录请求 DTO
	 * @return 登录命令
	 */
	public static AbstractLoginCommand toCommand(AbstractLoginRequest request) {
		if (request == null) {
			throw new AuthBusinessException(AuthResultCode.AUTH_TYPE_UNSUPPORTED);
		}
		if (request instanceof UsernamePasswordLoginRequest usernamePasswordLoginRequest) {
			return UsernamePasswordLoginCommand.from(usernamePasswordLoginRequest);
		}
		if (request instanceof EmailLoginRequest emailLoginRequest) {
			return EmailLoginCommand.from(emailLoginRequest);
		}
		if (request instanceof SmsLoginRequest smsLoginRequest) {
			return SmsLoginCommand.from(smsLoginRequest);
		}
		throw new AuthBusinessException(AuthResultCode.AUTH_TYPE_UNSUPPORTED);
	}

}
