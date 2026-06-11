package com.auth.service.system.admin.exception;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 系统管理域结果码（岗位、用户部门等 Admin 特有场景）
 *
 * <p>
 * 号段：268、276、315–324
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
	 * 同一部门下岗位编码已存在
	 */
	POST_CODE_DUPLICATE_IN_DEPT(409, 276, "POST_CODE_DUPLICATE_IN_DEPT", "system.post.code_duplicate_in_dept"),

	/**
	 * 目标用户不允许执行该写操作（内置超级管理员或当前登录账号）
	 */
	USER_OPERATION_FORBIDDEN(409, 315, "USER_OPERATION_FORBIDDEN", "system.user.operation_forbidden"),

	/**
	 * 用户状态码非法
	 */
	USER_STATUS_INVALID(400, 319, "USER_STATUS_INVALID", "system.user.status_invalid"),

	/**
	 * 两次输入的密码不一致
	 */
	PASSWORD_CONFIRM_MISMATCH(422, 320, "PASSWORD_CONFIRM_MISMATCH", "system.user.password_confirm_mismatch"),

	/**
	 * 旧密码不正确
	 */
	OLD_PASSWORD_INCORRECT(422, 321, "OLD_PASSWORD_INCORRECT", "system.user.old_password_incorrect"),

	/**
	 * 用户部门关联已存在
	 */
	USER_DEPT_DUPLICATE(409, 322, "USER_DEPT_DUPLICATE", "system.user.dept_duplicate"),

	/**
	 * 仅正常状态的部门关联可设为主部门
	 */
	USER_DEPT_PRIMARY_NOT_ELIGIBLE(400, 324, "USER_DEPT_PRIMARY_NOT_ELIGIBLE", "system.user.dept_primary_not_eligible"),

	/**
	 * 用户岗位关联已存在
	 */
	USER_POST_DUPLICATE(409, 325, "USER_POST_DUPLICATE", "system.user.post_duplicate"),

	/**
	 * 仅正常状态的岗位关联可设为主岗位
	 */
	USER_POST_PRIMARY_NOT_ELIGIBLE(400, 326, "USER_POST_PRIMARY_NOT_ELIGIBLE", "system.user.post_primary_not_eligible");

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
