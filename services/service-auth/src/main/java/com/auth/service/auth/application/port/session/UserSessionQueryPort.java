package com.auth.service.auth.application.port.session;

import com.auth.module.security.contract.api.UserSessionIndex;

import java.util.List;

/**
 * 用户活跃会话查询端口
 *
 * @author Bunny
 */
public interface UserSessionQueryPort {

	/**
	 * 查询用户活跃会话列表
	 * @param userId 用户 ID
	 * @return 活跃会话列表
	 */
	List<UserSessionIndex> listActiveSessions(long userId);

}
