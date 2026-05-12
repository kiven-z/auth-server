package com.auth.module.security.autoconfigure.pipeline.authenticate;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import com.auth.module.security.autoconfigure.config.user.UserConfigProperties;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.contract.redis.SecurityRedisKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_SESSIONS;

/**
 * Redis 会话存在性与并发会话集合校验
 *
 * @author Bunny
 */
@Slf4j
public final class SessionCountChecker {

	/**
	 * Redis 模板
	 */
	private final RedisTemplate<String, Object> redisTemplate;

	/**
	 * 用户配置属性
	 */
	private final UserConfigProperties userConfigProperties;

	public SessionCountChecker(RedisTemplate<String, Object> redisTemplate, UserConfigProperties userConfigProperties) {
		this.redisTemplate = redisTemplate;
		this.userConfigProperties = userConfigProperties;
	}

	/**
	 * 校验会话 Hash 是否存在
	 * @param sessionId 会话 ID（JWT jti）
	 */
	public void verifySessionPresent(String sessionId) {
		if (CharSequenceUtil.isBlank(sessionId)) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID, "Session id is missing.");
		}
		String sessionKey = SecurityRedisKey.USER_SESSION.key(sessionId);
		Boolean exists = redisTemplate.hasKey(sessionKey);
		if (BooleanUtil.isFalse(exists)) {
			throw new SecurityTokenException(SecurityResultCodeEnum.SESSION_MISSING, "Session is missing.");
		}
	}

	/**
	 * 校验活跃会话集合与并发上限（集合由登录流程维护；不存在集合时不做上限校验）
	 * @param userId 用户 ID
	 * @param sessionId 当前会话 ID
	 */
	public void verifyConcurrentSessionsWithinLimit(long userId, String sessionId) {
		if (CharSequenceUtil.isBlank(sessionId)) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID, "Session id is missing.");
		}
		String indexKey = USER_SESSIONS.key(userId);
		Boolean indexExists = redisTemplate.hasKey(indexKey);
		if (BooleanUtil.isFalse(indexExists)) {
			return;
		}
		Long size = redisTemplate.opsForSet().size(indexKey);
		if (size == null || size == 0) {
			throw new SecurityTokenException(SecurityResultCodeEnum.SESSION_INCONSISTENT,
					"Session set is empty - data inconsistent.");
		}
		Boolean member = redisTemplate.opsForSet().isMember(indexKey, sessionId);
		if (!Boolean.TRUE.equals(member)) {
			throw new SecurityTokenException(SecurityResultCodeEnum.SESSION_MISSING, "Session is not active.");
		}
		int max = userConfigProperties.getMaxSessionCount() != null ? userConfigProperties.getMaxSessionCount() : 3;
		if (size > max) {
			log.warn("User {} has {} concurrent sessions, exceeding limit of {}. Consider cleaning up stale sessions.",
					userId, size, max);
			throw new SecurityTokenException(SecurityResultCodeEnum.SESSION_LIMIT_EXCEEDED,
					"Concurrent session limit exceeded.");
		}
	}

}
