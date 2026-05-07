package com.auth.service.auth.infrastructure.profile.datascope;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 构建结果
 *
 * @author Bunny
 */
@Getter
@Builder
public class BuildResult {

	/**
	 * 生效数据权限
	 */
	private final Map<String, ScopeGrant> dataScopes;

	/**
	 * 规则命中轨迹
	 */
	private final List<ResolveTrace> traces;

}