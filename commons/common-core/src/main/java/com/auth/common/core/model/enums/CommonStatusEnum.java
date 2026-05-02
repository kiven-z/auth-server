package com.auth.common.core.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 *
 * @author Bunny
 *
 */
@Getter
@AllArgsConstructor
public enum CommonStatusEnum {

	/**
	 *
	 */
	DISABLED(0, "停用"),
	/**
	 *
	 */
	ENABLED(1, "启用"),;

	private final Integer code;

	private final String desc;

	public static String getDesc(Integer code) {
		for (CommonStatusEnum value : values()) {
			if (value.code.equals(code)) {
				return value.desc;
			}
		}
		return null;
	}

}