package com.auth.service.auth.authentication.profile.datascope;

import lombok.Builder;
import lombok.Getter;

/**
 * 数据权限解析轨迹（合并规则命中过程）。
 *
 * @author Bunny
 */
@Getter
@Builder
public class DataScopeResolveTrace {

	/**
	 * 范围键
	 */
	private final String scopeKey;

	/**
	 * 命中来源（USER_OVERRIDE/ROLE_MERGE/SELF_FALLBACK）
	 */
	private final String source;

	/**
	 * 原始片段数
	 */
	private final int totalPieces;

	/**
	 * 参与合并片段数
	 */
	private final int selectedPieces;

	/**
	 * 生效范围类型
	 */
	private final String effectiveScopeType;

	/**
	 * 生效值数量
	 */
	private final int valuesCount;

}
