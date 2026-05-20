package com.auth.service.auth.session.application;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端会话用例：踢人、查询活跃会话。
 *
 * @author Bunny
 */
@Service
public class SessionAdminApplicationService {

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	public SessionAdminApplicationService(RedisUserSessionIndexRepository redisUserSessionIndexRepository) {
		this.redisUserSessionIndexRepository = redisUserSessionIndexRepository;
	}

	/**
	 * 踢出指定会话。
	 * @param userId 用户 ID
	 * @param sessionId 会话 ID（jti）
	 */
	public void kickSession(long userId, String sessionId) {
		redisUserSessionIndexRepository.deleteSession(sessionId);
		redisUserSessionIndexRepository.removeActiveSession(userId, sessionId);
	}

	/**
	 * 踢出用户全部会话。
	 * @param userId 用户 ID
	 */
	public void kickAllSessions(long userId) {
		redisUserSessionIndexRepository.deleteAllSessions(userId);
	}

	/**
	 * 查询用户活跃会话列表。
	 * @param userId 用户 ID
	 * @return 活跃会话列表
	 */
	public List<UserSessionIndex> listActiveSessions(long userId) {
		return redisUserSessionIndexRepository.listUserSessions(userId);
	}

}
