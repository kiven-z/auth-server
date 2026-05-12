package com.auth.service.auth.infrastructure.profile.datascope;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 数据权限构建结果（生效范围与解析轨迹）。
 *
 * @author Bunny
 */
@Getter
@Builder
public class DataScopeBuildResult {

	/**
	 * 生效数据权限
	 */
	private final Map<String, ScopeGrant> dataScopes;

	/**
	 * 规则命中轨迹
	 */
	private final List<DataScopeResolveTrace> traces;

}