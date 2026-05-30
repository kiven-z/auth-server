package com.auth.module.security.datapermission.strategy;

import java.util.EnumSet;
import java.util.Set;

import com.auth.module.security.contract.api.granttable.ScopeGrantType;

/**
 * 仅本人范围策略
 *
 * @author Bunny
 */
public class SelfScopeSqlStrategy implements ScopeSqlStrategy {

	@Override
	public Set<ScopeGrantType> supportedTypes() {
		return EnumSet.of(ScopeGrantType.SELF);
	}

	@Override
	public String buildCondition(ScopeSqlBuildContext context) {
		return context.getUserColumn() + " = " + context.getUserId();
	}

}
