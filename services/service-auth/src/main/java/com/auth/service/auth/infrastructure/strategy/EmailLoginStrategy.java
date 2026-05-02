package com.auth.service.auth.infrastructure.strategy;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.infrastructure.login.LoginFailureRateLimiter;
import com.auth.service.auth.infrastructure.profile.AuthProfileService;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.request.EmailAuth;
import com.auth.service.auth.service.AuthLoginAuditService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.auth.module.security.contract.redis.SecurityRedisKey.EMAIL_CODE;

/**
 * 邮箱认证提供者
 *
 * @author Bunny
 */
@Component
public class EmailLoginStrategy extends AbstractAuthenticationStrategy<EmailAuth> {

	private final RedisTemplate<String, Object> redisTemplate;

	private final UserMapper userMapper;

	public EmailLoginStrategy(AuthProfileService authProfileService, RedisTemplate<String, Object> redisTemplate,
			UserMapper userMapper, LoginFailureRateLimiter loginFailureRateLimiter,
			AuthLoginAuditService loginLogService) {
		super(loginFailureRateLimiter, authProfileService, loginLogService);
		this.redisTemplate = redisTemplate;
		this.userMapper = userMapper;
	}

	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_EMAIL;
	}

	/**
	 * 解析用户ID
	 * @param auth 邮箱认证请求
	 * @return 用户ID
	 */
	@Override
	protected UserEntity resolveUser(EmailAuth auth) {
		String email = auth.getEmail();
		UserEntity user = userMapper.selectUserByEmail(email);
		if (user == null) {
			// 对外隐藏账号是否存在，统一按"凭证错误"处理
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR);
		}
		return user;
	}

	/**
	 * 认证
	 * @param auth 邮箱认证请求
	 * @param user 用户实体
	 */
	@Override
	protected AuthLoginLogType doAuthenticate(EmailAuth auth, UserEntity user) {
		// 验证码错误：不走 BadCredentialsException，直接返回验证码业务错误码
		String email = Objects.requireNonNull(auth.getEmail());
		String code = Objects.requireNonNull(auth.getCode());
		code = MD5.create().digestHex16(code);

		// 查询当前验证码是否正确
		@NotNull
		String redisKey = EMAIL_CODE.key(email);
		String redisEmailCode = (String) redisTemplate.opsForValue().get(redisKey);

		// 验证邮箱验证码 存入Redis时需要使用MD5加密，取出时需要对比MD5值是否正确
		if (!Objects.equals(code, redisEmailCode)) {
			throw new AuthBusinessException(AuthResultCode.AUTH_CODE_ERROR);
		}

		// 验证成功后消费验证码，避免重放
		deleteEmailCode(auth.getEmail());

		return AuthLoginLogType.LOGIN_EMAIL;
	}

	/**
	 * 删除邮箱验证码
	 * @param email 邮箱
	 */
	private void deleteEmailCode(String email) {
		if (CharSequenceUtil.isBlank(email)) {
			return;
		}
		String redisKey = EMAIL_CODE.key(Objects.requireNonNull(email));
		redisTemplate.delete(redisKey);
	}

	/**
	 * 当前策略支持的认证请求类型（作为注册表 key）
	 * @return EmailAuth
	 */
	@Override
	public Class<EmailAuth> supports() {
		return EmailAuth.class;
	}

}
