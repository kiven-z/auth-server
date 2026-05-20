package com.auth.service.auth.session.application.impl;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.session.application.SessionAdminApplicationService;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端会话用例实现：委托 Redis 会话索引仓储。
 *
 * @author Bunny
 */
@Service
public class SessionAdminApplicationServiceImpl implements SessionAdminApplicationService {

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	public SessionAdminApplicationServiceImpl(RedisUserSessionIndexRepository redisUserSessionIndexRepository) {
		this.redisUserSessionIndexRepository = redisUserSessionIndexRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void kickSession(long userId, String sessionId) {
		redisUserSessionIndexRepository.deleteSession(sessionId);
		redisUserSessionIndexRepository.removeActiveSession(userId, sessionId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void kickAllSessions(long userId) {
		redisUserSessionIndexRepository.deleteAllSessions(userId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UserSessionIndex> listActiveSessions(long userId) {
		return redisUserSessionIndexRepository.listUserSessions(userId);
	}

}
