package com.auth.service.auth.authentication.application.login.strategy;

import cn.hutool.crypto.digest.MD5;
import com.auth.service.auth.authentication.application.command.SmsLoginCommand;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditService;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.enums.CredentialDimension;
import com.auth.service.auth.authentication.domain.model.LoginAccount;
import com.auth.service.auth.authentication.domain.port.LoginAccountLookupPort;
import com.auth.service.auth.authentication.domain.port.LoginFailureRateLimitPort;
import com.auth.service.auth.authentication.domain.port.LoginVerificationCodePort;
import com.auth.service.auth.authorization.domain.port.AuthProfileQueryPort;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 短信验证码登录策略。
 *
 * @author Bunny
 */
@Component
public class SmsLoginStrategy extends AbstractLoginStrategyTemplate<SmsLoginCommand> {

	private final LoginAccountLookupPort loginAccountLookupPort;

	private final LoginVerificationCodePort loginVerificationCodePort;

	public SmsLoginStrategy(LoginAccountLookupPort loginAccountLookupPort,
			LoginFailureRateLimitPort loginFailureRateLimitPort, AuthProfileQueryPort authProfileQueryPort,
			LoginVerificationCodePort loginVerificationCodePort, LoginAuditService loginAuditService) {
		super(loginFailureRateLimitPort, authProfileQueryPort, loginAuditService);
		this.loginAccountLookupPort = loginAccountLookupPort;
		this.loginVerificationCodePort = loginVerificationCodePort;
	}

	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_SMS;
	}

	@Override
	protected LoginAccount resolveAccount(SmsLoginCommand command) {
		return loginAccountLookupPort.findForLogin(CredentialDimension.PHONE, command.getPhone())
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR));
	}

	@Override
	protected AuthLoginLogType doAuthenticate(SmsLoginCommand command, LoginAccount account) {
		String phone = Objects.requireNonNull(command.getPhone());
		String codeDigest = MD5.create().digestHex16(Objects.requireNonNull(command.getCode()));
		String stored = loginVerificationCodePort.getStoredDigest(CredentialDimension.PHONE, phone);

		if (!Objects.equals(codeDigest, stored)) {
			throw new AuthBusinessException(AuthResultCode.AUTH_CODE_ERROR);
		}

		loginVerificationCodePort.deleteCode(CredentialDimension.PHONE, phone);
		return AuthLoginLogType.LOGIN_SMS;
	}

	@Override
	public Class<SmsLoginCommand> supports() {
		return SmsLoginCommand.class;
	}

}
