package com.auth.service.auth.infrastructure.redis.session.adapter;

import java.util.Collection;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.auth.service.auth.application.port.session.SessionRevocationPort;
import com.auth.service.auth.infrastructure.redis.session.RedisUserSessionIndexRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 会话撤销 Port 适配器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Repository
public class RedisSessionRevocationAdapter implements SessionRevocationPort {

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void kickSession(long userId, String sessionId) {
		redisUserSessionIndexRepository.terminateSession(userId, sessionId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void kickAllSessions(long userId) {
		redisUserSessionIndexRepository.terminateAllSessions(userId);
	}

	/**
	 * {@inheritDoc}
	 */
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
