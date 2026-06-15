package com.auth.service.auth.infrastructure.redis.session.adapter;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.application.port.session.UserSessionQueryPort;
import com.auth.service.auth.infrastructure.redis.session.RedisUserSessionIndexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户活跃会话查询 Port 适配器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Repository
public class RedisUserSessionQueryAdapter implements UserSessionQueryPort {

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	@Override
	public List<UserSessionIndex> listActiveSessions(long userId) {
		return redisUserSessionIndexRepository.listUserSessions(userId);
	}

}
