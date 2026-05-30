package com.auth.service.auth.authorization.domain.invalidation.port;

import java.util.Collection;

/**
 * 用户权限版本递增端口
 *
 * @author Bunny
 */
public interface UserPermVersionPort {

	/**
	 * 分批递增 {@code perm_version}（仅正常且未删除用户，与 Mapper 防御条件一致）
	 * @param userIds 待递增用户 ID
	 * @param batchSize 每批大小，须 &gt; 0
	 * @return 实际更新行数合计
	 */
	int incrementInBatches(Collection<Long> userIds, int batchSize);

}
