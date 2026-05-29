package com.auth.module.security.datapermission.strategy;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 数据范围 SQL 构建上下文
 *
 * @author Bunny
 */
@Getter
@Builder
public class ScopeSqlBuildContext {

	/**
	 * 当前用户 ID
	 */
	private final Long userId;

	/**
	 * 表别名
	 */
	private final String alias;

	/**
	 * 自身列名
	 */
	private final String userColumn;

	/**
	 * 维度列名
	 */
	private final String dimensionColumn;

	/**
	 * 范围值列表（维度 ID，如部门 ID）
	 */
	private final List<Long> values;

}
