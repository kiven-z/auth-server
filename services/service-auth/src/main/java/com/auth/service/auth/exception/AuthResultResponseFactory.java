package com.auth.service.auth.exception;

import com.auth.common.core.model.response.Result;
import lombok.experimental.UtilityClass;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * 将 {@link AuthBusinessException} 转为统一 {@link Result}（稳定子码 + MessageSource 文案 + ext i18n
 * 元数据）。
 *
 * @author Bunny
 */
@UtilityClass
public class AuthResultResponseFactory {

	/**
	 * 构建认证服务错误响应体
	 * @param messageSource 消息源
	 * @param exception 业务异常（须含非空 {@link AuthResultCode}）
	 * @return Result
	 */
	public static Result<Object> build(MessageSource messageSource, AuthBusinessException exception) {
		Objects.requireNonNull(messageSource, "messageSource");
		Objects.requireNonNull(exception, "exception");
		AuthResultCode resultCode = Objects.requireNonNull(exception.getResultCode(), "resultCode");

		Locale locale = LocaleContextHolder.getLocale();
		Object[] args = exception.getMessageArgs() != null ? exception.getMessageArgs() : new Object[0];
		// 与枚举单一事实来源对齐，避免 I18nMessageProvider 与 resultCode 漂移
		String messageKey = Objects.requireNonNull(resultCode.getMessageKey(), "messageKey");
		// 英文兜底：仅用 MessageSource，避免依赖 SpringContextHolder（便于单测与无上下文场景）
		String defaultEnglish = messageSource.getMessage(messageKey, args, messageKey, Locale.ENGLISH);
		String message = messageSource.getMessage(messageKey, args, defaultEnglish, locale);

		Result<Object> body = Result.error(resultCode.getCode(), resultCode.getError(), message);
		body.setExt(Map.of("i18nKey", messageKey, "i18nArgs", args));
		return body;
	}

	/**
	 * 由 {@link AuthResultCode} 解析语义化 HTTP 状态（与历史 {@code AuthExceptionHandler} 映射一致）。
	 * @param resultCode 结果码
	 * @return HTTP 状态
	 */
	public static HttpStatus resolveHttpStatus(AuthResultCode resultCode) {
		Objects.requireNonNull(resultCode, "resultCode");
		return switch (resultCode) {
			// 429 Too Many Requests
			case TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS;

			// 403 Forbidden
			case USER_LOCKED, FAIL_NO_ACCESS_DENIED, USER_LOCKED_WITH_MINUTES, USER_DISABLED,
					MAX_SESSION_LIMIT_EXCEEDED ->
				HttpStatus.FORBIDDEN;

			// 401 Unauthorized
			case LOGIN_AUTH, TOKEN_IS_NULL, TOKEN_PARSING_FAILED, REFRESH_TOKEN_MISSING, REFRESH_TOKEN_EXPIRED,
					REFRESH_TOKEN_MALFORMED, USER_NOT_FOUND, AUTH_CODE_ERROR, USERNAME_OR_PASSWORD_ERROR,
					USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, SESSION_KICKED ->
				HttpStatus.UNAUTHORIZED;

			// 400 Bad Request
			case JSON_PARSER_EXCEPTION, BAD_REQUEST_MISSING_EMAIL_EXCEPTION, AUTH_TYPE_UNSUPPORTED,
					AUTH_REQUEST_TYPE_UNSUPPORTED ->
				HttpStatus.BAD_REQUEST;

			// 500 Internal Server Error
			case DATABASE_ERROR, REDIS_UNAVAILABLE, SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;

			// 503 Service Unavailable
			case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;

			default -> HttpStatus.BAD_REQUEST;
		};
	}

}
