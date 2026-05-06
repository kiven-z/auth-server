package com.auth.module.security.datapermission.handler;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.granttable.DataScopeKeyUtil;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.module.security.datapermission.annotation.DataScope;
import com.auth.module.security.datapermission.strategy.AllScopeSqlStrategy;
import com.auth.module.security.datapermission.strategy.DimensionValuesScopeSqlStrategy;
import com.auth.module.security.datapermission.strategy.ScopeSqlBuildContext;
import com.auth.module.security.datapermission.strategy.ScopeSqlStrategy;
import com.auth.module.security.datapermission.strategy.SelfScopeSqlStrategy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;

/**
 * 默认处理器用于部门/自身过滤
 *
 * @author Bunny
 */
public class DefaultDataScopeHandler implements DataScopeHandler {

	private static final String FAIL_CLOSE_CONDITION = "1 = 0";

	private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

	private final Map<ScopeGrantType, ScopeSqlStrategy> strategyRegistry;

	public DefaultDataScopeHandler() {
		this.strategyRegistry = buildStrategyRegistry();
	}

	/**
	 * 构建当前用户的 SQL 条件（仅 USER/ROLE/DEPT 三类主体的范围合并后作用于组织列）
	 */
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
		ScopeGrant grant = resolveGrant(profile, dataScope);
		ScopeGrantType scopeType = resolveScopeType(grant);
		ScopeSqlStrategy strategy = strategyRegistry.getOrDefault(scopeType, strategyRegistry.get(ScopeGrantType.SELF));
		ScopeSqlBuildContext context = ScopeSqlBuildContext.builder()
			.userId(profile.getUserId())
			.alias(safeAlias)
			.userColumn(safeUserColumn)
			.dimensionColumn(safeDimensionColumn)
			.values(normalizeNumericValues(grant != null ? grant.getValues() : List.of()))
			.build();
		return strategy.buildCondition(context);
	}

	private ScopeGrantType resolveScopeType(ScopeGrant grant) {
		if (grant == null || grant.getScopeType() == null) {
			return ScopeGrantType.SELF;
		}
		return grant.getScopeType();
	}

	private ScopeGrant resolveGrant(AuthProfile profile, DataScope dataScope) {
		Map<String, ScopeGrant> dataScopes = profile.getDataScopes();
		if (dataScopes == null || dataScopes.isEmpty()) {
			return ScopeGrant.builder().scopeType(ScopeGrantType.SELF).build();
		}
		String dimension = DataScopeKeyUtil.normalizeDimension(dataScope.dimension());
		String resourceCode = DataScopeKeyUtil.normalizeResourceCode(dataScope.resourceCode());
		String compositeKey = DataScopeKeyUtil.compose(dimension, resourceCode);
		List<String> lookupKeys = List.of(compositeKey, dimension, resourceCode,
				DataScopeKeyUtil.compose(DataScopeKeyUtil.DEFAULT_DIMENSION, resourceCode),
				DataScopeKeyUtil.compose(dimension, DataScopeKeyUtil.DEFAULT_RESOURCE_CODE),
				DataScopeKeyUtil.compose(DataScopeKeyUtil.DEFAULT_DIMENSION, DataScopeKeyUtil.DEFAULT_RESOURCE_CODE));
		return lookupKeys.stream()
			.map(dataScopes::get)
			.filter(Objects::nonNull)
			.findFirst()
			.orElseGet(() -> ScopeGrant.builder().scopeType(ScopeGrantType.SELF).build());
	}

	private List<String> normalizeNumericValues(List<String> values) {
		if (CollUtil.isEmpty(values)) {
			return List.of();
		}
		return values.stream()
			.filter(CharSequenceUtil::isNotBlank)
			.map(CharSequenceUtil::trim)
			.filter(value -> value.matches("\\d+"))
			.distinct()
			.toList();
	}

	private Map<ScopeGrantType, ScopeSqlStrategy> buildStrategyRegistry() {
		Map<ScopeGrantType, ScopeSqlStrategy> registry = new EnumMap<>(ScopeGrantType.class);
		List<ScopeSqlStrategy> strategies = List.of(new AllScopeSqlStrategy(), new SelfScopeSqlStrategy(),
				new DimensionValuesScopeSqlStrategy());
		for (ScopeSqlStrategy strategy : strategies) {
			for (ScopeGrantType type : strategy.supportedTypes()) {
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
