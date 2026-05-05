package com.auth.module.security.contract.api.granttable;

/**
 * 授权范围类型（按能力定义，而非绑定具体维度语义）
 *
 * @author Bunny
 */
public enum ScopeGrantType {

	/**
	 * 全部可见
	 */
	ALL,

	/**
	 * 仅本人
	 */
	SELF,

	/**
	 * 树形维度：仅当前节点（values 生效）
	 */
	NODE_ONLY,

	/**
	 * 树形维度：当前节点及后代（values 生效）
	 */
	NODE_AND_DESC,

	/**
	 * 自定义集合（values 生效）
	 */
	CUSTOM,

	/**
	 * 自定义策略：委托给 PolicyDecisionManager 决策
	 */
	CUSTOM_POLICY

}
