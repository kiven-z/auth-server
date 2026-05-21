package com.auth.service.system.admin.exception;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 系统管理域结果码（角色、权限、岗位）
 *
 * <p>
 * 号段：263、274–277（265–273、278 已释放）
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemAdminResultCode implements SystemResultCode {

	/**
	 * 分配完成
	 */
	ASSIGN_COMPLETED(200, 268, "ASSIGN_COMPLETED", "system.admin.assign_completed"),

	/**
	 * 角色不存在
	 */
	ROLE_NOT_FOUND(404, 263, "ROLE_NOT_FOUND", "system.role.not_found"),

	/**
	 * 角色编码已存在
	 */
	ROLE_CODE_DUPLICATE(409, 277, "ROLE_CODE_DUPLICATE", "system.role.code_duplicate"),

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
	POST_CODE_DUPLICATE_IN_DEPT(409, 276, "POST_CODE_DUPLICATE_IN_DEPT", "system.post.code_duplicate_in_dept"),;

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
