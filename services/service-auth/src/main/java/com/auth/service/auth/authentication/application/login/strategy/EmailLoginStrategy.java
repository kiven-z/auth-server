package com.auth.service.auth.authentication.application.login.strategy;

import cn.hutool.crypto.digest.MD5;
import com.auth.service.auth.authentication.application.command.EmailLoginCommand;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditCoordinator;
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
 * 邮箱验证码登录策略。
 *
 * @author Bunny
 */
@Component
public class EmailLoginStrategy extends AbstractLoginStrategyTemplate<EmailLoginCommand> {

	private final LoginAccountLookupPort loginAccountLookupPort;

	private final LoginVerificationCodePort loginVerificationCodePort;

	public EmailLoginStrategy(LoginAccountLookupPort loginAccountLookupPort,
			LoginFailureRateLimitPort loginFailureRateLimitPort, AuthProfileQueryPort authProfileQueryPort,
			LoginVerificationCodePort loginVerificationCodePort, LoginAuditCoordinator loginAuditCoordinator) {
		super(loginFailureRateLimitPort, authProfileQueryPort, loginAuditCoordinator);
		this.loginAccountLookupPort = loginAccountLookupPort;
		this.loginVerificationCodePort = loginVerificationCodePort;
	}

	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_EMAIL;
	}

	@Override
	protected LoginAccount resolveAccount(EmailLoginCommand command) {
		return loginAccountLookupPort.findForLogin(CredentialDimension.EMAIL, command.getEmail())
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR));
	}

	@Override
	protected AuthLoginLogType doAuthenticate(EmailLoginCommand command, LoginAccount account) {
		String email = Objects.requireNonNull(command.getEmail());
		String codeDigest = MD5.create().digestHex16(Objects.requireNonNull(command.getCode()));
		String stored = loginVerificationCodePort.getStoredDigest(CredentialDimension.EMAIL, email);

		if (!Objects.equals(codeDigest, stored)) {
			throw new AuthBusinessException(AuthResultCode.AUTH_CODE_ERROR);
		}

		loginVerificationCodePort.deleteCode(CredentialDimension.EMAIL, email);
		return AuthLoginLogType.LOGIN_EMAIL;
	}

	@Override
	public Class<EmailLoginCommand> supports() {
		return EmailLoginCommand.class;
	}

}
