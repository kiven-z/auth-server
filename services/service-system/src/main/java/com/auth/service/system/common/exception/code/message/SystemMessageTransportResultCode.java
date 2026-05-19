package com.auth.service.system.common.exception.code.message;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 消息传输层结果代码（英文唯一API消息，无i18n键）
 *
 * <p>
 * 段: 297–299
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemMessageTransportResultCode implements SystemResultCode {

	/**
	 * 传输验证失败（空接收者、空主题等）
	 */
	MESSAGE_TRANSPORT_VALIDATION_FAILED(400, 297, "MESSAGE_TRANSPORT_VALIDATION_FAILED", null),
	/**
	 * 传输发送失败（MIME、钉钉、阿里云Dypns等）
	 */
	MESSAGE_TRANSPORT_SEND_FAILED(502, 298, "MESSAGE_TRANSPORT_SEND_FAILED", null),
	/**
	 * 传输通道不可用
	 */
	MESSAGE_TRANSPORT_UNAVAILABLE(503, 299, "MESSAGE_TRANSPORT_UNAVAILABLE", null);

	private final int httpStatus;

	private final int code;

	private final String error;

	private final String messageKey;

	SystemMessageTransportResultCode(int httpStatus, int code, String error, String messageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

}
