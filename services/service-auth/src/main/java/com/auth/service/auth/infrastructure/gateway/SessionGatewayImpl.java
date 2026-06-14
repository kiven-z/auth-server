package com.auth.service.auth.infrastructure.gateway;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.application.port.SessionGateway;
import com.auth.service.auth.infrastructure.redis.session.RedisUserSessionIndexRepository;
import com.auth.service.auth.infrastructure.redis.session.support.SessionLimitManager;
import com.auth.service.auth.infrastructure.redis.session.support.UserSessionIndexBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * {@link SessionGateway} 实现：委托 Redis 会话仓储、索引构建与会话上限管理。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SessionGatewayImpl implements SessionGateway {

	private final SessionLimitManager sessionLimitManager;

	private final UserSessionIndexBuilder userSessionIndexBuilder;

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	@Override
	public void assertSessionLimit(Long userId) {
		sessionLimitManager.assertSessionLimit(userId);
	}

	@Override
	public UserSessionIndex buildSessionIndex(@NotNull HttpServletRequest request, Long userId, String jti,
			String refreshToken, boolean rememberMe) {
		return userSessionIndexBuilder.build(request, userId, jti, refreshToken, rememberMe);
	}

	@Override
	public void registerSession(@NotNull UserSessionIndex userSessionIndex) {
		redisUserSessionIndexRepository.registerSession(userSessionIndex);
	}

	@Override
	public Optional<UserSessionIndex> loadUserSessionIndex(String sessionId) {
		return redisUserSessionIndexRepository.loadUserSessionIndex(sessionId);
	}

	@Override
	public void updateRefreshInfo(String sessionId, String refreshTokenHash, long refreshTokenExpiresAt, Duration ttl) {
		redisUserSessionIndexRepository.updateRefreshInfo(sessionId, refreshTokenHash, refreshTokenExpiresAt, ttl);
	}

	@Override
	public void terminateSession(long userId, String sessionId) {
		redisUserSessionIndexRepository.terminateSession(userId, sessionId);
	}

}
