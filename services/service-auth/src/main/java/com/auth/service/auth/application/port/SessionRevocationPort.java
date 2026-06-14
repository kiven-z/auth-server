package com.auth.service.auth.application.port;

import java.util.Collection;

/**
 * 会话撤销端口：供授权失效等场景踢出活跃会话，不暴露管理端查询能力
 *
 * @author Bunny
 */
public interface SessionRevocationPort {

	/**
	 * 踢出指定会话
	 * @param userId 用户 ID
	 * @param sessionId 会话 ID（jti）
	 */
	void kickSession(long userId, String sessionId);

	/**
	 * 踢出用户全部会话
	 * @param userId 用户 ID
	 */
	void kickAllSessions(long userId);

	/**
	 * 批量踢出用户全部会话（跳过 null，单用户失败不影响其余用户）
	 * @param userIds 用户 ID 列表
	 */
	void kickAllSessions(Collection<Long> userIds);

}
