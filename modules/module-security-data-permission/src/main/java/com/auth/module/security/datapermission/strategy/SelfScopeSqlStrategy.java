package com.auth.module.security.datapermission.strategy;

import java.util.EnumSet;
import java.util.Set;

import com.auth.module.security.contract.api.datascope.DataScopeStorageType;

/**
 * 仅本人范围策略
 *
 * @author Bunny
 */
public class SelfScopeSqlStrategy implements ScopeSqlStrategy {

	@Override
	public Set<DataScopeStorageType> supportedTypes() {
		return EnumSet.of(DataScopeStorageType.SELF);
	}

	@Override
	public String buildCondition(ScopeSqlBuildContext context) {
		return context.getUserColumn() + " = " + context.getUserId();
	}

}
