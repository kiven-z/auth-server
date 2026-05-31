package com.auth.module.security.datapermission.strategy;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 部门 ID 集合范围策略
 *
 * @author Bunny
 */
public class DimensionValuesScopeSqlStrategy implements ScopeSqlStrategy {

	@Override
	public Set<DataScopeStorageType> supportedTypes() {
		return EnumSet.of(DataScopeStorageType.DEPT, DataScopeStorageType.DEPT_AND_CHILD,
				DataScopeStorageType.CUSTOMER);
	}

	@Override
	public String buildCondition(ScopeSqlBuildContext context) {
		if (CollUtil.isEmpty(context.getValues())) {
			return "1 = 0";
		}
		String ids = context.getValues().stream().map(String::valueOf).collect(Collectors.joining(","));
		return context.getDimensionColumn() + " IN (" + ids + ")";
	}

}
