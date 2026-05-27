package com.auth.service.auth.session.application;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 管理端会话用例：踢人、查询活跃会话
 *
 * @author Bunny
 */
@Slf4j
@Service
public class SessionAdminApplicationService {

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	public SessionAdminApplicationService(RedisUserSessionIndexRepository redisUserSessionIndexRepository) {
		this.redisUserSessionIndexRepository = redisUserSessionIndexRepository;
	}

	/**
	 * 踢出指定会话
	 * @param userId 用户 ID
	 * @param sessionId 会话 ID（jti）
	 */
	public void kickSession(long userId, String sessionId) {
		redisUserSessionIndexRepository.deleteSession(sessionId);
		redisUserSessionIndexRepository.removeActiveSession(userId, sessionId);
	}

	/**
	 * 踢出用户全部会话
	 * @param userId 用户 ID
	 */
	public void kickAllSessions(long userId) {
		redisUserSessionIndexRepository.deleteAllSessions(userId);
	}

	/**
	 * 批量踢出用户全部会话（跳过 null，单用户失败不影响其余用户）
	 * @param userIds 用户 ID 列表
	 */
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

	/**
	 * 查询用户活跃会话列表
	 * @param userId 用户 ID
	 * @return 活跃会话列表
	 */
	public List<UserSessionIndex> listActiveSessions(long userId) {
		return redisUserSessionIndexRepository.listUserSessions(userId);
	}

}
