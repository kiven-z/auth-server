package com.auth.service.auth.infrastructure.login.strategy;

import com.auth.service.auth.infrastructure.login.LoginResult;
import com.auth.service.auth.model.request.AbstractAuthentication;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录认证策略：根据请求类型执行认证并返回登录结果。
 *
 * @author Bunny
 */
public interface LoginStrategy<T extends AbstractAuthentication> {

	/**
	 * 当前策略支持的认证请求类型（作为注册表 key）
	 * @return 认证请求类型 Class
	 */
	Class<T> supports();

	/**
	 * 执行登录认证。
	 * @param auth 认证请求
	 * @param request 当前 HTTP 请求（用于审计 IP/UA）；允许为 null
	 * @return 登录结果（响应、画像、日志类型）
	 */
	LoginResult authenticate(T auth, HttpServletRequest request);

}
