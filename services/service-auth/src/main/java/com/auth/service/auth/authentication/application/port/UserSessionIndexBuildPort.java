package com.auth.service.auth.authentication.application.port;

import com.auth.module.security.contract.api.UserSessionIndex;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;

/**
 * 构建登录后会话索引（IP/UA/refresh 哈希等）的端口。
 *
 * @author Bunny
 */
public interface UserSessionIndexBuildPort {

	/**
	 * 根据 HTTP 上下文与刷新令牌构建会话索引。
	 * @param request HTTP 请求
	 * @param userId 用户 ID
	 * @param jti 会话唯一标识
	 * @param refreshToken 明文刷新令牌（用于计算存储哈希）
	 * @param rememberMe 是否记住登录
	 * @return 会话索引
	 */
	UserSessionIndex build(@NotNull HttpServletRequest request, Long userId, String jti, String refreshToken,
			boolean rememberMe);

}
