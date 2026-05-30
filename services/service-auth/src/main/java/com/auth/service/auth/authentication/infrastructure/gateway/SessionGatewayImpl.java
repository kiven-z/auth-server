package com.auth.service.auth.authentication.infrastructure.gateway;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.authentication.application.gateway.SessionGateway;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import com.auth.service.auth.session.infrastructure.redis.support.SessionLimitManager;
import com.auth.service.auth.session.infrastructure.redis.support.UserSessionIndexBuilder;
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
	public void createUserSessionIndex(@NotNull UserSessionIndex userSessionIndex) {
		redisUserSessionIndexRepository.createUserSessionIndex(userSessionIndex);
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
	public void deleteSession(String sessionId) {
		redisUserSessionIndexRepository.deleteSession(sessionId);
	}

	@Override
	public void removeActiveSession(long userId, String sessionId) {
		redisUserSessionIndexRepository.removeActiveSession(userId, sessionId);
	}

}
