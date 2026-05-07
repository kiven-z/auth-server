package com.auth.service.auth.infrastructure.profile.datascope;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.granttable.DataScopeKeyUtil;
import com.auth.module.security.contract.api.granttable.ScopeGrantEffectiveMerger;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.service.auth.model.projection.DataScopeProjection;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

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
@UtilityClass
@Slf4j
public class DataScopeBuilder {

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

	/**
	 * 构建轨迹
	 * @param key 范围键
	 * @param all 所有范围
	 * @param candidates 候选范围
	 * @param effective 生效范围
	 * @return 轨迹
	 */
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

	/**
	 * 转换为授权列表
	 * @param projections 原始授权片段
	 * @return 授权列表
	 */
	private static List<ScopeGrant> toGrants(List<DataScopeProjection> projections) {
		return projections.stream().map(DataScopeBuilder::toGrant).filter(Objects::nonNull).toList();
	}

	/**
	 * 转换为授权
	 * @param projection 原始授权片段
	 * @return 授权
	 */
	private static ScopeGrant toGrant(DataScopeProjection projection) {
		if (projection == null) {
			return null;
		}
		ScopeGrantType type = parseScopeType(projection.getScopeType());
		return ScopeGrant.builder()
			.scopeType(type)
			.values(parseValuesByScopeType(type, projection.getScopeSubjectIds()))
			.build();
	}

	/**
	 * 解析范围类型
	 * @param rawType 原始范围类型
	 * @return 范围类型
	 */
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

	/**
	 * 按范围类型解析值列表。
	 *
	 * <p>
	 * 约束约定： 1. CUSTOM / NODE_* 允许携带 scope_subject_ids； 2. ALL / SELF 忽略
	 * scope_subject_ids； 3. 非 CUSTOM 且 values 为空时，后续合并器会走 restrictiveWithoutIds 分支，最终由
	 * Handler fail-close。
	 * </p>
	 */
	private static List<String> parseValuesByScopeType(ScopeGrantType scopeType, String rawValues) {
		if (scopeType == ScopeGrantType.ALL || scopeType == ScopeGrantType.SELF) {
			return List.of();
		}
		return parseRawValues(rawValues);
	}

	/**
	 * 解析原始值列表
	 * @param rawValues 原始值列表
	 * @return 值列表
	 */
	private static List<String> parseRawValues(String rawValues) {
		if (CharSequenceUtil.isBlank(rawValues)) {
			return List.of();
		}
		String trimmed = CharSequenceUtil.trim(rawValues);
		String prefix = "[";
		if (trimmed.startsWith(prefix)) {
			try {
				return JSONUtil.parseArray(trimmed)
					.stream()
					.map(String::valueOf)
					.filter(CharSequenceUtil::isNotBlank)
					.toList();
			}
			catch (Exception exception) {
				// fall through to CSV parser
				log.warn(exception.getMessage(), exception);
			}
		}
		return Arrays.stream(trimmed.split(","))
			.map(CharSequenceUtil::trim)
			.filter(CharSequenceUtil::isNotBlank)
			.toList();
	}

	/**
	 * 选择候选范围
	 * @param group 范围组
	 * @return 候选范围
	 */
	private static List<DataScopeProjection> selectCandidates(List<DataScopeProjection> group) {
		List<DataScopeProjection> users = group.stream()
			.filter(p -> p != null && "USER".equalsIgnoreCase(p.getSubjectType()))
			.toList();
		if (CollUtil.isNotEmpty(users)) {
			return users;
		}
		return group.stream().filter(Objects::nonNull).toList();
	}

	/**
	 * 按维度分组
	 * @param projections 原始授权片段
	 * @return 分组结果
	 */
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
