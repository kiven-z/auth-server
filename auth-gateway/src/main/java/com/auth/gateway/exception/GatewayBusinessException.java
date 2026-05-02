package com.auth.gateway.exception;

import com.auth.common.core.i18n.I18nMessageProvider;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

/**
 * 网关业务异常，语义以 {@link GatewayResultCodeEnum} 为唯一来源，可选携带 i18n 占位参数。
 * <p>
 * 实现 {@link I18nMessageProvider}，与业务服务侧 {@code *BusinessException} 契约一致，便于统一组装
 * {@code Result}。
 * </p>
 *
 * @author Bunny
 */
@ToString(of = { "resultCode" })
public class GatewayBusinessException extends RuntimeException implements I18nMessageProvider {

	/**
	 * 网关结果代码（稳定语义：HTTP、业务码、error、i18nKey、默认英文文案）
	 */
	@Getter
	private final GatewayResultCodeEnum resultCode;

	/**
	 * 国际化格式化参数（与 {@link I18nMessageProvider#getMessageArgs()} 为同一引用，只读消费）
	 */
	private final transient Object[] messageArgs;

	public GatewayBusinessException(GatewayResultCodeEnum rc, Object... messageArgs) {
		super(rc != null ? rc.getI18nKey() : "gateway.error");
		if (rc == null) {
			throw new IllegalArgumentException("rc is required");
		}
		this.resultCode = rc;
		this.messageArgs = messageArgs != null ? messageArgs : new Object[0];
	}

	@Override
	public String getMessageKey() {
		return resultCode.getI18nKey();
	}

	@Override
	public Object[] getMessageArgs() {
		return messageArgs;
	}

	/**
	 * 国际化键
	 * @return i18n 键
	 */
	public String getI18nKey() {
		return getMessageKey();
	}

	/**
	 * HTTP 状态码
	 * @return HTTP 状态码
	 */
	public int getHttpStatus() {
		return resultCode.getHttpStatus();
	}

	/**
	 * 完整业务码
	 * @return 业务码
	 */
	public int getBizCode() {
		return resultCode.fullBizCode();
	}

	/**
	 * 稳定错误标识
	 * @return 错误标识
	 */
	public String getError() {
		return resultCode.getError();
	}

	/**
	 * 默认英文消息模板
	 * @return 英文兜底
	 */
	public String getDefaultMessageEn() {
		return resultCode.getDefaultMessageEn();
	}

	@Override
	public String getMessage() {
		return super.getMessage() + (messageArgs.length > 0 ? (" " + Arrays.toString(messageArgs)) : "");
	}

}
