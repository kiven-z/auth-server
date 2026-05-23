package com.auth.service.auth.authorization.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 授权影响面：按角色/权限/策略/grant 关联维度反查受影响用户 ID（与 UserRolesCte 同源）
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationImpactMapper {

	/**
	 * 按角色码反查可能持有该角色的用户 ID。
	 * @param roleCodes 角色编码列表，非空
	 * @return 用户 ID 列表（可能含重复，由上层去重）
	 */
	List<Long> selectUserIdsByRoleCodes(@Param("roleCodes") List<String> roleCodes);

}
