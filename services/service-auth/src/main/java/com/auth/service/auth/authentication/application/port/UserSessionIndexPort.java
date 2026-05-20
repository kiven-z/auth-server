package com.auth.service.auth.authentication.application.port;

import com.auth.module.security.contract.api.UserSessionIndex;
import org.jetbrains.annotations.NotNull;

/**
 * 用户会话索引读写端口（Redis 实现）。
 *
 * @author Bunny
 */
public interface UserSessionIndexPort {

	/**
	 * 创建用户会话索引。
	 * @param userSessionIndex 会话索引
	 */
	void createUserSessionIndex(@NotNull UserSessionIndex userSessionIndex);

	/**
	 * 按 jti 删除会话详情。
	 * @param sessionId 会话 ID（jti）
	 */
	void deleteSession(String sessionId);

	/**
	 * 从用户活跃会话集合中移除指定 jti。
	 * @param userId 用户 ID
	 * @param sessionId 会话 ID（jti）
	 */
	void removeActiveSession(long userId, String sessionId);

}
