package com.auth.service.system.admin.exception;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 系统管理域结果码（岗位、导出、授权失效 Outbox 等 Admin 特有场景）
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
