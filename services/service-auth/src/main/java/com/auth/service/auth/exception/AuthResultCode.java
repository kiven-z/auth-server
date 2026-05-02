package com.auth.service.auth.exception;

import com.auth.common.web.context.SpringContextHolder;
import lombok.Getter;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Objects;

/**
 * 统一返回结果状态信息类
 *
 * @author Bunny
 */
@Getter
public enum AuthResultCode {

	/**
	 * 登录成功
	 */
	LOGIN_SUCCESS(0, "SUCCESS", "result.login.success"),
	/**
	 * 退出登录成功
	 */
	SUCCESS_LOGOUT(0, "SUCCESS", "result.success.logout"),

	/**
	 * 业务规则拒绝：账号密码错误
	 */
	USERNAME_OR_PASSWORD_ERROR(1301, "BAD_CREDENTIALS", "username.or.password.error"),
	/**
	 * 业务规则拒绝：邮箱验证码错误（或验证码错误）
	 */
	AUTH_CODE_ERROR(1302, "EMAIL_CODE_INVALID", "auth.code.error"),
	/**
	 * 认证/授权失败：Access Token 为空
	 */
	TOKEN_IS_NULL(1411, "TOKEN_MISSING", "token.is.null"),

	/**
	 * 参数/格式错误：JSON 解析错误
	 */
	JSON_PARSER_EXCEPTION(1202, "JSON_PARSER_ERROR", "json.parser.exception"),

	/**
	 * 参数/格式错误：email 没有收件人
	 */
	BAD_REQUEST_MISSING_EMAIL_EXCEPTION(1203, "MISSING_EMAIL_RECIPIENT", "bad.request.missing.email.error"),

	/**
	 * 认证/授权失败：请先登陆（会话超时）
	 */
	LOGIN_AUTH(1401, "LOGIN_TIMEOUT", "please.login.first"),
	/**
	 * 认证/授权失败：Token 解析失败
	 */
	TOKEN_PARSING_FAILED(1410, "TOKEN_PARSING_FAILED", "token.parsing.failed"),

	/**
	 * 认证/授权失败：无权访问
	 */
	FAIL_NO_ACCESS_DENIED(1403, "ACCESS_DENIED", "no.access.denied"),
	/**
	 * 业务规则拒绝：账号已锁定
	 */
	USER_LOCKED(1305, "USER_LOCKED", "user.locked"),
	/**
	 * 业务规则拒绝：账号已锁定（带剩余锁定时间，分钟）
	 */
	USER_LOCKED_WITH_MINUTES(1305, "USER_LOCKED", "user.locked.with.minutes"),
	/**
	 * 业务规则拒绝：账号已禁用
	 */
	USER_DISABLED(1306, "USER_DISABLED", "user.disabled"),

	/**
	 * 认证失败：用户不存在（登录时通常以“凭证错误”对外隐藏；该码更多用于内部/管理端）
	 */
	USER_NOT_FOUND(1404, "USER_NOT_FOUND", "user.not.found"),

	/**
	 * 业务规则拒绝：操作频繁
	 */
	TOO_MANY_REQUESTS(1304, "TOO_MANY_REQUESTS", "too.many.requests"),
	/**
	 * 业务规则拒绝：账号密码错误（含剩余尝试次数）
	 */
	USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING(1301, "BAD_CREDENTIALS", "username.or.password.error.with.remaining"),
	/**
	 * 业务规则拒绝：活跃会话数已达上限
	 */
	MAX_SESSION_LIMIT_EXCEEDED(1307, "MAX_SESSION_LIMIT_EXCEEDED", "max.session.limit.exceeded"),
	/**
	 * 认证/授权失败：当前会话被新设备登录挤下线
	 */
	SESSION_KICKED(1424, "SESSION_KICKED", "session.kicked"),

	/**
	 * 参数/格式错误：认证类型不支持
	 */
	AUTH_TYPE_UNSUPPORTED(1303, "AUTH_TYPE_UNSUPPORTED", "auth.type.unsupported"),
	/**
	 * 参数/格式错误：认证请求类型不支持
	 */
	AUTH_REQUEST_TYPE_UNSUPPORTED(1204, "AUTH_REQUEST_TYPE_UNSUPPORTED", "auth.request.type.unsupported"),

	/**
	 * 认证/授权失败：Refresh Token 缺失
	 */
	REFRESH_TOKEN_MISSING(1423, "REFRESH_TOKEN_MISSING", "refresh.token.missing"),
	/**
	 * 认证/授权失败：Refresh Token 已过期/无效/会话异常
	 */
	REFRESH_TOKEN_EXPIRED(1421, "REFRESH_TOKEN_EXPIRED", "refresh.token.expired"),
	/**
	 * 认证/授权失败：Refresh Token 格式错误
	 */
	REFRESH_TOKEN_MALFORMED(1422, "REFRESH_TOKEN_MALFORMED", "refresh.token.malformed"),

	/**
	 * 数据库异常
	 */
	DATABASE_ERROR(1101, "DATABASE_UNAVAILABLE", "database.error"),
	REDIS_UNAVAILABLE(1102, "REDIS_UNAVAILABLE", "redis.unavailable"),
	SERVER_ERROR(1199, "SERVER_ERROR", "server.error"),

	/**
	 * 服务不可用 503 服务不可用
	 */
	SERVICE_UNAVAILABLE(1103, "SERVICE_UNAVAILABLE", "service.unavailable"),;

	/**
	 * 业务码（5位规则在文档中，存 int 以便传输/比较）
	 */
	private final Integer code;

	/**
	 * 稳定错误标识符（用于 Result.error/subCode）
	 */
	private final String error;

	/**
	 * i18n 消息键
	 */
	private final String messageKey;

	AuthResultCode(Integer code, String error, String messageKey) {
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

	/**
	 * 获取国际化消息
	 * @param locale 语言环境
	 * @param args 消息参数
	 * @return 国际化后的消息
	 */
	public String getMessage(Locale locale, Object... args) {
		Locale finalLocale = Objects.requireNonNullElse(locale, LocaleContextHolder.getLocale());
		return SpringContextHolder.getMessage(messageKey, args, finalLocale);
	}

	/**
	 * 获取默认语言环境的国际化消息
	 * @param args 消息参数
	 * @return 国际化后的消息
	 */
	public String getMessage(Object... args) {
		Locale locale = LocaleContextHolder.getLocale();
		return getMessage(locale, args);
	}

}
