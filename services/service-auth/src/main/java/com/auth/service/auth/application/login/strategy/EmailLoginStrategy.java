package com.auth.service.auth.application.login.strategy;

import cn.hutool.crypto.digest.MD5;
import com.auth.service.auth.application.login.audit.LoginAuditService;
import com.auth.service.auth.application.login.input.credential.EmailLogin;
import com.auth.service.auth.application.port.AuthProfileQueryPort;
import com.auth.service.auth.application.port.CredentialGateway;
import com.auth.service.auth.domain.authentication.enums.AuthLoginLogType;
import com.auth.service.auth.domain.authentication.enums.CredentialDimension;
import com.auth.service.auth.domain.authentication.model.LoginAccount;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.AuthResultCode;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 邮箱验证码登录策略。
 *
 * @author Bunny
 */
@Component
public class EmailLoginStrategy extends AbstractLoginStrategyTemplate<EmailLogin> {

	public EmailLoginStrategy(CredentialGateway credentialGateway, AuthProfileQueryPort authProfileQueryPort,
			LoginAuditService loginAuditService) {
		super(credentialGateway, authProfileQueryPort, loginAuditService);
	}

	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_EMAIL;
	}

	@Override
	protected LoginAccount resolveAccount(EmailLogin credential) {
		return credentialGateway.findForLogin(CredentialDimension.EMAIL, credential.getEmail())
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR));
	}

	@Override
	protected AuthLoginLogType doAuthenticate(EmailLogin credential, LoginAccount account) {
		String email = Objects.requireNonNull(credential.getEmail());
		String codeDigest = MD5.create().digestHex16(Objects.requireNonNull(credential.getCode()));
		String stored = credentialGateway.getStoredDigest(CredentialDimension.EMAIL, email);

		if (!Objects.equals(codeDigest, stored)) {
			throw new AuthBusinessException(AuthResultCode.AUTH_CODE_ERROR);
		}

		credentialGateway.deleteCode(CredentialDimension.EMAIL, email);
		return AuthLoginLogType.LOGIN_EMAIL;
	}

	@Override
	public Class<EmailLogin> supports() {
		return EmailLogin.class;
	}

}
