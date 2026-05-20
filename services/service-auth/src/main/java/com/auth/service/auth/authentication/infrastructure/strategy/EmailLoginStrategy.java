package com.auth.service.auth.authentication.infrastructure.strategy;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.authentication.application.command.EmailLoginCommand;
import com.auth.service.auth.authentication.application.support.AbstractLoginStrategyTemplate;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.event.LoginAuditEventPublisher;
import com.auth.service.auth.authentication.infrastructure.persistence.UserCredentialField;
import com.auth.service.auth.authentication.infrastructure.persistence.UserCredentialQueries;
import com.auth.service.auth.authentication.infrastructure.persistence.UserMapper;
import com.auth.service.auth.authentication.infrastructure.token.LoginFailureRateLimiter;
import com.auth.service.auth.authorization.application.AuthProfileQueryService;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.auth.module.security.contract.redis.SecurityRedisKey.EMAIL_CODE;

/**
 * 邮箱验证码登录策略
 *
 * @author Bunny
 */
@Component
public class EmailLoginStrategy extends AbstractLoginStrategyTemplate<EmailLoginCommand> {

	private final RedisTemplate<String, Object> redisTemplate;

	private final UserMapper userMapper;

	public EmailLoginStrategy(AuthProfileQueryService authProfileQueryService,
			RedisTemplate<String, Object> redisTemplate, UserMapper userMapper,
			LoginFailureRateLimiter loginFailureRateLimiter, LoginAuditEventPublisher loginAuditEventPublisher) {
		super(loginFailureRateLimiter, authProfileQueryService, loginAuditEventPublisher);
		this.redisTemplate = redisTemplate;
		this.userMapper = userMapper;
	}

	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_EMAIL;
	}

	@Override
	protected UserEntity resolveUser(EmailLoginCommand command) {
		String email = command.getEmail();
		UserEntity user = UserCredentialQueries.forLogin(userMapper, UserCredentialField.EMAIL, email);
		if (user == null) {
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR);
		}
		return user;
	}

	@Override
	protected AuthLoginLogType doAuthenticate(EmailLoginCommand command, UserEntity user) {
		String email = Objects.requireNonNull(command.getEmail());
		String code = Objects.requireNonNull(command.getCode());

		code = MD5.create().digestHex16(code);
		@NotNull
		String redisKey = EMAIL_CODE.key(email);
		String redisEmailCode = (String) redisTemplate.opsForValue().get(redisKey);

		if (!Objects.equals(code, redisEmailCode)) {
			throw new AuthBusinessException(AuthResultCode.AUTH_CODE_ERROR);
		}

		deleteEmailCode(command.getEmail());

		return AuthLoginLogType.LOGIN_EMAIL;
	}

	private void deleteEmailCode(String email) {
		if (CharSequenceUtil.isBlank(email)) {
			return;
		}
		String redisKey = EMAIL_CODE.key(Objects.requireNonNull(email));
		redisTemplate.delete(redisKey);
	}

	@Override
	public Class<EmailLoginCommand> supports() {
		return EmailLoginCommand.class;
	}

}
