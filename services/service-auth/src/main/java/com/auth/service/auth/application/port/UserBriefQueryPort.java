package com.auth.service.auth.application.port;

import com.auth.service.auth.domain.session.UserOnlineBrief;

import java.util.Collection;
import java.util.Map;

/**
 * 在线会话列表所需的用户展示快照查询端口
 *
 * @author Bunny
 */
public interface UserBriefQueryPort {

	/**
	 * 按用户 ID 批量查询展示快照
	 * @param userIds 用户 ID 集合
	 * @return 用户 ID 到展示快照的映射
	 */
	Map<Long, UserOnlineBrief> findByIds(Collection<Long> userIds);

}
