package com.auth.service.system.common.exception;

import com.auth.common.core.i18n.I18nMessageProvider;
import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 系统业务异常：语义以 {@link com.auth.service.system.common.exception.code.SystemResultCode}
 * 为唯一来源，可选携带 i18n 占位参数（与 {@code AuthBusinessException} 同构）
 * <p>
 * 对外 API 的 {@code message} 由 {@link SystemResultResponseFactory} 结合
 * {@link org.springframework.context.MessageSource} 解析；{@link #getMessage()} 用于日志/调试
 * </p>
 *
 * @author Bunny
 */
public class SystemBusinessException extends RuntimeException implements I18nMessageProvider {

	@Getter
	private final transient SystemResultCode resultCode;

	private final transient Object[] args;

	public SystemBusinessException(SystemResultCode resultCode, Object... args) {
		super(resultCode != null ? resultCode.getMessageKey() : null);
		this.resultCode = Objects.requireNonNull(resultCode, "resultCode");
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
			return resultCode.getMessageKey() + (args.length > 0 ? (" " + Arrays.toString(args)) : "");
		}
	}

	@Override
	public String getMessageKey() {
		return resultCode != null ? resultCode.getMessageKey() : null;
	}

	/**
	 * i18n 占位参数（与 {@link I18nMessageProvider#getMessageArgs()} 一致）
	 * @return 参数数组
	 */
	@Override
	public Object[] getMessageArgs() {
		return args;
	}

}
