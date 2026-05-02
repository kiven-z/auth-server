package com.auth.service.auth.service;

import java.util.List;

/**
 * 内部认证服务
 *
 * @author Bunny
 */
public interface AuthInnerService {

	/**
	 * 刷新用户缓存
	 * @param userIds 用户ID列表
	 */
	void refreshUserCache(List<Long> userIds);

	/**
	 * 踢出所有会话
	 * @param userIds 用户ID列表
	 */
	void kickAllSessions(List<Long> userIds);

}
