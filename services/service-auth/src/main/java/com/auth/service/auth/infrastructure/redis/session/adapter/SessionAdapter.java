package com.auth.service.auth.infrastructure.redis.session.adapter;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.application.port.session.SessionPort;
import com.auth.service.auth.application.port.session.SessionRevocationPort;
import com.auth.service.auth.infrastructure.redis.session.RedisUserSessionIndexRepository;
import com.auth.service.auth.infrastructure.redis.session.support.SessionLimitManager;
import com.auth.service.auth.infrastructure.redis.session.support.UserSessionIndexBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 会话 Port 适配器：Redis 会话生命周期与撤销。
 *
 * @author Bunny
 */
@Slf4j
@RequiredArgsConstructor
@Repository
public class SessionAdapter implements SessionPort, SessionRevocationPort {

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

	@Override
	public void kickAllSessions(long userId) {
		redisUserSessionIndexRepository.terminateAllSessions(userId);
	}

	@Override
	public void kickAllSessions(Collection<Long> userIds) {
		userIds.stream().filter(Objects::nonNull).forEach(userId -> {
			try {
				kickAllSessions(userId);
			}
			catch (RuntimeException ex) {
				log.warn("Failed to kick all sessions for userId={}: {}", userId, ex.getMessage());
			}
		});
	}

}
