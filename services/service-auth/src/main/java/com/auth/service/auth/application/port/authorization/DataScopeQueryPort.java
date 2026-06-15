package com.auth.service.auth.application.port.authorization;

import com.auth.service.auth.application.authorization.datascope.RoleScopeRow;
import com.auth.service.auth.application.authorization.datascope.UserScopeRow;

import java.util.List;
import java.util.Optional;

/**
 * 数据权限范围查询端口
 *
 * @author Bunny
 */
public interface DataScopeQueryPort {

	/**
	 * 查询用户级数据权限
	 * @param userId 用户 ID
	 * @return 用户 scope 行，不存在时为空
	 */
	Optional<UserScopeRow> findUserScopeByUserId(Long userId);

	/**
	 * 按角色编码查询 role_scope
	 * @param roleCodes 角色编码列表
	 * @return role_scope 行列表
	 */
	List<RoleScopeRow> findRoleScopesByRoleCodes(List<String> roleCodes);

	/**
	 * 展开部门闭包后代 ID
	 * @param ancestorDeptIds 祖先部门 ID 列表
	 * @return 后代部门 ID 列表
	 */
	List<Long> expandDescendantDeptIds(List<Long> ancestorDeptIds);

}
