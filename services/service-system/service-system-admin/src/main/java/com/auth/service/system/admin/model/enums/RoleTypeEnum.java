package com.auth.service.system.admin.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 角色类型枚举，与表 {@code sys_role.role_type} 注释一致
 *
 * @author Bunny
 */
@Getter
@AllArgsConstructor
public enum RoleTypeEnum {

	/**
	 * 系统角色
	 */
	SYSTEM(1, "系统角色"),

	/**
	 * 菜单角色
	 */
	MENU(2, "菜单角色"),

	/**
	 * 业务角色
	 */
	BUSINESS(3, "业务角色"),

	/**
	 * 自定义角色
	 */
	CUSTOM(4, "自定义角色");

	/**
	 * 组织类 grant_table 主体（部门、岗位等）允许绑定的角色类型（业务、自定义）
	 */
	public static final List<Integer> ORG_GRANT_TYPES = List.of(BUSINESS.code, CUSTOM.code);

	/**
	 * 用户直连 grant_table 主体允许绑定的角色类型（业务、自定义）
	 */
	public static final List<Integer> USER_GRANT_TYPES = List.of(BUSINESS.code, CUSTOM.code);

	/**
	 * 菜单-角色关联允许的角色类型（菜单角色）
	 */
	public static final List<Integer> MENU_ASSIGN_TYPES = List.of(MENU.code);

	private final Integer code;

	private final String desc;

	/**
	 * 根据编码获取描述
	 * @param code 类型编码
	 * @return 描述，未找到时返回 null
	 */
	public static String getDesc(Integer code) {
		for (RoleTypeEnum value : RoleTypeEnum.values()) {
			if (value.code.equals(code)) {
				return value.desc;
			}
		}
		return null;
	}

}
