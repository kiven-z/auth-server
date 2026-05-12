package com.auth.service.auth.exception;

import com.auth.common.core.i18n.I18nMessageProvider;
import com.auth.service.auth.model.enums.AuthResultCode;
import lombok.Getter;

import java.util.Arrays;

/**
 * 认证服务业务异常：语义以 {@link AuthResultCode} 为唯一来源，可选携带 i18n 占位参数。
 * <p>
 * {@link #getMessage()} 可用于日志/调试；对外 API 文案由 {@link AuthBusinessExceptionResultBuilder} 结合
 * {@link org.springframework.context.MessageSource} 解析。
 * </p>
 *
 * @author Bunny
 */
@Getter
public class AuthBusinessException extends RuntimeException implements I18nMessageProvider {

	private final AuthResultCode resultCode;

	private final transient Object[] args;

	public AuthBusinessException(AuthResultCode resultCode, Object... args) {
		super(resultCode != null ? resultCode.getMessageKey() : null);
		this.resultCode = resultCode;
		this.args = args != null ? args : new Object[0];
	}

	@Override
	public String getMessage() {
		if (resultCode == null) {
			return super.getMessage();
		}
		try {
			return resultCode.getMessage(args);
		}
		catch (Exception ignored) {
			// If i18n resolution fails, fall back to key + args to keep it debuggable.
			return resultCode.getMessageKey() + (args.length > 0 ? (" " + Arrays.toString(args)) : "");
		}
	}

	@Override
	public String getMessageKey() {
		return resultCode != null ? resultCode.getMessageKey() : null;
	}

	@Override
	public Object[] getMessageArgs() {
		return args;
	}

}
