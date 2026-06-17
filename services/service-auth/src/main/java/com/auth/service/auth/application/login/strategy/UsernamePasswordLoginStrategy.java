package com.auth.service.auth.application.login.strategy;

import com.auth.service.auth.application.login.LoginAuditService;
import com.auth.service.auth.application.login.input.UsernamePasswordCommand;
import com.auth.service.auth.application.port.authorization.AuthProfileQueryPort;
import com.auth.service.auth.application.port.login.CredentialPort;
import com.auth.service.auth.domain.authentication.model.LoginAccount;
import com.auth.service.auth.domain.authentication.model.enums.AuthLoginLogType;
import com.auth.service.auth.domain.authentication.model.enums.CredentialDimension;
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
public class UsernamePasswordLoginStrategy extends AbstractLoginStrategyTemplate<UsernamePasswordCommand> {

	private final PasswordEncoder passwordEncoder;

	public UsernamePasswordLoginStrategy(CredentialPort credentialGateway, AuthProfileQueryPort authProfileQueryPort,
			PasswordEncoder passwordEncoder, LoginAuditService loginAuditService) {
		super(credentialGateway, authProfileQueryPort, loginAuditService);
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	@Override
	protected LoginAccount resolveAccount(UsernamePasswordCommand command) {
		return credentialGateway.findForLogin(CredentialDimension.USERNAME, command.getUsername())
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR));
	}

	@Override
	protected AuthLoginLogType doAuthenticate(UsernamePasswordCommand command, LoginAccount account) {
		if (!passwordEncoder.matches(command.getPassword(), account.encodedPassword())) {
			throw new AuthBadCredentialsException("Password mismatch");
		}
		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	@Override
	public Class<UsernamePasswordCommand> supports() {
		return UsernamePasswordCommand.class;
	}

}
