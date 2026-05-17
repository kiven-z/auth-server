package com.auth.service.system.common.exception;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.common.exception.code.SystemResultCode;

import lombok.experimental.UtilityClass;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * 将 {@link SystemBusinessException} /
 * {@link com.auth.service.system.common.exception.code.SystemResultCode} 转为统一
 * {@link Result}（与 {@code AuthResultResponseFactory} 轮廓一致：MessageSource 文案 + ext i18n）
 *
 * @author Bunny
 */
@UtilityClass
public class SystemResultResponseFactory {

	/**
	 * 构建系统服务错误响应体（业务异常）
	 * @param messageSource 消息源
	 * @param exception 业务异常
	 * @return Result
	 */
	public static Result<Object> build(MessageSource messageSource, SystemBusinessException exception) {
		Objects.requireNonNull(messageSource, "messageSource");
		Objects.requireNonNull(exception, "exception");
		SystemResultCode resultCode = Objects.requireNonNull(exception.getResultCode(), "resultCode");
		Object[] args = exception.getMessageArgs() != null ? exception.getMessageArgs() : new Object[0];
		return buildBody(messageSource, resultCode, args);
	}

	/**
	 * 由枚举与占位参数直接构建错误体（技术异常等场景）
	 * @param messageSource 消息源
	 * @param rc 结果码枚举
	 * @param args i18n 参数
	 * @return Result
	 */
	public static Result<Object> build(MessageSource messageSource, SystemResultCode rc, Object... args) {
		Objects.requireNonNull(messageSource, "messageSource");
		Objects.requireNonNull(rc, "rc");
		Object[] safeArgs = args != null ? args : new Object[0];
		return buildBody(messageSource, rc, safeArgs);
	}

	/**
	 * 公共拼装：当前线程 Locale + 两段式 MessageSource + Result.error + ext
	 * @param messageSource 消息源
	 * @param resultCode 结果码
	 * @param args i18n 参数（已非 null）
	 * @return Result
	 */
	private static Result<Object> buildBody(MessageSource messageSource, SystemResultCode resultCode, Object[] args) {
		Locale locale = LocaleContextHolder.getLocale();
		String messageKey = Objects.requireNonNull(resultCode.getMessageKey(), "messageKey");
		String defaultEnglish = messageSource.getMessage(messageKey, args, messageKey, Locale.ENGLISH);
		String message = messageSource.getMessage(messageKey, args, defaultEnglish, locale);

		Result<Object> body = Result.error(resultCode.getBizCode(), resultCode.getError(), message);
		body.setExt(Map.of("i18nKey", messageKey, "i18nArgs", args));
		return body;
	}

	/**
	 * 将枚举上的 HTTP 状态解析为 {@link HttpStatus}；无法解析时回退为 400
	 * @param resultCode 结果码
	 * @return HTTP 状态
	 */
	public static HttpStatus resolveHttpStatus(SystemResultCode resultCode) {
		Objects.requireNonNull(resultCode, "resultCode");
		HttpStatus resolved = HttpStatus.resolve(resultCode.getHttpStatus());
		if (resolved != null) {
			return resolved;
		}
		return HttpStatus.BAD_REQUEST;
	}

}
