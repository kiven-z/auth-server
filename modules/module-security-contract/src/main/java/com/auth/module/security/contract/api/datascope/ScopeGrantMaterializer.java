package com.auth.module.security.contract.api.datascope;

import java.util.List;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;

import cn.hutool.core.collection.CollUtil;
import lombok.experimental.UtilityClass;

/**
 * 将存储层 scope_type + 部门 ID 列表物化为运行时 {@link ScopeGrant}
 *
 * @author Bunny
 */
@UtilityClass
public class ScopeGrantMaterializer {

	/**
	 * 物化为运行时授权范围
	 * @param storageType 存储枚举
	 * @param deptIds 部门 ID 字符串列表（DEPT / DEPT_AND_CHILD 使用；后者须已展开）
	 * @return 运行时范围；storageType 无效时返回 {@link ScopeGrantType#SELF}
	 */
	public static ScopeGrant materialize(DataScopeStorageType storageType, List<String> deptIds) {
		if (storageType == null) {
			return selfGrant();
		}
		return switch (storageType) {
			case ALL -> ScopeGrant.builder().scopeType(ScopeGrantType.ALL).build();
			case SELF -> selfGrant();
			case DEPT -> nodeOnlyGrant(deptIds);
			case DEPT_AND_CHILD -> customGrant(deptIds);
		};
	}

	/**
	 * 仅本人
	 * @return 运行时范围
	 */
	private static ScopeGrant selfGrant() {
		return ScopeGrant.builder().scopeType(ScopeGrantType.SELF).build();
	}

	/**
	 * 仅当前节点
	 * @param deptIds 部门 ID 列表
	 * @return 运行时范围
	 */
	private static ScopeGrant nodeOnlyGrant(List<String> deptIds) {
		List<String> values = normalizeDeptIds(deptIds);
		return ScopeGrant.builder().scopeType(ScopeGrantType.NODE_ONLY).values(values).build();
	}

	/**
	 * 自定义集合
	 * @param deptIds 部门 ID 列表
	 * @return 运行时范围
	 */
	private static ScopeGrant customGrant(List<String> deptIds) {
		List<String> values = normalizeDeptIds(deptIds);
		return ScopeGrant.builder().scopeType(ScopeGrantType.CUSTOM).values(values).build();
	}

	/**
	 * 标准化部门 ID 列表
	 * @param deptIds 部门 ID 列表
	 * @return 标准化部门 ID 列表
	 */
	private static List<String> normalizeDeptIds(List<String> deptIds) {
		if (CollUtil.isEmpty(deptIds)) {
			return List.of();
		}
		return List.copyOf(deptIds);
	}

}
