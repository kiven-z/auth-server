package com.auth.module.security.datapermission.strategy;

import java.util.EnumSet;
import java.util.Set;

import com.auth.module.security.contract.api.granttable.ScopeGrantType;

/**
 * 全量范围策略
 *
 * @author Bunny
 */
public class AllScopeSqlStrategy implements ScopeSqlStrategy {

	@Override
	public Set<ScopeGrantType> supportedTypes() {
		return EnumSet.of(ScopeGrantType.ALL);
	}

	@Override
	public String buildCondition(ScopeSqlBuildContext context) {
		return null;
	}

}
