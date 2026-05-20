package com.auth.service.auth.authentication.application;

import com.auth.service.auth.authentication.api.dto.request.AbstractLoginRequest;
import com.auth.service.auth.authentication.api.dto.response.RefreshTokenResponse;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP 会话编排：登录/刷新/登出的业务调用与 Refresh Cookie 写入
 *
 * @author Bunny
 */
public interface AuthSessionApplicationService {

	/**
	 * 完成登录：签发令牌、写入 Redis 会话、下发 Refresh Cookie。
	 * @param auth 登录请求（含 rememberMe）
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 * @return 登录响应（含 readMeDay）
	 */
	UserLoginResponse completeLogin(AbstractLoginRequest auth, HttpServletRequest request,
			HttpServletResponse response);

	/**
	 * 完成刷新：旋转令牌、按会话 rememberMe 重写 Refresh Cookie。
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 * @return 刷新响应（含 readMeDay）
	 */
	RefreshTokenResponse completeRefresh(HttpServletRequest request, HttpServletResponse response);

	/**
	 * 完成登出：撤销服务端会话并清除 Refresh Cookie。
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 */
	void completeLogout(HttpServletRequest request, HttpServletResponse response);

}
