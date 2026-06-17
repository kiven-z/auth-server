package com.auth.service.auth.infrastructure.redis.adapter;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.auth.domain.authentication.model.enums.CredentialDimension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

import static com.auth.module.security.contract.redis.SecurityRedisKey.EMAIL_CODE;
import static com.auth.module.security.contract.redis.SecurityRedisKey.SMS_CODE;

/**
 * 基于 Redis 的登录验证码存储适配器。
 *
 * @author Bunny
 */
@Repository
public class RedisLoginVerificationCodeAdapter {

	private final RedisTemplate<String, Object> redisTemplate;

	public RedisLoginVerificationCodeAdapter(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
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

	/**
	 * 读取已存储的验证码摘要
	 * @param dimension 凭证维度
	 * @param target 目标（邮箱或手机号）
	 * @return 摘要十六进制，不存在时为 null
	 */
	public String getStoredDigest(CredentialDimension dimension, String target) {
		String redisKey = resolveKey(dimension, target);
		if (redisKey == null) {
			return null;
		}
		return (String) redisTemplate.opsForValue().get(redisKey);
	}

	/**
	 * 写入验证码摘要
	 * @param dimension 凭证维度
	 * @param target 目标
	 * @param digestHex16 摘要十六进制
	 */
	public void storeDigest(CredentialDimension dimension, String target, String digestHex16) {
		String redisKey = resolveKey(dimension, target);
		if (redisKey == null) {
			return;
		}
		var ttl = switch (dimension) {
			case EMAIL -> EMAIL_CODE.getDefaultTtl();
			case PHONE -> SMS_CODE.getDefaultTtl();
			default -> null;
		};
		if (ttl != null) {
			redisTemplate.opsForValue().set(redisKey, digestHex16, ttl);
		}
	}

	/**
	 * 删除验证码
	 * @param dimension 凭证维度
	 * @param target 目标
	 */
	public void deleteCode(CredentialDimension dimension, String target) {
		if (CharSequenceUtil.isBlank(target)) {
			return;
		}
		String redisKey = resolveKey(dimension, target);
		if (redisKey != null) {
			redisTemplate.delete(redisKey);
		}
	}

}
