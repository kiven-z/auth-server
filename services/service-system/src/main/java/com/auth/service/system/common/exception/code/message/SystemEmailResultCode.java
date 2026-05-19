package com.auth.service.system.common.exception.code.message;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 邮件模板、发送与 require_fields 结果码
 *
 * <p>
 * 号段：241、247–261（248–249 合并为 248；255–256、262 已释放）
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
	REQUIRE_FIELD_KEY_DUPLICATE(422, 261, "REQUIRE_FIELD_KEY_DUPLICATE", "system.email.require_field.key_duplicate");

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
