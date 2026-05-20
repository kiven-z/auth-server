package com.auth.service.auth.authentication.infrastructure.strategy;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.authentication.application.command.SmsLoginCommand;
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

import static com.auth.module.security.contract.redis.SecurityRedisKey.SMS_CODE;

/**
 * 短信验证码登录策略
 *
 * @author Bunny
 */
@Component
public class SmsLoginStrategy extends AbstractLoginStrategyTemplate<SmsLoginCommand> {

	private final RedisTemplate<String, Object> redisTemplate;

	private final UserMapper userMapper;

	public SmsLoginStrategy(AuthProfileQueryService authProfileQueryService,
			RedisTemplate<String, Object> redisTemplate, UserMapper userMapper,
			LoginFailureRateLimiter loginFailureRateLimiter, LoginAuditEventPublisher loginAuditEventPublisher) {
		super(loginFailureRateLimiter, authProfileQueryService, loginAuditEventPublisher);
		this.redisTemplate = redisTemplate;
		this.userMapper = userMapper;
	}

	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_SMS;
	}

	@Override
	protected UserEntity resolveUser(SmsLoginCommand command) {
		String phone = command.getPhone();
		UserEntity user = UserCredentialQueries.forLogin(userMapper, UserCredentialField.PHONE, phone);
		if (user == null) {
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR);
		}
		return user;
	}

	@Override
	protected AuthLoginLogType doAuthenticate(SmsLoginCommand command, UserEntity user) {
		String phone = Objects.requireNonNull(command.getPhone());
		String code = Objects.requireNonNull(command.getCode());

		code = MD5.create().digestHex16(code);
		@NotNull
		String redisKey = SMS_CODE.key(phone);
		String redisSmsCode = (String) redisTemplate.opsForValue().get(redisKey);

		if (!Objects.equals(code, redisSmsCode)) {
			throw new AuthBusinessException(AuthResultCode.AUTH_CODE_ERROR);
		}

		deleteSmsCode(command.getPhone());

		return AuthLoginLogType.LOGIN_SMS;
	}

	private void deleteSmsCode(String phone) {
		if (CharSequenceUtil.isBlank(phone)) {
			return;
		}
		String redisKey = SMS_CODE.key(Objects.requireNonNull(phone));
		redisTemplate.delete(redisKey);
	}

	@Override
	public Class<SmsLoginCommand> supports() {
		return SmsLoginCommand.class;
	}

}
