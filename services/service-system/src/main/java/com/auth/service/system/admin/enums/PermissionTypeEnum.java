package com.auth.service.system.admin.enums;

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

	/**
	 * 用户状态枚举
	 *
	 * @author Bunny
	 */
	@Getter
	public enum UserStatusEnums {

		/**
		 * 禁用
		 */
		DISABLED(0, "禁用"),

		/**
		 * 正常
		 */
		NORMAL(1, "正常"),

		/**
		 * 锁定
		 */
		LOCKED(2, "锁定");

		private final int code;

		private final String desc;

		UserStatusEnums(int code, String desc) {
			this.code = code;
			this.desc = desc;
		}

		public static String getDesc(int code) {
			for (UserStatusEnums value : values()) {
				if (value.code == code) {
					return value.desc;
				}
			}
			return null;
		}

	}

}
