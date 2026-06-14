package com.auth.module.security.datapermission.strategy;

import com.auth.module.security.contract.api.datascope.DataScopeStorageType;

import java.util.EnumSet;
import java.util.Set;

/**
 * 全部可见范围策略
 *
 * @author Bunny
 */
public class AllScopeSqlStrategy implements ScopeSqlStrategy {

	@Override
	public Set<DataScopeStorageType> supportedTypes() {
		return EnumSet.of(DataScopeStorageType.ALL);
	}

	@Override
	public String buildCondition(ScopeSqlBuildContext context) {
		return null;
	}

}
