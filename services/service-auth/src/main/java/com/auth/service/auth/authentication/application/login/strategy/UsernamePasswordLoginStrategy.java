package com.auth.service.auth.authentication.application.login.strategy;

import com.auth.service.auth.authentication.application.command.UsernamePasswordLoginCommand;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditCoordinator;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.enums.CredentialDimension;
import com.auth.service.auth.authentication.domain.model.LoginAccount;
import com.auth.service.auth.authentication.domain.port.LoginAccountLookupPort;
import com.auth.service.auth.authentication.domain.port.LoginFailureRateLimitPort;
import com.auth.service.auth.authorization.domain.port.AuthProfileQueryPort;
import com.auth.service.auth.shared.exception.AuthBadCredentialsException;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 用户名密码认证策略：查询用户并用 {@link PasswordEncoder} 校验密码。
 *
 * @author Bunny
 */
@Component
public class UsernamePasswordLoginStrategy extends AbstractLoginStrategyTemplate<UsernamePasswordLoginCommand> {

	private final LoginAccountLookupPort loginAccountLookupPort;

	private final PasswordEncoder passwordEncoder;

	public UsernamePasswordLoginStrategy(LoginAccountLookupPort loginAccountLookupPort,
			LoginFailureRateLimitPort loginFailureRateLimitPort, AuthProfileQueryPort authProfileQueryPort,
			PasswordEncoder passwordEncoder, LoginAuditCoordinator loginAuditCoordinator) {
		super(loginFailureRateLimitPort, authProfileQueryPort, loginAuditCoordinator);
		this.loginAccountLookupPort = loginAccountLookupPort;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	@Override
	protected LoginAccount resolveAccount(UsernamePasswordLoginCommand command) {
		return loginAccountLookupPort.findForLogin(CredentialDimension.USERNAME, command.getUsername())
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR));
	}

	@Override
	protected AuthLoginLogType doAuthenticate(UsernamePasswordLoginCommand command, LoginAccount account) {
		if (!passwordEncoder.matches(command.getPassword(), account.encodedPassword())) {
			throw new AuthBadCredentialsException("Password mismatch");
		}
		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	@Override
	public Class<UsernamePasswordLoginCommand> supports() {
		return UsernamePasswordLoginCommand.class;
	}

}
