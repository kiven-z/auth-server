package com.auth.service.auth.authentication.application;

import com.auth.service.auth.authentication.domain.model.RefreshSessionResult;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户刷新Token
 *
 * @author Bunny
 */
public interface RefreshTokenService {

	/**
	 * 刷新令牌
	 * @param refreshToken 刷新令牌
	 * @param request 当前 HTTP 请求（审计 IP/UA）；允许为 null
	 * @return 刷新令牌结果（含 rememberMe，供 Cookie 策略使用）
	 */
	RefreshSessionResult refreshToken(String refreshToken, HttpServletRequest request);

	/**
	 * 按刷新令牌撤销服务端会话（登出等场景；令牌无效时静默跳过）。
	 * @param refreshToken 刷新令牌
	 * @param request 当前 HTTP 请求（审计）；允许为 null
	 */
	void revokeByRefreshToken(String refreshToken, HttpServletRequest request);

}
