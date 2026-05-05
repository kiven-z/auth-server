package com.auth.module.security.contract.api.authorization;

import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 单个维度的数据范围授权（dept/post/project/...）
 *
 * @author Bunny
 */
@JsonDeserialize(builder = ScopeGrant.ScopeGrantBuilder.class)
@Getter
@Builder
public class ScopeGrant implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 维度模式：树 or 集合
	 */
	private final ScopeMode mode;

	/**
	 * 授权范围类型（按能力定义）
	 */
	private final ScopeGrantType scopeType;

	/**
	 * 原子范围值：例如 deptId / postId / projectId 列表
	 */
	@Builder.Default
	private final List<String> values = Collections.emptyList();

	/**
	 * 树形维度可选：展开后的可见集合（或 path 前缀），避免每次递归 mode!=TREE 时保持空即可
	 */
	@Builder.Default
	private final List<String> expandedValues = Collections.emptyList();

}
