package com.auth.common.data.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户账号状态枚举
 *
 * <p>
 * 与 sys_user 表的 status 字段一一对应
 * </p>
 *
 * @author Bunny
 */
@Getter
@AllArgsConstructor
public enum UserStatus {

	/**
	 * 禁用
	 */
	DISABLED(0, "禁用"),

	/**
	 * 启用
	 */
	NORMAL(1, "启用"),

	/**
	 * 锁定
	 */
	LOCKED(2, "锁定");

	private final int code;

	private final String desc;

	/**
	 * 根据 code 解析枚举
	 * @return 匹配的枚举，不存在则返回 null
	 */
	public static UserStatus of(int code) {
		for (UserStatus v : values()) {
			if (v.code == code) {
				return v;
			}
		}
		return null;
	}

}