package com.auth.service.auth.authentication.domain.port;

/**
 * 登录失败计数与账户锁定端口。
 *
 * @author Bunny
 */
public interface LoginFailureRateLimitPort {

	/**
	 * 登录前置只读检查：失败次数达阈值时抛出锁定业务异常。
	 * @param userId 用户 ID
	 */
	void assertNotLocked(Long userId);

	/**
	 * 记录一次认证失败并返回剩余可尝试次数。
	 * @param userId 用户 ID
	 * @return 剩余次数
	 */
	int recordFailure(Long userId);

	/**
	 * 认证成功时清除失败计数。
	 * @param userId 用户 ID
	 */
	void recordSuccess(Long userId);

}
