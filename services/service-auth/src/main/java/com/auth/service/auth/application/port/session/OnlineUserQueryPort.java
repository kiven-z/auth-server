package com.auth.service.auth.application.port.session;

import com.auth.service.auth.domain.session.OnlineUserPageSlice;

import java.util.Set;

/**
 * 在线用户索引查询端口
 *
 * @author Bunny
 */
public interface OnlineUserQueryPort {

	/**
	 * 分页扫描在线用户 ZSet
	 * @param pageIndex 页码（从 1 开始）
	 * @param pageSize 每页条数
	 * @param allowedUserIds 允许的用户 ID 集合，null 表示不过滤
	 * @return 分页切片
	 */
	OnlineUserPageSlice pageOnlineUsers(int pageIndex, int pageSize, Set<Long> allowedUserIds);

	/**
	 * 统计在线用户 ZSet 成员数
	 * @return 在线用户数
	 */
	long countOnlineUsers();

}
