package com.auth.service.auth.session.application;

import com.auth.module.security.contract.api.UserSessionIndex;

import java.util.List;

/**
 * 管理端会话用例：踢人、查询活跃会话。
 *
 * @author Bunny
 */
public interface SessionAdminApplicationService {

	/**
	 * 踢出指定会话。
	 * @param userId 用户 ID
	 * @param sessionId 会话 ID（jti）
	 */
	void kickSession(long userId, String sessionId);

	/**
	 * 踢出用户全部会话。
	 * @param userId 用户 ID
	 */
	void kickAllSessions(long userId);

	/**
	 * 查询用户活跃会话列表。
	 * @param userId 用户 ID
	 * @return 活跃会话列表
	 */
	List<UserSessionIndex> listActiveSessions(long userId);

}
