package com.auth.service.auth.authorization.domain.invalidation.port;

import com.auth.service.auth.authorization.domain.invalidation.model.UserInvalidationState;

import java.util.Collection;
import java.util.List;

/**
 * 用户失效状态加载端口（分桶前查询 status / is_deleted）。
 *
 * @author Bunny
 */
public interface UserInvalidationStateQueryPort {

	/**
	 * 批量加载用户失效分桶所需状态。
	 * @param userIds 用户 ID 集合，非空
	 * @return 状态快照列表
	 */
	List<UserInvalidationState> loadByUserIds(Collection<Long> userIds);

}
