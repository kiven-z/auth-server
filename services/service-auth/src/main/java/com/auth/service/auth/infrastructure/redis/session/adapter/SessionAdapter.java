package com.auth.service.auth.infrastructure.redis.session.adapter;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.application.port.session.SessionPort;
import com.auth.service.auth.infrastructure.redis.session.RedisUserSessionIndexRepository;
import com.auth.service.auth.infrastructure.redis.session.support.SessionLimitManager;
import com.auth.service.auth.infrastructure.redis.session.support.UserSessionIndexBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * {@link SessionPort} 适配器：委托 Redis 会话仓储、索引构建与会话上限管理。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Repository
public class SessionAdapter implements SessionPort {

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

	@Override
	public List<UserSessionIndex> listActiveSessions(long userId) {
		return redisUserSessionIndexRepository.listUserSessions(userId);
	}

}
