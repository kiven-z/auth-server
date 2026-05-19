package com.auth.service.system.message.infrastructure.exception;

import com.auth.service.system.common.exception.SystemResultCode;
import lombok.Getter;

/**
 * 邮件模板、require_fields 与 MIME 传输结果码
 *
 * <p>
 * 号段：241、247–248、253–261、262–270（255–256 已释放，见 {@code package-info.java}）
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemEmailResultCode implements SystemResultCode {

	/**
	 * 邮件模板不存在或已禁用
	 */
	EMAIL_TEMPLATE_NOT_FOUND(404, 241, "EMAIL_TEMPLATE_NOT_FOUND", "system.message.template.not_found"),

	/**
	 * 缺少必填模板变量
	 */
	EMAIL_TEMPLATE_VARS_MISSING(422, 247, "EMAIL_TEMPLATE_VARS_MISSING", "system.message.template.vars_missing"),

	/**
	 * 模板渲染失败（{0}=subject|body，{1}=详情）
	 */
	EMAIL_RENDER_FAILED(500, 248, "EMAIL_RENDER_FAILED", "system.email.render.failed"),

	/**
	 * require_fields 不能为 null
	 */
	REQUIRE_FIELDS_NULL(422, 253, "REQUIRE_FIELDS_NULL", "system.email.require_fields.null"),

	/**
	 * require_fields 序列化失败
	 */
	REQUIRE_FIELDS_SERIALIZE_FAILED(400, 254, "REQUIRE_FIELDS_SERIALIZE_FAILED",
			"system.email.require_fields.serialize_failed"),

	/**
	 * 存在非法字段名
	 */
	REQUIRE_FIELDS_ILLEGAL_PROPERTY(422, 257, "REQUIRE_FIELDS_ILLEGAL_PROPERTY",
			"system.email.require_fields.illegal_property"),

	/**
	 * require_fields 解析失败
	 */
	REQUIRE_FIELDS_PARSE_FAILED(422, 258, "REQUIRE_FIELDS_PARSE_FAILED", "system.email.require_fields.parse_failed"),

	/**
	 * 变量 key 不能为空
	 */
	REQUIRE_FIELD_KEY_EMPTY(422, 259, "REQUIRE_FIELD_KEY_EMPTY", "system.email.require_field.key_empty"),

	/**
	 * exampleValue 不能为空
	 */
	REQUIRE_FIELD_EXAMPLE_EMPTY(422, 260, "REQUIRE_FIELD_EXAMPLE_EMPTY", "system.email.require_field.example_empty"),

	/**
	 * 变量 key 重复
	 */
	REQUIRE_FIELD_KEY_DUPLICATE(422, 261, "REQUIRE_FIELD_KEY_DUPLICATE", "system.email.require_field.key_duplicate"),

	/**
	 * 邮件发送参数为空
	 */
	EMAIL_SEND_DTO_NULL(400, 262, "EMAIL_SEND_DTO_NULL", "system.email.send.dto_null"),

	/**
	 * 收件人不能为空
	 */
	EMAIL_SEND_TO_EMPTY(400, 263, "EMAIL_SEND_TO_EMPTY", "system.email.send.to_empty"),

	/**
	 * 邮件主题不能为空
	 */
	EMAIL_SEND_SUBJECT_EMPTY(400, 264, "EMAIL_SEND_SUBJECT_EMPTY", "system.email.send.subject_empty"),

	/**
	 * 邮件正文不能为空
	 */
	EMAIL_SEND_BODY_EMPTY(400, 265, "EMAIL_SEND_BODY_EMPTY", "system.email.send.body_empty"),

	/**
	 * 附件文件名不能为空
	 */
	EMAIL_ATTACHMENT_FILENAME_EMPTY(400, 266, "EMAIL_ATTACHMENT_FILENAME_EMPTY",
			"system.email.attachment.filename_empty"),

	/**
	 * 附件 Base64 解码失败（{0}=详情）
	 */
	EMAIL_ATTACHMENT_BASE64_FAILED(400, 267, "EMAIL_ATTACHMENT_BASE64_FAILED", "system.email.attachment.base64_failed"),

	/**
	 * 从 URL 下载附件失败，HTTP 状态非 2xx（{0}=状态码）
	 */
	EMAIL_ATTACHMENT_HTTP_STATUS(502, 268, "EMAIL_ATTACHMENT_HTTP_STATUS", "system.email.attachment.http_status"),

	/**
	 * 从 URL 下载附件失败（{0}=详情）
	 */
	EMAIL_ATTACHMENT_DOWNLOAD_FAILED(502, 269, "EMAIL_ATTACHMENT_DOWNLOAD_FAILED",
			"system.email.attachment.download_failed"),

	/**
	 * MIME 邮件发送失败（{0}=详情）
	 */
	EMAIL_SEND_FAILED(502, 270, "EMAIL_SEND_FAILED", "system.email.send.failed");

	private final int httpStatus;

	private final int code;

	private final String error;

	private final String messageKey;

	SystemEmailResultCode(int httpStatus, int code, String error, String messageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

}
