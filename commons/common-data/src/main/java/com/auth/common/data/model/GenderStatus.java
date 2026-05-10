package com.auth.common.data.model;

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

	public static String getDesc(int code) {
		for (GenderStatus value : values()) {
			if (value.code == code) {
				return value.desc;
			}
		}
		return null;
	}

}
