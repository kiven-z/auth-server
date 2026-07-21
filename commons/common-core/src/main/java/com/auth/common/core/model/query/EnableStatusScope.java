package com.auth.common.core.model.query;

/**
 * 布尔启停列的查询范围（仅启用 / 不限）
 *
 * @author Bunny
 */
public enum EnableStatusScope {

	/**
	 * 仅启用（status = true）
	 */
	ENABLED,

	/**
	 * 不限启停
	 */
	ALL;

	/**
	 * 转为 Mapper 等值条件：{@link #ENABLED} → true；{@link #ALL} → null（不加 status 条件）
	 * @return status 等值；null 表示不限
	 */
	public Boolean toStatusEquals() {
		return this == ENABLED ? Boolean.TRUE : null;
	}

}
