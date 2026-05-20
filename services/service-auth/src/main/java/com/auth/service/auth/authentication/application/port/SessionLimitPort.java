package com.auth.service.auth.authentication.application.port;

/**
 * 登录前会话数量上限检查端口。
 *
 * @author Bunny
 */
public interface SessionLimitPort {

	/**
	 * 认证成功后检查：活跃会话数达上限时抛会话超限异常。
	 * @param userId 用户 ID
	 */
	void assertSessionLimit(Long userId);

}
