package com.auth.service.auth.authentication.application.assembler;

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
 * HTTP 登录 DTO 与 {@link AbstractLoginCommand} 的装配器（应用层：入站 DTO → 用例命令）。
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
		// 如果请求为空，则抛出异常
		if (request == null) {
			throw new AuthBusinessException(AuthResultCode.AUTH_TYPE_UNSUPPORTED);
		}

		// 如果请求为UsernamePasswordLoginRequest，则创建UsernamePasswordLoginCommand
		if (request instanceof UsernamePasswordLoginRequest usernamePasswordLoginRequest) {
			UsernamePasswordLoginCommand command = new UsernamePasswordLoginCommand(
					usernamePasswordLoginRequest.getUsername(), usernamePasswordLoginRequest.getPassword());
			command.setRememberMe(usernamePasswordLoginRequest.getRememberMe());
			return command;
		}

		// 如果请求为EmailLoginRequest，则创建EmailLoginCommand
		if (request instanceof EmailLoginRequest emailLoginRequest) {
			EmailLoginCommand command = new EmailLoginCommand(emailLoginRequest.getEmail(),
					emailLoginRequest.getCode());
			command.setRememberMe(emailLoginRequest.getRememberMe());
			return command;
		}

		// 如果请求为SmsLoginRequest，则创建SmsLoginCommand
		if (request instanceof SmsLoginRequest smsLoginRequest) {
			SmsLoginCommand command = new SmsLoginCommand(smsLoginRequest.getPhone(), smsLoginRequest.getCode());
			command.setRememberMe(smsLoginRequest.getRememberMe());
			return command;
		}
		throw new AuthBusinessException(AuthResultCode.AUTH_TYPE_UNSUPPORTED);
	}

}
