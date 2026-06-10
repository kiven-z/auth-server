package com.auth.service.system.admin.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户组织关系状态，与 {@code user_dept.status}、{@code user_post.status} 一致
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

	/**
	 * 授权计算等场景认定的有效关联状态
	 */
	public static final int ACTIVE_NORMAL = NORMAL.code;

	public static final int ACTIVE_PART_TIME = PART_TIME.code;

	private final int code;

	private final String desc;

	/**
	 * 是否参与授权与组织引用统计
	 * @param status 状态码
	 * @return 有效时为 true
	 */
	public static boolean isActive(Integer status) {
		return status != null && (status == NORMAL.code || status == PART_TIME.code);
	}

	/**
	 * 是否允许设为主部门/主岗位
	 * @param status 状态码
	 * @return 仅正常状态允许
	 */
	public static boolean canBePrimary(Integer status) {
		return status != null && status == NORMAL.code;
	}

	/**
	 * 解析状态码
	 * @param code 状态码
	 * @return 枚举，非法时返回 null
	 */
	public static UserOrgRelationStatusEnum fromCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (UserOrgRelationStatusEnum value : values()) {
			if (value.code == code) {
				return value;
			}
		}
		return null;
	}

}
