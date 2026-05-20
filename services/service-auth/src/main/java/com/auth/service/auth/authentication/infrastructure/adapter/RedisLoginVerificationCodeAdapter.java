package com.auth.service.auth.authentication.infrastructure.adapter;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.auth.authentication.domain.enums.CredentialDimension;
import com.auth.service.auth.authentication.domain.port.LoginVerificationCodePort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.auth.module.security.contract.redis.SecurityRedisKey.EMAIL_CODE;
import static com.auth.module.security.contract.redis.SecurityRedisKey.SMS_CODE;

/**
 * 基于 Redis 的登录验证码存储适配器。
 *
 * @author Bunny
 */
@Component
public class RedisLoginVerificationCodeAdapter implements LoginVerificationCodePort {

	private final RedisTemplate<String, Object> redisTemplate;

	public RedisLoginVerificationCodeAdapter(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public String getStoredDigest(CredentialDimension dimension, String target) {
		String redisKey = resolveKey(dimension, target);
		if (redisKey == null) {
			return null;
		}
		return (String) redisTemplate.opsForValue().get(redisKey);
	}

	@Override
	public void deleteCode(CredentialDimension dimension, String target) {
		if (CharSequenceUtil.isBlank(target)) {
			return;
		}
		String redisKey = resolveKey(dimension, target);
		if (redisKey != null) {
			redisTemplate.delete(redisKey);
		}
	}

	private static String resolveKey(CredentialDimension dimension, String target) {
		if (CharSequenceUtil.isBlank(target)) {
			return null;
		}
		String normalized = Objects.requireNonNull(target);
		return switch (dimension) {
			case EMAIL -> EMAIL_CODE.key(normalized);
			case PHONE -> SMS_CODE.key(normalized);
			default -> null;
		};
	}

}
