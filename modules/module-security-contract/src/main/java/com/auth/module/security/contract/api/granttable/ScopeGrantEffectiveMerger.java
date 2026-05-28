package com.auth.module.security.contract.api.granttable;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import lombok.experimental.UtilityClass;

import java.util.*;

/**
 * 将多条 {@link ScopeGrant} 宽松合并为一条，供画像构建或默认 SQL 条件生成使用。
 *
 * @author Bunny
 */
@UtilityClass
public class ScopeGrantEffectiveMerger {

	/**
	 * 宽松合并：ALL 优先；再合并 NODE_ONLY/NODE_AND_DESC/CUSTOM 的取值列表为 CUSTOM；否则若存在 SELF 则返回 SELF。
	 * @param grants 授权片段集合
	 * @return 合并结果；无可合并项时返回 {@code null}
	 */
	public static ScopeGrant mergeLoose(Collection<ScopeGrant> grants) {
		if (CollUtil.isEmpty(grants)) {
			return null;
		}
		boolean anyAll = false;
		boolean anySelf = false;
		boolean restrictiveWithoutIds = false;
		LinkedHashSet<String> unionIds = new LinkedHashSet<>();
		for (ScopeGrant grant : grants) {
			if (grant == null || grant.getScopeType() == null) {
				continue;
			}

			// 授权范围
			switch (grant.getScopeType()) {
				case ALL -> anyAll = true;
				// 只能看自己
				case SELF -> anySelf = true;
				case NODE_ONLY, NODE_AND_DESC, CUSTOM -> {
					List<String> vals = Optional.ofNullable(grant.getValues()).orElse(List.of());
					if (CollUtil.isEmpty(vals)) {
						restrictiveWithoutIds = true;
					}
					unionIds.addAll(vals);
				}
				default -> anySelf = true;
			}
		}

		if (anyAll) {
			return ScopeGrant.builder().scopeType(ScopeGrantType.ALL).build();
		}
		if (CollUtil.isNotEmpty(unionIds)) {
			return ScopeGrant.builder().scopeType(ScopeGrantType.CUSTOM).values(List.copyOf(unionIds)).build();
		}
		if (restrictiveWithoutIds) {
			return ScopeGrant.builder().scopeType(ScopeGrantType.CUSTOM).values(List.of()).build();
		}
		if (anySelf) {
			return ScopeGrant.builder().scopeType(ScopeGrantType.SELF).build();
		}
		return null;
	}

	/**
	 * 针对「部门/组织列」过滤：仅合并 USER、ROLE、DEPT 三类主体下的范围，避免岗位主体下的 ID 误入部门 IN 条件。
	 * @param dataScopes 画像中的按主体类型分桶范围
	 * @return 合并后的有效范围
	 */
	public static ScopeGrant mergeLooseForDeptColumn(Map<String, ScopeGrant> dataScopes) {
		if (CollUtil.isEmpty(dataScopes)) {
			return null;
		}

		List<ScopeGrant> parts = new ArrayList<>(3);
		// 整合数据权限范围
		for (DataScopeGrantTableSubjectType subjectType : EnumSet.of(DataScopeGrantTableSubjectType.USER,
				DataScopeGrantTableSubjectType.ROLE, DataScopeGrantTableSubjectType.DEPT)) {
			ScopeGrant grant = dataScopes.get(subjectType.name());
			if (grant != null) {
				parts.add(grant);
			}
		}

		return mergeLoose(parts);
	}

}
