package com.auth.service.system.admin.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 权限类型枚举
 *
 * @author Bunny
 */
@Getter
@AllArgsConstructor
public enum PermissionTypeEnum {

	/**
	 * 按钮权限
	 */
	BUTTON(0, "按钮权限"),

	/**
	 * API权限
	 */
	API(1, "API权限"),;

	private final Integer code;

	private final String desc;

	public static String getDesc(Integer code) {
		for (PermissionTypeEnum value : values()) {
			if (value.code.equals(code)) {
				return value.desc;
			}
		}
		return null;
	}

}
