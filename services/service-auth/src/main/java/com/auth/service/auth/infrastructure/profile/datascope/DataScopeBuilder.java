package com.auth.service.auth.infrastructure.profile.datascope;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.granttable.DataScopeKeyUtil;
import com.auth.module.security.contract.api.granttable.ScopeGrantEffectiveMerger;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.service.auth.model.projection.DataScopeProjection;

import java.util.*;

/**
 * 数据权限生效构建器
 *
 * <p>
 * 按文档规则执行：先按维度+资源键分组，再做 USER 优先；若无 USER 则做 ROLE/DEPT/POST 宽松合并，最终兜底 SELF。
 * </p>
 *
 * @author Bunny
 */
public final class DataScopeBuilder {

	private DataScopeBuilder() {
	}

	/**
	 * 构建生效数据权限
	 * @param projections 原始授权片段
	 * @return 生效授权 Map
	 */
	public static Map<String, ScopeGrant> buildDataScopes(List<DataScopeProjection> projections) {
		return buildDataScopesWithTrace(projections).getDataScopes();
	}

	/**
	 * 构建生效数据权限与轨迹
	 * @param projections 原始授权片段
	 * @return 构建结果
	 */
	public static BuildResult buildDataScopesWithTrace(List<DataScopeProjection> projections) {
		if (CollUtil.isEmpty(projections)) {
			return BuildResult.builder().dataScopes(Map.of()).traces(List.of()).build();
		}
		Map<String, List<DataScopeProjection>> grouped = groupByDimensionAndResource(projections);
		Map<String, ScopeGrant> dataScopes = new LinkedHashMap<>(grouped.size());
		List<ResolveTrace> traces = new ArrayList<>(grouped.size());
		for (Map.Entry<String, List<DataScopeProjection>> entry : grouped.entrySet()) {
			String key = entry.getKey();
			List<DataScopeProjection> group = entry.getValue();
			List<DataScopeProjection> candidates = selectCandidates(group);
			ScopeGrant merged = ScopeGrantEffectiveMerger.mergeLoose(toGrants(candidates));
			ScopeGrant effective = merged != null ? merged
					: ScopeGrant.builder().scopeType(ScopeGrantType.SELF).build();
			dataScopes.put(key, effective);
			traces.add(buildTrace(key, group, candidates, effective));
		}
		return BuildResult.builder().dataScopes(Map.copyOf(dataScopes)).traces(List.copyOf(traces)).build();
	}

	private static ResolveTrace buildTrace(String key, List<DataScopeProjection> all,
			List<DataScopeProjection> candidates, ScopeGrant effective) {
		String source;
		if (all.stream().anyMatch(p -> "USER".equalsIgnoreCase(p.getSubjectType()))) {
			source = "USER_OVERRIDE";
		}
		else {
			source = "ROLE_MERGE";
		}
		if (CollUtil.isEmpty(candidates)) {
			source = "SELF_FALLBACK";
		}
		return ResolveTrace.builder()
			.scopeKey(key)
			.source(source)
			.totalPieces(all.size())
			.selectedPieces(candidates.size())
			.effectiveScopeType(effective.getScopeType() != null ? effective.getScopeType().name() : null)
			.valuesCount(effective.getValues() != null ? effective.getValues().size() : 0)
			.build();
	}

	private static List<ScopeGrant> toGrants(List<DataScopeProjection> projections) {
		return projections.stream().map(DataScopeBuilder::toGrant).filter(Objects::nonNull).toList();
	}

	private static ScopeGrant toGrant(DataScopeProjection projection) {
		if (projection == null) {
			return null;
		}
		ScopeGrantType type = parseScopeType(projection.getScopeType());
		return ScopeGrant.builder().scopeType(type).values(parseValues(projection.getScopeSubjectIds())).build();
	}

	private static ScopeGrantType parseScopeType(String rawType) {
		if (CharSequenceUtil.isBlank(rawType)) {
			return ScopeGrantType.SELF;
		}
		try {
			return ScopeGrantType.valueOf(CharSequenceUtil.trim(rawType).toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			return ScopeGrantType.SELF;
		}
	}

	private static List<String> parseValues(String rawValues) {
		if (CharSequenceUtil.isBlank(rawValues)) {
			return List.of();
		}
		String trimmed = CharSequenceUtil.trim(rawValues);
		if (trimmed.startsWith("[")) {
			try {
				return JSONUtil.parseArray(trimmed)
					.stream()
					.map(String::valueOf)
					.filter(CharSequenceUtil::isNotBlank)
					.toList();
			}
			catch (Exception ignored) {
				// fall through to CSV parser
			}
		}
		return Arrays.stream(trimmed.split(","))
			.map(CharSequenceUtil::trim)
			.filter(CharSequenceUtil::isNotBlank)
			.toList();
	}

	private static List<DataScopeProjection> selectCandidates(List<DataScopeProjection> group) {
		List<DataScopeProjection> users = group.stream()
			.filter(p -> p != null && "USER".equalsIgnoreCase(p.getSubjectType()))
			.toList();
		if (CollUtil.isNotEmpty(users)) {
			return users;
		}
		return group.stream().filter(Objects::nonNull).toList();
	}

	private static Map<String, List<DataScopeProjection>> groupByDimensionAndResource(
			List<DataScopeProjection> projections) {
		Map<String, List<DataScopeProjection>> grouped = new LinkedHashMap<>();
		for (DataScopeProjection projection : projections) {
			if (projection == null) {
				continue;
			}
			String key = DataScopeKeyUtil.compose(projection.getDimensionType(), projection.getScopeResourceCode());
			grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(projection);
		}
		return grouped;
	}

}
