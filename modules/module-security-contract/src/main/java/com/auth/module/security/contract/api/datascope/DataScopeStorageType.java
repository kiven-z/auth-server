package com.auth.module.security.contract.api.datascope;

import cn.hutool.core.text.CharSequenceUtil;

import java.util.Locale;

/**
 * 数据范围存储层枚举（role_scope / user_scope.scope_type）
 *
 * @author Bunny
 */
public enum DataScopeStorageType {

	/**
	 * 全部可见
	 */
	ALL,

	/**
	 * 仅本人
	 */
	SELF,

	/**
	 * 指定部门（不含子部门）
	 */
	DEPT,

	/**
	 * 指定部门及全部后代（登录解析时需展开为部门 ID 列表）
	 */
	DEPT_AND_CHILD;

	/**
	 * 解析库表 scope_type 字符串
	 * @param raw 原始值
	 * @return 枚举
	 */
	public static DataScopeStorageType parse(String raw) {
		if (CharSequenceUtil.isBlank(raw)) {
			return null;
		}
		try {
			String upperCase = raw.trim().toUpperCase(Locale.ROOT);
			return valueOf(upperCase);
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

}
