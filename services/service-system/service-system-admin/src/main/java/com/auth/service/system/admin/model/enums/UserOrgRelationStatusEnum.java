package com.auth.service.system.admin.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户组织关系状态
 *
 * @author Bunny
 */
@Getter
@AllArgsConstructor
public enum UserOrgRelationStatusEnum {

	/**
	 * 已调离
	 */
	TRANSFERRED(0, "已调离"),

	/**
	 * 正常
	 */
	NORMAL(1, "正常"),

	/**
	 * 兼职
	 */
	PART_TIME(2, "兼职");

	private final int code;

	private final String desc;

	/**
	 * 是否允许设为主部门/主岗位
	 * @param status 状态码
	 * @return 仅正常状态允许
	 */
	public static boolean canBePrimary(Integer status) {
		return status == null || status != NORMAL.code;
	}

}
