package com.auth.service.system.common.exception.code;

import lombok.Getter;

/**
 * 消息发送、模板渲染与渠道（Dypns、钉钉）相关结果码
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
	 * 消息发送命令业务规则不合法
	 */
	MESSAGE_COMMAND_INVALID(400, 283, "MESSAGE_COMMAND_INVALID", "system.message.send.command_invalid"),

	/**
	 * 消息渠道未实现或不支持
	 */
	MESSAGE_CHANNEL_UNSUPPORTED(400, 280, "MESSAGE_CHANNEL_UNSUPPORTED", "system.message.channel.unsupported"),

	/**
	 * Dypns 验证码短信模板不存在或已禁用（channel=SMS_ALIYUN_DYPNS）
	 */
	SMS_TEMPLATE_NOT_FOUND(404, 281, "SMS_TEMPLATE_NOT_FOUND", "system.message.template.not_found"),

	/**
	 * 缺少必填 Dypns 验证码短信模板变量
	 */
	SMS_TEMPLATE_VARS_MISSING(422, 282, "SMS_TEMPLATE_VARS_MISSING", "system.message.template.vars_missing"),

	/**
	 * 钉钉模板不存在或已禁用
	 */
	DING_TALK_TEMPLATE_NOT_FOUND(404, 292, "DING_TALK_TEMPLATE_NOT_FOUND", "system.message.template.not_found"),

	/**
	 * 缺少必填钉钉模板变量
	 */
	DING_TALK_TEMPLATE_VARS_MISSING(422, 293, "DING_TALK_TEMPLATE_VARS_MISSING",
			"system.message.template.vars_missing"),

	/**
	 * 钉钉正文为空
	 */
	DING_TALK_BODY_EMPTY(400, 294, "DING_TALK_BODY_EMPTY", "system.dingtalk.body.empty"),

	/**
	 * 钉钉模板渲染失败
	 */
	DING_TALK_RENDER_FAILED(500, 295, "DING_TALK_RENDER_FAILED", "system.dingtalk.render.failed"),

	/**
	 * SMS_ALIYUN_DYPNS 只走模板（templateCode + variables），不支持自定义正文
	 */
	SMS_CUSTOM_BODY_NOT_SUPPORTED(400, 296, "SMS_CUSTOM_BODY_NOT_SUPPORTED", "system.sms.custom_body.not_supported"),

	/**
	 * Dypns 手机号不能为空
	 */
	SMS_DYPNS_PHONE_BLANK(400, 297, "SMS_DYPNS_PHONE_BLANK", "system.sms.dypns.phone_blank"),

	/**
	 * Dypns 厂商模板编码不能为空
	 */
	SMS_DYPNS_TEMPLATE_CODE_BLANK(400, 298, "SMS_DYPNS_TEMPLATE_CODE_BLANK", "system.sms.dypns.template_code_blank"),

	/**
	 * Dypns 发送被中断
	 */
	SMS_DYPNS_SEND_INTERRUPTED(502, 299, "SMS_DYPNS_SEND_INTERRUPTED", "system.sms.dypns.send_interrupted"),

	/**
	 * Dypns 发送失败（{0}=详情）
	 */
	SMS_DYPNS_SEND_FAILED(502, 300, "SMS_DYPNS_SEND_FAILED", "system.sms.dypns.send_failed"),

	/**
	 * 钉钉 OAuth 响应体为空
	 */
	DING_TALK_OAUTH_EMPTY_RESPONSE(502, 301, "DING_TALK_OAUTH_EMPTY_RESPONSE", "system.dingtalk.oauth.empty_response"),

	/**
	 * 钉钉 OAuth accessToken 为空
	 */
	DING_TALK_OAUTH_ACCESS_TOKEN_BLANK(502, 302, "DING_TALK_OAUTH_ACCESS_TOKEN_BLANK",
			"system.dingtalk.oauth.access_token_blank"),

	/**
	 * 钉钉 OAuth 请求失败（{0}=详情）
	 */
	DING_TALK_OAUTH_TOKEN_REQUEST_FAILED(502, 303, "DING_TALK_OAUTH_TOKEN_REQUEST_FAILED",
			"system.dingtalk.oauth.token_request_failed"),

	/**
	 * 钉钉工作通知接收人不能为空
	 */
	DING_TALK_WORK_NOTICE_USER_IDS_EMPTY(400, 304, "DING_TALK_WORK_NOTICE_USER_IDS_EMPTY",
			"system.dingtalk.work_notice.user_ids_empty"),

	/**
	 * 钉钉工作通知正文不能为空
	 */
	DING_TALK_WORK_NOTICE_CONTENT_BLANK(400, 305, "DING_TALK_WORK_NOTICE_CONTENT_BLANK",
			"system.dingtalk.work_notice.content_blank"),

	/**
	 * 钉钉工作通知 HTTP 请求失败（{0}=详情）
	 */
	DING_TALK_WORK_NOTICE_HTTP_ERROR(502, 306, "DING_TALK_WORK_NOTICE_HTTP_ERROR",
			"system.dingtalk.work_notice.http_error"),

	/**
	 * 钉钉工作通知响应体为空
	 */
	DING_TALK_WORK_NOTICE_EMPTY_RESPONSE(502, 307, "DING_TALK_WORK_NOTICE_EMPTY_RESPONSE",
			"system.dingtalk.work_notice.empty_response"),

	/**
	 * 钉钉工作通知发送失败（{0}=详情）
	 */
	DING_TALK_WORK_NOTICE_FAILED(502, 308, "DING_TALK_WORK_NOTICE_FAILED", "system.dingtalk.work_notice.failed");

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
