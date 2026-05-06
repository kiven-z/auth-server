package com.auth.service.auth.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.auth.service.auth.model.projection.DataScopeProjection;

/**
 * 用户角色权限 Mapper（认证专用） 查询用户角色 查询用户权限 查询用户数据权限范围 查询用户策略
 *
 * @author Bunny
 */
@Mapper
public interface UserRolePermissionDataScopePolicyMapper {

	/**
	 * 查询用户角色码
	 * @param userId 用户Id
	 * @return 角色列表
	 */
	List<String> selectRolesByUserId(@Param("userId") Long userId);

	/**
	 * 查询用户权限码
	 * @param userId 用户Id
	 * @return 权限列表
	 */
	List<String> selectPermissionsByUserId(@Param("userId") Long userId);

	/**
	 * 查询用户生效数据权限片段（按主体展开后）
	 * @param userId 用户 ID
	 * @return 原始数据权限片段
	 */
	List<DataScopeProjection> selectEffectiveDataScopesByUserId(@Param("userId") Long userId);

}
