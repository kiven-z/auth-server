package com.auth.service.system.common.exception.code.admin;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 系统管理域结果码（角色、菜单、权限、岗位、用户档案）
 *
 * <p>
 * 号段：263–278
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemAdminResultCode implements SystemResultCode {

	/**
	 * 角色不存在
	 */
	ROLE_NOT_FOUND(404, 263, "ROLE_NOT_FOUND", "system.role.not_found"),
	/**
	 * 角色编码已存在
	 */
	ROLE_CODE_DUPLICATE(409, 264, "ROLE_CODE_DUPLICATE", "system.role.code_duplicate"),
	/**
	 * 系统角色不可删除
	 */
	ROLE_SYSTEM_CANNOT_DELETE(400, 265, "ROLE_SYSTEM_CANNOT_DELETE", "system.role.system_cannot_delete"),
	/**
	 * 系统角色不可禁用
	 */
	ROLE_SYSTEM_CANNOT_DISABLE(400, 266, "ROLE_SYSTEM_CANNOT_DISABLE", "system.role.system_cannot_disable"),
	/**
	 * 系统角色不可修改类型
	 */
	ROLE_SYSTEM_CANNOT_CHANGE_TYPE(400, 267, "ROLE_SYSTEM_CANNOT_CHANGE_TYPE", "system.role.system_cannot_change_type"),
	/**
	 * 角色已被引用，不可删除
	 */
	ROLE_IN_USE(409, 268, "ROLE_IN_USE", "system.role.in_use"),
	/**
	 * 用户已存在
	 */
	USER_ALREADY_EXISTS(409, 269, "USER_ALREADY_EXISTS", "user.already.exists"),
	/**
	 * 菜单不存在
	 */
	MENU_NOT_FOUND(404, 270, "MENU_NOT_FOUND", "system.menu.not_found"),
	/**
	 * 角色编码在系统中不存在
	 */
	MENU_ROLE_CODE_INVALID(422, 271, "MENU_ROLE_CODE_INVALID", "system.menu.role_code_invalid"),
	/**
	 * 权限编码在系统中不存在
	 */
	MENU_PERMISSION_CODE_INVALID(422, 272, "MENU_PERMISSION_CODE_INVALID", "system.menu.permission_code_invalid"),
	/**
	 * 权限不存在
	 */
	PERMISSION_NOT_FOUND(404, 273, "PERMISSION_NOT_FOUND", "system.permission.not_found"),
	/**
	 * 权限编码已存在
	 */
	PERMISSION_CODE_DUPLICATE(409, 274, "PERMISSION_CODE_DUPLICATE", "system.permission.code_duplicate"),
	/**
	 * 部门不存在、已停用或已删除，不可挂载岗位
	 */
	POST_DEPT_NOT_FOUND(404, 275, "POST_DEPT_NOT_FOUND", "system.post.dept_not_found"),
	/**
	 * 同一部门下岗位编码已存在
	 */
	POST_CODE_DUPLICATE_IN_DEPT(409, 276, "POST_CODE_DUPLICATE_IN_DEPT", "system.post.code_duplicate_in_dept"),
	/**
	 * 权限编码格式不符合约定
	 */
	PERMISSION_CODE_FORMAT_INVALID(422, 277, "PERMISSION_CODE_FORMAT_INVALID", "system.permission.code_format_invalid"),
	/**
	 * 角色编码格式不符合约定
	 */
	ROLE_CODE_FORMAT_INVALID(422, 278, "ROLE_CODE_FORMAT_INVALID", "system.role.code_format_invalid");

	private final int httpStatus;

	private final int code;

	private final String error;

	private final String messageKey;

	SystemAdminResultCode(int httpStatus, int code, String error, String messageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

}
