package com.auth.service.auth.authentication.application.port;

import com.auth.module.security.contract.api.UserSessionIndex;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;

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

	/**
	 * 按 jti 加载会话索引。
	 * @param sessionId 会话 ID（jti）
	 * @return 会话索引，不存在时为空
	 */
	Optional<UserSessionIndex> loadUserSessionIndex(String sessionId);

	/**
	 * 更新刷新令牌校验信息并刷新会话 TTL。
	 * @param sessionId 会话 ID（jti）
	 * @param refreshTokenHash refreshToken 摘要
	 * @param refreshTokenExpiresAt refreshToken 过期时间戳（毫秒）
	 * @param ttl 会话 TTL
	 */
	void updateRefreshInfo(String sessionId, String refreshTokenHash, long refreshTokenExpiresAt, Duration ttl);

}
