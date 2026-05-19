package com.auth.service.system.common.exception;

import com.auth.common.core.i18n.I18nMessageProvider;
import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 消息发送域异常（传输层 / 渠道基础设施）：语义以 {@link SystemResultCode} 为唯一来源。
 * <p>
 * 与 {@link SystemBusinessException} 同构，由 {@link SystemBusinessExceptionHandler} 统一处理；
 * 便于日后将 message 模块拆为独立微服务时整体迁移。
 * </p>
 *
 * @author Bunny
 */
public class SystemMessageException extends RuntimeException implements I18nMessageProvider {

	@Getter
	private final transient SystemResultCode resultCode;

	private final transient Object[] args;

	/**
	 * @param resultCode 消息域结果码（见 {@code code.message} 包）
	 * @param args i18n 占位参数
	 */
	public SystemMessageException(SystemResultCode resultCode, Object... args) {
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
	 * i18n 占位参数
	 * @return 参数数组
	 */
	@Override
	public Object[] getMessageArgs() {
		return args;
	}

}
