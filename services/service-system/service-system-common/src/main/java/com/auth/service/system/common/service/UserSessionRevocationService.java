package com.auth.service.system.common.service;

import java.util.List;

/**
 * 用户会话撤销：账户状态变更或删除后踢出全部在线会话
 *
 * @author Bunny
 */
public interface UserSessionRevocationService {

	/**
	 * 批量踢出用户全部会话（失败仅记日志，不向外抛异常）
	 * @param userIds 用户 ID 列表
	 */
	void revokeAllSessions(List<Long> userIds);

}
