package com.auth.service.auth.authentication.infrastructure.strategy;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.authentication.application.command.UsernamePasswordLoginCommand;
import com.auth.service.auth.authentication.application.support.AbstractLoginStrategyTemplate;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.event.LoginAuditEventPublisher;
import com.auth.service.auth.authentication.infrastructure.persistence.UserCredentialField;
import com.auth.service.auth.authentication.infrastructure.persistence.UserCredentialQueries;
import com.auth.service.auth.authentication.infrastructure.persistence.UserMapper;
import com.auth.service.auth.authentication.infrastructure.token.LoginFailureRateLimiter;
import com.auth.service.auth.authorization.application.AuthProfileQueryService;
import com.auth.service.auth.shared.exception.AuthBadCredentialsException;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 用户名密码认证策略：查询用户并用 {@link PasswordEncoder} 校验密码
 *
 * @author Bunny
 */
@Component
public class UsernamePasswordLoginStrategy extends AbstractLoginStrategyTemplate<UsernamePasswordLoginCommand> {

	private final UserMapper userMapper;

	private final PasswordEncoder passwordEncoder;

	public UsernamePasswordLoginStrategy(UserMapper userMapper, LoginFailureRateLimiter loginFailureRateLimiter,
			AuthProfileQueryService authProfileQueryService, PasswordEncoder passwordEncoder,
			LoginAuditEventPublisher loginAuditEventPublisher) {
		super(loginFailureRateLimiter, authProfileQueryService, loginAuditEventPublisher);
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	@Override
	protected UserEntity resolveUser(UsernamePasswordLoginCommand command) {
		String username = command.getUsername();
		UserEntity user = UserCredentialQueries.forLogin(userMapper, UserCredentialField.USERNAME, username);
		if (user == null) {
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR);
		}
		return user;
	}

	@Override
	protected AuthLoginLogType doAuthenticate(UsernamePasswordLoginCommand command, UserEntity user) {
		String encodedPassword = user.getPassword();
		if (!passwordEncoder.matches(command.getPassword(), encodedPassword)) {
			throw new AuthBadCredentialsException("Password mismatch");
		}
		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	@Override
	public Class<UsernamePasswordLoginCommand> supports() {
		return UsernamePasswordLoginCommand.class;
	}

}
