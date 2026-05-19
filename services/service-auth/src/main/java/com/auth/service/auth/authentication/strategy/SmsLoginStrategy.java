package com.auth.service.auth.authentication.strategy;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.authentication.audit.event.LoginAuditEventPublisher;
import com.auth.service.auth.authentication.mapper.UserMapper;
import com.auth.service.auth.authentication.service.AuthProfileQueryService;
import com.auth.service.auth.authentication.token.LoginFailureRateLimiter;
import com.auth.service.auth.support.exception.AuthBusinessException;
import com.auth.service.auth.support.model.enums.AuthLoginLogType;
import com.auth.service.auth.support.model.enums.AuthResultCode;
import com.auth.service.auth.support.model.request.login.SmsLoginRequest;
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
public class SmsLoginStrategy extends AbstractLoginStrategy<SmsLoginRequest> {

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
	protected UserEntity resolveUser(SmsLoginRequest auth) {
		String phone = auth.getPhone();
		UserEntity user = userMapper.selectUserByPhone(phone);
		if (user == null) {
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR);
		}
		return user;
	}

	@Override
	protected AuthLoginLogType doAuthenticate(SmsLoginRequest auth, UserEntity user) {
		String phone = Objects.requireNonNull(auth.getPhone());
		String code = Objects.requireNonNull(auth.getCode());

		code = MD5.create().digestHex16(code);
		@NotNull
		String redisKey = SMS_CODE.key(phone);
		String redisSmsCode = (String) redisTemplate.opsForValue().get(redisKey);

		if (!Objects.equals(code, redisSmsCode)) {
			throw new AuthBusinessException(AuthResultCode.AUTH_CODE_ERROR);
		}

		deleteSmsCode(auth.getPhone());

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
	public Class<SmsLoginRequest> supports() {
		return SmsLoginRequest.class;
	}

}
