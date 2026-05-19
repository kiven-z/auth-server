package com.auth.service.system.common.exception.code.common;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 系统服务通用结果码（成功、请求校验、用户、基础设施等）
 *
 * <p>
 * 号段：0、102–103、205–209、402、206
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemCommonResultCode implements SystemResultCode {

	/**
	 * 操作成功
	 */
	OPERATION_SUCCESS(200, 0, "OPERATION_SUCCESS", "result.operation.success"),

	/**
	 * 添加成功
	 */
	ADD_SUCCESS(200, 265, "ADD_SUCCESS", "system.admin.add_success"),

	/**
	 * 删除成功
	 */
	DELETE_SUCCESS(200, 266, "DELETE_SUCCESS", "system.admin.delete_success"),

	/**
	 * 修改成功
	 */
	MODIFY_SUCCESS(200, 267, "MODIFY_SUCCESS", "system.admin.modify_success"),

	/**
	 * 数据不存在
	 */
	DATA_NOT_EXIST(404, 206, "DATA_NOT_EXIST", "data.not.exists"),

	/**
	 * 验证码错误
	 */
	VERIFY_CODE_ERROR(422, 205, "VERIFY_CODE_ERROR", "verify.code.error"),

	/**
	 * 用户邮箱未绑定
	 */
	USER_EMAIL_NOT_BIND(422, 208, "USER_EMAIL_NOT_BIND", "user.email.not.bind"),

	/**
	 * 用户 ID 不能为空
	 */
	ILLEGAL_USER_ID_NOT_NULL(422, 209, "ILLEGAL_USER_ID_NOT_NULL", "illegal.user.id.not.null"),

	/**
	 * 用户不存在
	 */
	USER_NOT_FOUND(404, 402, "USER_NOT_FOUND", "user.not.found"),

	/**
	 * 服务不可用
	 */
	SERVICE_UNAVAILABLE(503, 102, "SERVICE_UNAVAILABLE", "service.unavailable"),

	/**
	 * JWT 无效/过期
	 */
	JWT_INVALID(401, 405, "JWT_INVALID", "system.jwt.invalid"),

	/**
	 * Redis 基础设施错误
	 */
	REDIS_SYSTEM_ERROR(503, 103, "REDIS_SYSTEM_ERROR", "system.redis.unavailable");

	private final int httpStatus;

	private final int code;

	private final String error;

	private final String messageKey;

	SystemCommonResultCode(int httpStatus, int code, String error, String messageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

}
