package com.auth.service.auth.application.login.strategy;

import com.auth.service.auth.application.login.audit.LoginAuditService;
import com.auth.service.auth.application.login.input.credential.UsernamePasswordLogin;
import com.auth.service.auth.application.port.AuthProfileQueryPort;
import com.auth.service.auth.application.port.CredentialGateway;
import com.auth.service.auth.domain.authentication.enums.AuthLoginLogType;
import com.auth.service.auth.domain.authentication.enums.CredentialDimension;
import com.auth.service.auth.domain.authentication.model.LoginAccount;
import com.auth.service.auth.shared.exception.AuthBadCredentialsException;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.AuthResultCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 用户名密码认证策略：查询用户并用 {@link PasswordEncoder} 校验密码。
 *
 * @author Bunny
 */
@Component
public class UsernamePasswordLoginStrategy extends AbstractLoginStrategyTemplate<UsernamePasswordLogin> {

	private final PasswordEncoder passwordEncoder;

	public UsernamePasswordLoginStrategy(CredentialGateway credentialGateway, AuthProfileQueryPort authProfileQueryPort,
			PasswordEncoder passwordEncoder, LoginAuditService loginAuditService) {
		super(credentialGateway, authProfileQueryPort, loginAuditService);
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	@Override
	protected LoginAccount resolveAccount(UsernamePasswordLogin credential) {
		return credentialGateway.findForLogin(CredentialDimension.USERNAME, credential.getUsername())
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR));
	}

	@Override
	protected AuthLoginLogType doAuthenticate(UsernamePasswordLogin credential, LoginAccount account) {
		if (!passwordEncoder.matches(credential.getPassword(), account.encodedPassword())) {
			throw new AuthBadCredentialsException("Password mismatch");
		}
		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	@Override
	public Class<UsernamePasswordLogin> supports() {
		return UsernamePasswordLogin.class;
	}

}
