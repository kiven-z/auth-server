package com.auth.service.system.common.exception.code.message;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 邮件模板、发送与 require_fields 结果码
 *
 * <p>
 * 号段：241–262
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemEmailResultCode implements SystemResultCode {

	/**
	 * 邮件模板不存在或已禁用
	 */
	EMAIL_TEMPLATE_NOT_FOUND(404, 241, "EMAIL_TEMPLATE_NOT_FOUND", "system.email.template.not_found"),
	/**
	 * 邮件发送参数为空
	 */
	EMAIL_SEND_DTO_NULL(400, 242, "EMAIL_SEND_DTO_NULL", "system.email.send.dto_null"),
	/**
	 * 收件人为空
	 */
	EMAIL_TO_EMPTY(400, 243, "EMAIL_TO_EMPTY", "system.email.send.to_empty"),
	/**
	 * 邮件主题为空
	 */
	EMAIL_SUBJECT_EMPTY(400, 244, "EMAIL_SUBJECT_EMPTY", "system.email.send.subject_empty"),
	/**
	 * 邮件正文为空
	 */
	EMAIL_BODY_EMPTY(400, 245, "EMAIL_BODY_EMPTY", "system.email.send.body_empty"),
	/**
	 * 附件文件名为空
	 */
	EMAIL_ATTACHMENT_FILENAME_EMPTY(400, 246, "EMAIL_ATTACHMENT_FILENAME_EMPTY",
			"system.email.attachment.filename_empty"),
	/**
	 * 缺少必填模板变量
	 */
	EMAIL_TEMPLATE_VARS_MISSING(422, 247, "EMAIL_TEMPLATE_VARS_MISSING", "system.email.template.vars_missing"),
	/**
	 * 主题渲染失败
	 */
	EMAIL_SUBJECT_RENDER_FAILED(500, 248, "EMAIL_SUBJECT_RENDER_FAILED", "system.email.render.subject_failed"),
	/**
	 * 正文渲染失败
	 */
	EMAIL_BODY_RENDER_FAILED(500, 249, "EMAIL_BODY_RENDER_FAILED", "system.email.render.body_failed"),
	/**
	 * 附件 Base64 解码失败
	 */
	EMAIL_ATTACHMENT_BASE64_FAILED(400, 250, "EMAIL_ATTACHMENT_BASE64_FAILED", "system.email.attachment.base64_failed"),
	/**
	 * 附件 URL 响应非 2xx
	 */
	EMAIL_ATTACHMENT_HTTP_STATUS(502, 251, "EMAIL_ATTACHMENT_HTTP_STATUS", "system.email.attachment.http_status"),
	/**
	 * 附件 URL 下载失败
	 */
	EMAIL_ATTACHMENT_DOWNLOAD_FAILED(502, 252, "EMAIL_ATTACHMENT_DOWNLOAD_FAILED",
			"system.email.attachment.download_failed"),
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
	 * require_fields 须为 JSON 数组
	 */
	REQUIRE_FIELDS_NOT_ARRAY(422, 255, "REQUIRE_FIELDS_NOT_ARRAY", "system.email.require_fields.not_array"),
	/**
	 * 数组元素须为对象
	 */
	REQUIRE_FIELDS_ELEMENT_NOT_OBJECT(422, 256, "REQUIRE_FIELDS_ELEMENT_NOT_OBJECT",
			"system.email.require_fields.element_not_object"),
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
	 * FreeMarker 模型构建失败
	 */
	FREEMARKER_MODEL_BUILD_FAILED(500, 262, "FREEMARKER_MODEL_BUILD_FAILED", "system.email.freemarker.model_failed");

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
