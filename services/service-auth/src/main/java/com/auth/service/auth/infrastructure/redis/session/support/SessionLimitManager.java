package com.auth.service.auth.infrastructure.redis.session.support;

import com.auth.module.security.autoconfigure.config.user.SessionLimitStrategy;
import com.auth.module.security.autoconfigure.config.user.UserConfigProperties;
import com.auth.service.auth.infrastructure.redis.session.RedisUserSessionIndexRepository;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.AuthResultCode;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 会话上限管理器
 *
 * @author Bunny
 */
@Component
public class SessionLimitManager {

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	private final UserConfigProperties userConfigProperties;

	public SessionLimitManager(RedisUserSessionIndexRepository redisUserSessionIndexRepository,
			UserConfigProperties userConfigProperties) {
		this.redisUserSessionIndexRepository = redisUserSessionIndexRepository;
		this.userConfigProperties = userConfigProperties;
	}

	/**
	 * 认证成功后检查：活跃会话数达上限时抛会话超限异常
	 * @param userId 用户 ID
	 * @throws AuthBusinessException 会话达上限且策略为拒绝时抛出
	 */
	public void assertSessionLimit(Long userId) {
		Assert.notNull(userId, "userId must not be null");
		redisUserSessionIndexRepository.cleanupStaleActiveSessions(userId);
		Integer maxSessionCount = userConfigProperties.getMaxSessionCount();
		if (maxSessionCount == null || maxSessionCount <= 0) {
			return;
		}
		long sessionCount = redisUserSessionIndexRepository.countActiveSessions(userId);

		// 无会话或未达上限：直接放行
		if (sessionCount < maxSessionCount) {
			return;
		}

		SessionLimitStrategy limitStrategy = userConfigProperties.getSessionLimitStrategy();
		if (limitStrategy == null) {
			throw new AuthBusinessException(AuthResultCode.MAX_SESSION_LIMIT_EXCEEDED);
		}
		switch (limitStrategy) {
			// 达到上限时拒绝本次登录
			case REJECT_LOGIN -> throw new AuthBusinessException(AuthResultCode.MAX_SESSION_LIMIT_EXCEEDED);
			// 达到上限时踢出最旧会话
			case EVICT_OLDEST -> {
				redisUserSessionIndexRepository.evictOldestActiveSession(userId);
				long sessionCountAfterEvict = redisUserSessionIndexRepository.countActiveSessions(userId);
				if (sessionCountAfterEvict >= maxSessionCount) {
					throw new AuthBusinessException(AuthResultCode.MAX_SESSION_LIMIT_EXCEEDED);
				}
			}
			default -> throw new AuthBusinessException(AuthResultCode.MAX_SESSION_LIMIT_EXCEEDED);
		}
	}

}
