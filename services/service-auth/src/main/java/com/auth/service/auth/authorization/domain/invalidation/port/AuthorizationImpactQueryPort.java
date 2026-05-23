package com.auth.service.auth.authorization.domain.invalidation.port;

import java.util.Collection;
import java.util.Set;

/**
 * 授权影响面查询端口：由 grant 语义反查受影响用户 ID。
 *
 * @author Bunny
 */
public interface AuthorizationImpactQueryPort {

	/**
	 * 按角色码反查所有可能持有该角色的用户 ID（含 USER/DEPT/POST 授权路径，与正向 CTE 同源）。
	 * @param roleCodes 角色编码，非空
	 * @return 去重后的用户 ID 集合
	 */
	Set<Long> findUserIdsByRoleCodes(Collection<String> roleCodes);

}
