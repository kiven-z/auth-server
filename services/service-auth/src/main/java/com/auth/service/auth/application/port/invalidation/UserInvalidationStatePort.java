package com.auth.service.auth.application.port.invalidation;

import com.auth.service.auth.domain.authorization.model.UserInvalidationState;

import java.util.Collection;
import java.util.List;

/**
 * 用户失效状态读写端口：分桶前加载状态、分批递增 perm_version。
 *
 * @author Bunny
 */
public interface UserInvalidationStatePort {

	/**
	 * 批量加载用户失效分桶所需状态
	 * @param userIds 用户 ID 集合，非空
	 * @return 状态快照列表
	 */
	List<UserInvalidationState> loadByUserIds(Collection<Long> userIds);

	/**
	 * 分批递增 {@code perm_version}（仅正常且未删除用户，与 Mapper 防御条件一致）
	 * @param userIds 待递增用户 ID
	 * @param batchSize 每批大小，须 &gt; 0
	 * @return 实际更新行数合计
	 */
	int incrementPermVersionInBatches(Collection<Long> userIds, int batchSize);

}
