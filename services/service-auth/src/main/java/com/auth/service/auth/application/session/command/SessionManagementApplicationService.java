package com.auth.service.auth.application.session.command;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.application.port.session.SessionPort;
import com.auth.service.auth.application.port.session.SessionRevocationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 会话管理用例编排：踢人、查询活跃会话
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SessionManagementApplicationService {

	private final SessionRevocationPort sessionRevocationPort;

	private final SessionPort sessionPort;

	/**
	 * 踢出指定会话
	 * @param userId 用户 ID
	 * @param sessionId 会话 ID（jti）
	 */
	public void kickSession(long userId, String sessionId) {
		sessionRevocationPort.kickSession(userId, sessionId);
	}

	/**
	 * 踢出用户全部会话
	 * @param userId 用户 ID
	 */
	public void kickAllSessions(long userId) {
		sessionRevocationPort.kickAllSessions(userId);
	}

	/**
	 * 批量踢出用户全部会话
	 * @param userIds 用户 ID 列表
	 */
	public void kickAllSessions(Collection<Long> userIds) {
		sessionRevocationPort.kickAllSessions(userIds);
	}

	/**
	 * 查询用户活跃会话列表
	 * @param userId 用户 ID
	 * @return 活跃会话列表
	 */
	public List<UserSessionIndex> listActiveSessions(long userId) {
		return sessionPort.listActiveSessions(userId);
	}

}
