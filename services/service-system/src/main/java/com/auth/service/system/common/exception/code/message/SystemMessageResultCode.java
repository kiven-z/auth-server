package com.auth.service.system.common.exception.code.message;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 统一消息发送与 Dypns 验证码短信（SMS_ALIYUN_DYPNS）结果码
 *
 * <p>
 * 号段：279–296（下一可用 297）
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
	 * Dypns 验证码短信模板不存在或已禁用（channel=SMS_ALIYUN_DYPNS）
	 */
	SMS_TEMPLATE_NOT_FOUND(404, 281, "SMS_TEMPLATE_NOT_FOUND", "system.sms.template.not_found"),
	/**
	 * 缺少必填 Dypns 验证码短信模板变量
	 */
	SMS_TEMPLATE_VARS_MISSING(422, 282, "SMS_TEMPLATE_VARS_MISSING", "system.sms.template.vars_missing"),
	/**
	 * Dypns 验证码短信发送失败
	 */
	SMS_SEND_FAILED(502, 283, "SMS_SEND_FAILED", "system.sms.send.failed"),
	/**
	 * Dypns 验证码短信接收人为空
	 */
	SMS_TARGET_EMPTY(400, 284, "SMS_TARGET_EMPTY", "system.sms.send.target_empty"),
	/**
	 * Dypns 短信签名未配置
	 */
	SMS_SIGN_NAME_NOT_CONFIGURED(500, 285, "SMS_SIGN_NAME_NOT_CONFIGURED", "system.sms.sign_name.not_configured"),
	/**
	 * Dypns 验证码短信未启用（需开启 auth.message.sms.enabled 并配置阿里云 Dypns）
	 */
	SMS_SENDER_NOT_ENABLED(503, 286, "SMS_SENDER_NOT_ENABLED", "system.sms.sender.not_enabled"),
	/**
	 * 钉钉工作通知发送失败
	 */
	DING_TALK_SEND_FAILED(502, 289, "DING_TALK_SEND_FAILED", "system.dingtalk.send.failed"),
	/**
	 * 钉钉接收人为空
	 */
	DING_TALK_TARGET_EMPTY(400, 290, "DING_TALK_TARGET_EMPTY", "system.dingtalk.send.target_empty"),
	/**
	 * 钉钉工作通知未启用（需设置 auth.message.ding-talk.enabled=true）
	 */
	DING_TALK_NOT_ENABLED(503, 291, "DING_TALK_NOT_ENABLED", "system.dingtalk.not_enabled"),
	/**
	 * 钉钉模板不存在或已禁用
	 */
	DING_TALK_TEMPLATE_NOT_FOUND(404, 292, "DING_TALK_TEMPLATE_NOT_FOUND", "system.dingtalk.template.not_found"),
	/**
	 * 缺少必填钉钉模板变量
	 */
	DING_TALK_TEMPLATE_VARS_MISSING(422, 293, "DING_TALK_TEMPLATE_VARS_MISSING",
			"system.dingtalk.template.vars_missing"),
	/**
	 * 钉钉正文为空
	 */
	DING_TALK_BODY_EMPTY(400, 294, "DING_TALK_BODY_EMPTY", "system.dingtalk.body.empty"),
	/**
	 * 钉钉模板渲染失败
	 */
	DING_TALK_RENDER_FAILED(500, 295, "DING_TALK_RENDER_FAILED", "system.dingtalk.render.failed"),
	/**
	 * SMS_ALIYUN_DYPNS 不支持自定义正文（须使用 templateCode 与 variables）
	 */
	SMS_CUSTOM_BODY_NOT_SUPPORTED(400, 296, "SMS_CUSTOM_BODY_NOT_SUPPORTED", "system.sms.custom_body.not_supported");

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
