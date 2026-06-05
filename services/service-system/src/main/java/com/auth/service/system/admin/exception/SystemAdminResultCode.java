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
	ROLE_NOT_FOUND(404, 263, "ROLE_NOT_FOUND", "system.role.not_found"),

	/**
	 * 角色编码已存在
	 */
	ROLE_CODE_DUPLICATE(409, 277, "ROLE_CODE_DUPLICATE", "system.role.code_duplicate"),

	/**
	 * 权限不存在
	 */
	PERMISSION_NOT_FOUND(404, 280, "PERMISSION_NOT_FOUND", "system.permission.not_found"),

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
	 * 岗位不存在或已删除
	 */
	POST_NOT_FOUND(404, 292, "POST_NOT_FOUND", "system.post.not_found"),

	/**
	 * 岗位仍被 user_post 或 grant_table 引用，不可删除
	 */
	POST_IN_USE(409, 293, "POST_IN_USE", "system.post.in_use"),

	/**
	 * 岗位授权角色无效（不存在、未启用或类型不允许）
	 */
	POST_ROLE_INVALID(400, 294, "POST_ROLE_INVALID", "system.post.role_invalid"),

	/**
	 * 角色仍被 grant_table 引用，不可删除
	 */
	ROLE_IN_USE(409, 278, "ROLE_IN_USE", "system.role.in_use"),

	/**
	 * 存在无效或未启用的权限码
	 */
	ROLE_PERMISSION_INVALID(400, 279, "ROLE_PERMISSION_INVALID", "system.role.permission_invalid"),

	/**
	 * 部门授权角色无效（不存在、未启用或类型不允许）
	 */
	DEPT_ROLE_INVALID(400, 290, "DEPT_ROLE_INVALID", "system.dept.role_invalid"),

	/**
	 * 部门不存在或已删除
	 */
	DEPT_NOT_FOUND(404, 281, "DEPT_NOT_FOUND", "system.dept.not_found"),

	/**
	 * 部门编码已存在
	 */
	DEPT_CODE_DUPLICATE(409, 282, "DEPT_CODE_DUPLICATE", "system.dept.code_duplicate"),

	/**
	 * 父部门不存在或不可用
	 */
	DEPT_PARENT_NOT_FOUND(404, 283, "DEPT_PARENT_NOT_FOUND", "system.dept.parent_not_found"),

	/**
	 * 不能将部门移动到自身下
	 */
	DEPT_MOVE_TO_SELF(400, 284, "DEPT_MOVE_TO_SELF", "system.dept.move_to_self"),

	/**
	 * 不能将部门移动到其子部门下
	 */
	DEPT_MOVE_TO_DESCENDANT(400, 285, "DEPT_MOVE_TO_DESCENDANT", "system.dept.move_to_descendant"),

	/**
	 * 存在未删除的子部门
	 */
	DEPT_HAS_CHILDREN(409, 286, "DEPT_HAS_CHILDREN", "system.dept.has_children"),

	/**
	 * 部门或子树仍被用户/授权/岗位引用
	 */
	DEPT_IN_USE(409, 287, "DEPT_IN_USE", "system.dept.in_use"),

	/**
	 * 菜单不存在
	 */
	MENU_NOT_FOUND(404, 288, "MENU_NOT_FOUND", "system.menu.not_found"),

	/**
	 * 存在未删除的子菜单
	 */
	MENU_HAS_CHILDREN(409, 289, "MENU_HAS_CHILDREN", "system.menu.has_children"),

	/**
	 * 菜单分配角色无效（不存在、未启用或类型非菜单角色）
	 */
	MENU_ROLE_INVALID(400, 291, "MENU_ROLE_INVALID", "system.menu.role_invalid"),

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
	EXPORT_SCOPE_NOT_SUPPORTED(400, 298, "EXPORT_SCOPE_NOT_SUPPORTED", "system.export.scope_not_supported"),;

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
