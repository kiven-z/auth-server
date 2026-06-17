package com.auth.service.auth.domain.session.model;

import java.util.List;

/**
 * 在线用户分页切片（Port 读模型）
 *
 * @param total 匹配用户总数
 * @param users 当前页用户条目
 * @author Bunny
 */
public record OnlineUserPageSlice(long total, List<OnlineUserEntry> users) {

	/**
	 * 在线用户索引条目
	 *
	 * @param userId 用户 ID
	 * @param lastLoginAt 最近登录时间（毫秒）
	 * @param activeSessionCount 活跃会话数
	 */
	public record OnlineUserEntry(long userId, long lastLoginAt, int activeSessionCount) {

	}

}
