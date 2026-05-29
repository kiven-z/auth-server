package com.auth.module.security.datapermission.handler;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeProfileKeys;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.module.security.datapermission.annotation.DataScope;
import com.auth.module.security.datapermission.strategy.*;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cn.hutool.core.text.CharSequenceUtil;

/**
 * 默认处理器用于部门/自身过滤
 *
 * @author Bunny
 */
public class DefaultDataScopeHandler implements DataScopeHandler {

	private static final String FAIL_CLOSE_CONDITION = "1 = 0";

	private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z_]\\w*$");

	private final Map<ScopeGrantType, ScopeSqlStrategy> strategyRegistry;

	public DefaultDataScopeHandler() {
		this.strategyRegistry = buildStrategyRegistry();
	}

	/**
	 * 构建当前用户的 SQL 条件
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
			.values(normalizeValues(grant != null ? grant.getValues() : List.of()))
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
		String resourceKey = DataScopeProfileKeys.forResource(dataScope.resourceCode());
		ScopeGrant grant = dataScopes.get(resourceKey);
		if (grant == null && !DataScopeProfileKeys.DEFAULT.equals(resourceKey)) {
			grant = dataScopes.get(DataScopeProfileKeys.DEFAULT);
		}
		return grant != null ? grant : ScopeGrant.builder().scopeType(ScopeGrantType.SELF).build();
	}

	private List<Long> normalizeValues(List<Long> values) {
		if (CollUtil.isEmpty(values)) {
			return List.of();
		}
		return values.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), List::copyOf));
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
