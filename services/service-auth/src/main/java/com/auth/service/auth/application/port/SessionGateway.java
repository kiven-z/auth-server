package com.auth.service.auth.application.port;

import com.auth.module.security.contract.api.UserSessionIndex;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;

/**
 * 会话基础设施网关：活跃会话上限、Redis 会话索引生命周期与 Refresh 哈希轮换
 *
 * @author Bunny
 */
public interface SessionGateway {

	// --- 会话上限 ---

	/**
	 * 认证成功后检查：活跃会话数达上限时抛会话超限异常
	 * @param userId 用户 ID
	 */
	void assertSessionLimit(Long userId);

	// --- 会话索引构建与持久化 ---

	/**
	 * 根据 HTTP 上下文与刷新令牌构建会话索引。
	 * @param request HTTP 请求
	 * @param userId 用户 ID
	 * @param jti 会话唯一标识
	 * @param refreshToken 明文刷新令牌（用于计算存储哈希）
	 * @param rememberMe 是否记住登录
	 * @return 会话索引
	 */
	UserSessionIndex buildSessionIndex(@NotNull HttpServletRequest request, Long userId, String jti,
			String refreshToken, boolean rememberMe);

	/**
	 * 注册活跃会话索引。
	 * @param userSessionIndex 会话索引
	 */
	void registerSession(@NotNull UserSessionIndex userSessionIndex);

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

	/**
	 * 终止单条会话（Hash、用户 Set；无活跃会话时同步移出在线用户 ZSet）。
	 * @param userId 用户 ID
	 * @param sessionId 会话 ID（jti）
	 */
	void terminateSession(long userId, String sessionId);

}
