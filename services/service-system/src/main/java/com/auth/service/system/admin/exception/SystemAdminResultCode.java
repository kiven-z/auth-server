package com.auth.service.system.admin.exception;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 系统管理域结果码（角色、权限、岗位）
 *
 * <p>
 * 号段：263、274–277、278–279、288–28
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
	ROLE_NOT_FOUND(404, 263, "ROLE_NOT_FOUND", "data.not.exists"),

	/**
	 * 角色编码已存在
	 */
	ROLE_CODE_DUPLICATE(409, 277, "ROLE_CODE_DUPLICATE", "data.code.duplicate"),

	/**
	 * 权限不存在
	 */
	PERMISSION_NOT_FOUND(404, 280, "PERMISSION_NOT_FOUND", "data.not.exists"),

	/**
	 * 权限编码已存在
	 */
	PERMISSION_CODE_DUPLICATE(409, 274, "PERMISSION_CODE_DUPLICATE", "data.code.duplicate"),

	/**
	 * 部门不存在、已停用或已删除，不可挂载岗位
	 */
	POST_DEPT_NOT_FOUND(404, 275, "POST_DEPT_NOT_FOUND", "data.unavailable"),

	/**
	 * 同一部门下岗位编码已存在
	 */
	POST_CODE_DUPLICATE_IN_DEPT(409, 276, "POST_CODE_DUPLICATE_IN_DEPT", "system.post.code_duplicate_in_dept"),

	/**
	 * 岗位不存在或已删除
	 */
	POST_NOT_FOUND(404, 292, "POST_NOT_FOUND", "data.not.exists"),

	/**
	 * 岗位仍被 user_post 或 grant_table 引用，不可删除
	 */
	POST_IN_USE(409, 293, "POST_IN_USE", "data.in_use"),

	/**
	 * 岗位授权角色无效（不存在、未启用或类型不允许）
	 */
	POST_ROLE_INVALID(400, 294, "POST_ROLE_INVALID", "grant.reference_invalid"),

	/**
	 * 角色仍被 grant_table 引用，不可删除
	 */
	ROLE_IN_USE(409, 278, "ROLE_IN_USE", "data.in_use"),

	/**
	 * 存在无效或未启用的权限码
	 */
	ROLE_PERMISSION_INVALID(400, 279, "ROLE_PERMISSION_INVALID", "grant.reference_invalid"),

	/**
	 * 部门授权角色无效（不存在、未启用或类型不允许）
	 */
	DEPT_ROLE_INVALID(400, 290, "DEPT_ROLE_INVALID", "grant.reference_invalid"),

	/**
	 * 部门不存在或已删除
	 */
	DEPT_NOT_FOUND(404, 281, "DEPT_NOT_FOUND", "data.not.exists"),

	/**
	 * 部门编码已存在
	 */
	DEPT_CODE_DUPLICATE(409, 282, "DEPT_CODE_DUPLICATE", "data.code.duplicate"),

	/**
	 * 父部门不存在或不可用
	 */
	DEPT_PARENT_NOT_FOUND(404, 283, "DEPT_PARENT_NOT_FOUND", "tree.parent.unavailable"),

	/**
	 * 存在未删除的子部门
	 */
	DEPT_HAS_CHILDREN(409, 286, "DEPT_HAS_CHILDREN", "tree.has_active_children"),

	/**
	 * 部门或子树仍被用户/授权/岗位引用
	 */
	DEPT_IN_USE(409, 287, "DEPT_IN_USE", "data.in_use"),

	/**
	 * 菜单不存在
	 */
	MENU_NOT_FOUND(404, 288, "MENU_NOT_FOUND", "data.not.exists"),

	/**
	 * 存在未删除的子菜单
	 */
	MENU_HAS_CHILDREN(409, 289, "MENU_HAS_CHILDREN", "tree.has_active_children"),

	/**
	 * 父菜单不存在
	 */
	MENU_PARENT_NOT_FOUND(404, 292, "MENU_PARENT_NOT_FOUND", "tree.parent.unavailable"),

	/**
	 * 菜单分配角色无效（不存在、未启用或类型非菜单角色）
	 */
	MENU_ROLE_INVALID(400, 291, "MENU_ROLE_INVALID", "grant.reference_invalid"),

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
	 * Outbox 当前状态不允许人工重试
	 */
	AUTH_INVALIDATION_OUTBOX_RETRY_NOT_ALLOWED(400, 299, "AUTH_INVALIDATION_OUTBOX_RETRY_NOT_ALLOWED",
			"system.authorization.invalidation.outbox.retry_not_allowed"),

	/**
	 * Outbox 人工重试并发冲突（版本或状态已变更）
	 */
	AUTH_INVALIDATION_OUTBOX_RETRY_CONFLICT(409, 300, "AUTH_INVALIDATION_OUTBOX_RETRY_CONFLICT",
			"system.authorization.invalidation.outbox.retry_conflict"),

	/**
	 * Outbox 仍处于 PROCESSING 且未满足解锁条件
	 */
	AUTH_INVALIDATION_OUTBOX_PROCESSING_LOCKED(409, 301, "AUTH_INVALIDATION_OUTBOX_PROCESSING_LOCKED",
			"system.authorization.invalidation.outbox.processing_locked"),

	/**
	 * Outbox 人工重试投递成功
	 */
	AUTH_INVALIDATION_OUTBOX_RETRY_SUCCESS(200, 302, "AUTH_INVALIDATION_OUTBOX_RETRY_SUCCESS",
			"system.authorization.invalidation.outbox.retry_success"),

	/**
	 * Outbox 人工重试投递失败
	 */
	AUTH_INVALIDATION_OUTBOX_RETRY_FAILED(200, 303, "AUTH_INVALIDATION_OUTBOX_RETRY_FAILED",
			"system.authorization.invalidation.outbox.retry_failed"),

	/**
	 * 幂等事件非 processing 占位，无需释放
	 */
	AUTH_INVALIDATION_EVENT_RELEASE_NOT_PROCESSING(400, 304, "AUTH_INVALIDATION_EVENT_RELEASE_NOT_PROCESSING",
			"system.authorization.invalidation.event.release_not_processing"),

	/**
	 * 幂等事件 processing 占位已释放
	 */
	AUTH_INVALIDATION_EVENT_RELEASE_SUCCESS(200, 305, "AUTH_INVALIDATION_EVENT_RELEASE_SUCCESS",
			"system.authorization.invalidation.event.release_success"),;

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
