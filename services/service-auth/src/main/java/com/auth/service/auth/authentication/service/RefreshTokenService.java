package com.auth.service.auth.authentication.service;

import com.auth.service.auth.authentication.token.RefreshSessionResult;
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

}
