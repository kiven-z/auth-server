package com.auth.module.security.contract.api.granttable;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
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
	 * 宽松合并：ALL 优先；存在部门 ID 时取并集，若含任一条 DEPT_AND_CHILD 则结果为 DEPT_AND_CHILD，否则为 DEPT；否则 SELF。
	 * @param grants 授权片段集合
	 * @return 合并结果；无可合并项时返回 {@code null}
	 */
	public static ScopeGrant mergeLoose(Collection<ScopeGrant> grants) {
		if (CollUtil.isEmpty(grants)) {
			return null;
		}
		boolean anyAll = false;
		boolean anySelf = false;
		boolean anyDeptAndChild = false;
		boolean restrictiveWithoutIds = false;
		LinkedHashSet<Long> unionIds = new LinkedHashSet<>();
		for (ScopeGrant grant : grants) {
			if (grant == null || grant.getScopeType() == null) {
				continue;
			}
			switch (grant.getScopeType()) {
				case ALL -> anyAll = true;
				case SELF -> anySelf = true;
				case DEPT -> restrictiveWithoutIds |= mergeDeptValues(grant, unionIds);
				case DEPT_AND_CHILD -> {
					anyDeptAndChild = true;
					restrictiveWithoutIds |= mergeDeptValues(grant, unionIds);
				}
				default -> anySelf = true;
			}
		}

		if (anyAll) {
			return ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).build();
		}
		if (CollUtil.isNotEmpty(unionIds)) {
			DataScopeStorageType mergedType = anyDeptAndChild ? DataScopeStorageType.DEPT_AND_CHILD
					: DataScopeStorageType.DEPT;
			return ScopeGrant.builder().scopeType(mergedType).values(List.copyOf(unionIds)).build();
		}
		if (restrictiveWithoutIds) {
			DataScopeStorageType mergedType = anyDeptAndChild ? DataScopeStorageType.DEPT_AND_CHILD
					: DataScopeStorageType.DEPT;
			return ScopeGrant.builder().scopeType(mergedType).values(List.of()).build();
		}
		if (anySelf) {
			return ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).build();
		}
		return null;
	}

	/**
	 * @return 是否合并了非空部门 ID
	 */
	private static boolean mergeDeptValues(ScopeGrant grant, LinkedHashSet<Long> unionIds) {
		List<Long> vals = grant.getValues();
		if (CollUtil.isEmpty(vals)) {
			return true;
		}
		unionIds.addAll(vals);
		return false;
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
