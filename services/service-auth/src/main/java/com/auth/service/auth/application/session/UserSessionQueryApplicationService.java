package com.auth.service.auth.application.session;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.application.port.session.UserSessionQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端用户活跃会话查询
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class UserSessionQueryApplicationService {

	private final UserSessionQueryPort userSessionQueryPort;

	/**
	 * 查询用户活跃会话列表
	 * @param userId 用户 ID
	 * @return 活跃会话索引列表
	 */
	public List<UserSessionIndex> listActiveSessions(long userId) {
		return userSessionQueryPort.listActiveSessions(userId);
	}

}
