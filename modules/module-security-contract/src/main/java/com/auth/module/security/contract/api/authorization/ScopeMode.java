package com.auth.module.security.contract.api.authorization;

/**
 * 数据范围维度模式：树形 or 集合
 *
 * @author Bunny
 */
public enum ScopeMode {

	/**
	 * 树形维度（如部门）
	 */
	TREE,

	/**
	 * 集合维度（如岗位/项目组）
	 */
	SET

}
