package com.auth.service.system.admin.exception;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 系统管理域结果码（岗位、导出等 Admin 特有场景）
 *
 * <p>
 * 号段：268、276、295–305
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
	 * Excel 导出页码区间无效（结束页小于起始页）
	 */
	EXPORT_PAGE_RANGE_INVALID(400, 295, "EXPORT_PAGE_RANGE_INVALID", "system.export.page_range_invalid"),

	/**
	 * Excel 导出超过最大行数
	 */
	EXPORT_ROW_LIMIT_EXCEEDED(400, 296, "EXPORT_ROW_LIMIT_EXCEEDED", "system.export.row_limit_exceeded"),

	/**
	 * Excel 导出单页条数非法
	 */
	EXPORT_PAGE_SIZE_INVALID(400, 297, "EXPORT_PAGE_SIZE_INVALID", "system.export.page_size_invalid"),

	/**
	 * 导出范围不支持或参数不完整（含 FULL_ASYNC 预留）
	 */
	EXPORT_SCOPE_NOT_SUPPORTED(400, 298, "EXPORT_SCOPE_NOT_SUPPORTED", "system.export.scope_not_supported"),

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
	 * 用户部门关联状态非法
	 */
	USER_DEPT_RELATION_STATUS_INVALID(400, 323, "USER_DEPT_RELATION_STATUS_INVALID",
			"system.user.dept_relation_status_invalid"),

	/**
	 * 仅正常状态的部门关联可设为主部门
	 */
	USER_DEPT_PRIMARY_NOT_ELIGIBLE(400, 324, "USER_DEPT_PRIMARY_NOT_ELIGIBLE",
			"system.user.dept_primary_not_eligible"),;

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
