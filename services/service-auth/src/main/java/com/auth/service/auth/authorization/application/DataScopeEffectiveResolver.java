package com.auth.service.auth.authorization.application;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.service.auth.authorization.domain.datascope.DataScopeBuildResult;

import java.util.Map;

/**
 * 用户数据权限生效范围解析：聚合授权片段并生成最终生效范围。
 *
 * @author Bunny
 */
public interface DataScopeEffectiveResolver {

	/**
	 * 查询并构建用户生效数据权限。
	 * @param userId 用户 ID
	 * @return 生效数据权限
	 */
	Map<String, ScopeGrant> resolveByUserId(Long userId);

	/**
	 * 查询并构建用户生效数据权限（含轨迹）。
	 * @param userId 用户 ID
	 * @return 生效数据权限构建结果
	 */
	DataScopeBuildResult resolveWithTrace(Long userId);

}
