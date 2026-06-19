package com.auth.module.security.datapermission.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.module.security.datapermission.annotation.DataScope;
import com.auth.module.security.datapermission.strategy.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 默认处理器用于部门/自身过滤
 *
 * @author Bunny
 */
public class DefaultDataScopeHandler implements DataScopeHandler {

	private static final String FAIL_CLOSE_CONDITION = "1 = 0";

	private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z_]\\w*$");

	private final Map<DataScopeStorageType, ScopeSqlStrategy> strategyRegistry;

	public DefaultDataScopeHandler() {
		this.strategyRegistry = buildStrategyRegistry();
	}

	@Override
	public String buildCondition(AuthProfile profile, DataScope dataScope) {
		if (profile == null || profile.getUserId() == null || dataScope == null) {
			return null;
		}
		if (PermissionConstant.isSuperAdmin(profile.getUserId()) && !dataScope.ignoreSuperAdmin()) {
			return null;
		}
		String safeAlias = normalizeAlias(dataScope.alias());
		String safeUserColumn = safeQualifiedColumn(safeAlias, dataScope.userColumn());
		String safeDimensionColumn = safeQualifiedColumn(safeAlias, dataScope.dimensionColumn());
		if (safeUserColumn == null || safeDimensionColumn == null) {
			return FAIL_CLOSE_CONDITION;
		}
		ScopeGrant grant = resolveGrant(profile);
		DataScopeStorageType scopeType = ObjectUtil.defaultIfNull(grant.getScopeType(), DataScopeStorageType.SELF);
		ScopeSqlStrategy strategy = strategyRegistry.getOrDefault(scopeType,
				strategyRegistry.get(DataScopeStorageType.SELF));
		ScopeSqlBuildContext context = ScopeSqlBuildContext.builder()
			.userId(profile.getUserId())
			.alias(safeAlias)
			.userColumn(safeUserColumn)
			.dimensionColumn(safeDimensionColumn)
			.values(normalizeValues(grant.getValues()))
			.build();
		return strategy.buildCondition(context);
	}

	private ScopeGrant resolveGrant(AuthProfile profile) {
		ScopeGrant grant = profile.getDeptScope();
		if (grant == null || grant.getScopeType() == null) {
			return ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).build();
		}
		return grant;
	}

	private List<Long> normalizeValues(List<Long> values) {
		if (CollUtil.isEmpty(values)) {
			return List.of();
		}
		return values.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), List::copyOf));
	}

	private Map<DataScopeStorageType, ScopeSqlStrategy> buildStrategyRegistry() {
		Map<DataScopeStorageType, ScopeSqlStrategy> registry = new EnumMap<>(DataScopeStorageType.class);
		List<ScopeSqlStrategy> strategies = List.of(new AllScopeSqlStrategy(), new SelfScopeSqlStrategy(),
				new DimensionValuesScopeSqlStrategy());
		for (ScopeSqlStrategy strategy : strategies) {
			for (DataScopeStorageType type : strategy.supportedTypes()) {
				registry.put(type, strategy);
			}
		}
		return registry;
	}

	private String normalizeAlias(String alias) {
		if (CharSequenceUtil.isBlank(alias)) {
			return null;
		}
		String normalized = CharSequenceUtil.trim(alias);
		if (!SAFE_IDENTIFIER.matcher(normalized).matches()) {
			return null;
		}
		return normalized;
	}

	private String safeQualifiedColumn(String alias, String column) {
		if (CharSequenceUtil.isBlank(column)) {
			return null;
		}
		String normalizedColumn = CharSequenceUtil.trim(column);
		if (!SAFE_IDENTIFIER.matcher(normalizedColumn).matches()) {
			return null;
		}
		if (CharSequenceUtil.isBlank(alias)) {
			return normalizedColumn;
		}
		return alias + "." + normalizedColumn;
	}

}
