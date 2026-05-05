package com.auth.service.auth.mapper;

import com.auth.service.auth.model.projection.DataScopeProjection;
import com.auth.service.auth.model.projection.PolicyProjection;
import com.auth.service.auth.model.projection.RolePermissionProjection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色权限 Mapper（认证专用）
 *
 * @author Bunny
 */
@Mapper
public interface UserRolePermissionMapper {

	/**
	 * 单次查询用户角色与权限（聚合结构）
	 * @param userId 用户Id
	 * @return 角色与权限聚合投影
	 */
	RolePermissionProjection selectRolePermissionByUserId(@Param("userId") long userId);

	/**
	 * 查询用户生效数据权限（用户覆盖优先）
	 * @param userId 用户Id
	 * @return 数据权限行
	 */
	List<DataScopeProjection> selectEffectiveDataScopesByUserId(@Param("userId") long userId);

	/**
	 * 查询用户生效策略
	 * @param userId 用户Id
	 * @return 策略行
	 */
	List<PolicyProjection> selectEffectivePoliciesByUserId(@Param("userId") long userId);

}
