package com.auth.module.security.datapermission.strategy;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 维度值集合范围策略
 *
 * @author Bunny
 */
public class DimensionValuesScopeSqlStrategy implements ScopeSqlStrategy {

	@Override
	public Set<ScopeGrantType> supportedTypes() {
		return EnumSet.of(ScopeGrantType.CUSTOM, ScopeGrantType.NODE_ONLY, ScopeGrantType.NODE_AND_DESC);
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
