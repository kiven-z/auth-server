package com.auth.service.system.common.exception.code.message;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 统一消息发送与短信渠道结果码
 *
 * <p>
 * 号段：279–285（下一可用 286）
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemMessageResultCode implements SystemResultCode {

	/**
	 * 消息发送命令为空
	 */
	MESSAGE_SEND_COMMAND_NULL(400, 279, "MESSAGE_SEND_COMMAND_NULL", "system.message.send.command_null"),
	/**
	 * 消息渠道未实现或不支持
	 */
	MESSAGE_CHANNEL_UNSUPPORTED(400, 280, "MESSAGE_CHANNEL_UNSUPPORTED", "system.message.channel.unsupported"),
	/**
	 * 短信模板不存在或已禁用
	 */
	SMS_TEMPLATE_NOT_FOUND(404, 281, "SMS_TEMPLATE_NOT_FOUND", "system.sms.template.not_found"),
	/**
	 * 缺少必填短信模板变量
	 */
	SMS_TEMPLATE_VARS_MISSING(422, 282, "SMS_TEMPLATE_VARS_MISSING", "system.sms.template.vars_missing"),
	/**
	 * 短信发送失败
	 */
	SMS_SEND_FAILED(502, 283, "SMS_SEND_FAILED", "system.sms.send.failed"),
	/**
	 * 短信接收人为空
	 */
	SMS_TARGET_EMPTY(400, 284, "SMS_TARGET_EMPTY", "system.sms.send.target_empty"),
	/**
	 * 短信签名未配置
	 */
	SMS_SIGN_NAME_NOT_CONFIGURED(500, 285, "SMS_SIGN_NAME_NOT_CONFIGURED", "system.sms.sign_name.not_configured");

	private final int httpStatus;

	private final int code;

	private final String error;

	private final String messageKey;

	SystemMessageResultCode(int httpStatus, int code, String error, String messageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

}
