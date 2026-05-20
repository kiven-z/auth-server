package com.auth.service.auth.authentication.application;

import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证服务
 *
 * @author Bunny
 */
public interface AuthService {

	/**
	 * 登录
	 * @param command 认证信息
	 * @param request 请求
	 * @return 登录响应
	 */
	UserLoginResponse login(AbstractLoginCommand command, HttpServletRequest request);

	/**
	 * 登出当前会话（永远不抛出异常） 幂等：会话不存在/索引不存在都视为成功 永远成功：token 无效/过期/解析失败时不抛错，仅执行客户端登出
	 * @param accessToken Access Token（不含 Bearer 前缀）
	 * @param request HTTP 请求
	 */
	void logout(String accessToken, HttpServletRequest request);

}
