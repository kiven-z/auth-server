package com.auth.service.system.common.exception.code.common;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 系统服务通用结果码（成功、请求校验、用户、基础设施等）
 *
 * <p>
 * 号段：0、101–103、199、201–211、301–302、401–405
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemCommonResultCode implements SystemResultCode {

	// 0xx: 成功
	/**
	 * 添加成功
	 */
	ADD_SUCCESS(200, 0, "ADD_SUCCESS", "result.add.success"),
	/**
	 * 更新成功
	 */
	UPDATE_SUCCESS(200, 0, "UPDATE_SUCCESS", "result.update.success"),
	/**
	 * 删除成功
	 */
	DELETE_SUCCESS(200, 0, "DELETE_SUCCESS", "result.delete.success"),
	/**
	 * 分配成功
	 */
	ASSIGN_SUCCESS(200, 0, "ASSIGN_SUCCESS", "result.assign.success"),
	/**
	 * 上传成功
	 */
	SUCCESS_UPLOAD(200, 0, "SUCCESS_UPLOAD", "result.success.upload"),
	/**
	 * 退出成功
	 */
	SUCCESS_LOGOUT(200, 0, "SUCCESS_LOGOUT", "result.success.logout"),

	// 2xx: 客户端/输入
	/**
	 * 请求为空
	 */
	REQUEST_IS_EMPTY(400, 201, "REQUEST_IS_EMPTY", "request.data.empty"),
	/**
	 * 数据不存在
	 */
	DATA_NOT_EXIST(404, 206, "DATA_NOT_EXIST", "data.not.exists"),
	/**
	 * 文件不存在
	 */
	FILE_NOT_EXIST(404, 207, "FILE_NOT_EXIST", "file.not.exists"),
	/**
	 * JSON 解析异常
	 */
	JSON_PARSER_EXCEPTION(400, 202, "JSON_PARSER_EXCEPTION", "json.parser.exception"),
	/**
	 * 转换属性异常
	 */
	CONVERT_PROPERTY_EXCEPTION(400, 203, "CONVERT_PROPERTY_EXCEPTION", "convert.property.exception"),
	/**
	 * 非法请求
	 */
	ILLEGAL_REQUEST(422, 204, "ILLEGAL_REQUEST", "illegal.request"),
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
	 * 新密码不能为空
	 */
	ILLEGAL_NEW_PASSWORD_NOT_NULL(422, 210, "ILLEGAL_NEW_PASSWORD_NOT_NULL", "illegal.new.password.not.null"),
	/**
	 * 非法页面动画
	 */
	ILLEGAL_PAGE_ANIMATION(422, 211, "ILLEGAL_PAGE_ANIMATION", "illegal.page.animation"),

	// 3xx: 业务规则
	/**
	 * 更新异常
	 */
	UPDATE_ERROR(400, 301, "UPDATE_ERROR", "update.error"),
	/**
	 * 重置密码过于频繁
	 */
	RESET_PASSWORD_TOO_FREQUENTLY(429, 302, "RESET_PASSWORD_TOO_FREQUENTLY", "reset.password.too.frequent"),

	// 4xx: 授权/用户
	/**
	 * 无访问权限
	 */
	FAIL_NO_ACCESS_DENIED(403, 401, "FAIL_NO_ACCESS_DENIED", "no.access.denied"),
	/**
	 * 用户不存在
	 */
	USER_NOT_FOUND(404, 402, "USER_NOT_FOUND", "user.not.found"),
	/**
	 * 用户不存在
	 */
	USER_NOT_EXIST(404, 403, "USER_NOT_EXIST", "user.not.exist"),

	// 5xx / 技术
	/**
	 * 数据库错误
	 */
	DATABASE_ERROR(500, 101, "DATABASE_ERROR", "database.error"),
	/**
	 * 服务不可用
	 */
	SERVICE_UNAVAILABLE(503, 102, "SERVICE_UNAVAILABLE", "service.unavailable"),
	/**
	 * 服务器错误
	 */
	SERVER_ERROR(500, 199, "SERVER_ERROR", "server.error"),
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
