package com.auth.common.data.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户性别标识
 *
 * @author Bunny
 */
@Getter
@AllArgsConstructor
public enum GenderStatus {

	/**
	 * 未知
	 */
	UNKNOWN(0, "未知"),

	/**
	 * 男
	 */
	MALE(1, "男"),

	/**
	 * 女
	 */
	FEMALE(2, "女"),;

	private final int code;

	private final String desc;

	/**
	 * 根据 code 解析枚举
	 * @param code 性别码
	 * @return 匹配的枚举，不存在则返回 null
	 */
	public static GenderStatus of(int code) {
		for (GenderStatus value : values()) {
			if (value.code == code) {
				return value;
			}
		}
		return null;
	}

}
