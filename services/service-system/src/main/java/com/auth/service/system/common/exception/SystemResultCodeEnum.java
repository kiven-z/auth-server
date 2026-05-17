package com.auth.service.system.common.exception;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

import com.auth.common.web.context.SpringContextHolder;

import lombok.Getter;

/**
 * 系统服务结果代码(模块 {@code 10})
 *
 * <p>
 * 业务代码格式: {@code 10CATSEQ} where {@code CAT} is the 1-digit category and {@code SEQ} is 2
 * digits.
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemResultCodeEnum {

	// 0xx: 成功/通用成功消息(API成功信封仍为HTTP 200)
	/**
	 * 添加成功
	 */
	ADD_SUCCESS(200, 0, "ADD_SUCCESS", "result.add.success"),
	/**
	 * 更新成功
	 */
	UPDATE_SUCCESS(200, 0, "UPDATE_SUCCESS", "result.update.success"),
	/**
	 * 删除成功
	 */
	DELETE_SUCCESS(200, 0, "DELETE_SUCCESS", "result.delete.success"),
	/**
	 * 分配成功
	 */
	ASSIGN_SUCCESS(200, 0, "ASSIGN_SUCCESS", "result.assign.success"),
	/**
	 * 上传成功
	 */
	SUCCESS_UPLOAD(200, 0, "SUCCESS_UPLOAD", "result.success.upload"),
	/**
	 * 退出成功
	 */
	SUCCESS_LOGOUT(200, 0, "SUCCESS_LOGOUT", "result.success.logout"),

	// 2xx: 客户端/输入问题(HTTP大多为400/422)
	/**
	 * 请求为空
	 */
	REQUEST_IS_EMPTY(400, 201, "REQUEST_IS_EMPTY", "request.data.empty"),
	/**
	 * 数据不存在
	 */
	DATA_NOT_EXIST(404, 206, "DATA_NOT_EXIST", "data.not.exists"),
	/**
	 * 文件不存在
	 */
	FILE_NOT_EXIST(404, 207, "FILE_NOT_EXIST", "file.not.exists"),
	/**
	 * JSON解析异常
	 */
	JSON_PARSER_EXCEPTION(400, 202, "JSON_PARSER_EXCEPTION", "json.parser.exception"),
	/**
	 * 转换属性异常
	 */
	CONVERT_PROPERTY_EXCEPTION(400, 203, "CONVERT_PROPERTY_EXCEPTION", "convert.property.exception"),

	// 3xx-ish: 业务规则/提示(HTTP varies)
	/**
	 * 更新异常
	 */
	UPDATE_ERROR(400, 301, "UPDATE_ERROR", "update.error"),

	// 4xx-ish: 授权问题(HTTP 403)
	/**
	 * 无访问权限
	 */
	FAIL_NO_ACCESS_DENIED(403, 401, "FAIL_NO_ACCESS_DENIED", "no.access.denied"),

	// 4xx: 未找到
	/**
	 * 用户不存在
	 */
	USER_NOT_FOUND(404, 402, "USER_NOT_FOUND", "user.not.found"),
	/**
	 * 用户不存在
	 */
	USER_NOT_EXIST(404, 403, "USER_NOT_EXIST", "user.not.exist"),

	// 422: 非法请求
	/**
	 * 非法请求
	 */
	ILLEGAL_REQUEST(422, 204, "ILLEGAL_REQUEST", "illegal.request"),
	/**
	 * 验证码错误
	 */
	VERIFY_CODE_ERROR(422, 205, "VERIFY_CODE_ERROR", "verify.code.error"),
	/**
	 * 用户邮箱未绑定
	 */
	USER_EMAIL_NOT_BIND(422, 208, "USER_EMAIL_NOT_BIND", "user.email.not.bind"),
	/**
	 * 用户ID不能为空
	 */
	ILLEGAL_USER_ID_NOT_NULL(422, 209, "ILLEGAL_USER_ID_NOT_NULL", "illegal.user.id.not.null"),
	/**
	 * 新密码不能为空
	 */
	ILLEGAL_NEW_PASSWORD_NOT_NULL(422, 210, "ILLEGAL_NEW_PASSWORD_NOT_NULL", "illegal.new.password.not.null"),
	/**
	 * 非法页面动画
	 */
	ILLEGAL_PAGE_ANIMATION(422, 211, "ILLEGAL_PAGE_ANIMATION", "illegal.page.animation"),

	// --- Schedule: HTTP 演示 Job / Quartz / 任务配置（扩展 catSeq 212+）
	/**
	 * HttpInvokeJob：缺少 url
	 */
	HTTP_JOB_MISSING_URL(422, 212, "HTTP_JOB_MISSING_URL", "system.job.http.missing_url"),
	/**
	 * HttpInvokeJob：HTTP method 非法
	 */
	HTTP_JOB_INVALID_HTTP_METHOD(422, 213, "HTTP_JOB_INVALID_HTTP_METHOD", "system.job.http.invalid_method"),
	/**
	 * HttpInvokeJob：响应非 2xx
	 */
	HTTP_JOB_NON_2XX_RESPONSE(422, 214, "HTTP_JOB_NON_2XX_RESPONSE", "system.job.http.non2xx"),
	/**
	 * invoke_target 为空
	 */
	JOB_INVOKE_TARGET_EMPTY(422, 215, "JOB_INVOKE_TARGET_EMPTY", "system.job.invoke_target.empty"),
	/**
	 * invoke_target 格式应为 beanName.method
	 */
	JOB_INVOKE_TARGET_FORMAT(422, 216, "JOB_INVOKE_TARGET_FORMAT", "system.job.invoke_target.format"),
	/**
	 * invoke_target 括号不匹配
	 */
	JOB_INVOKE_TARGET_PAREN_MISMATCH(422, 217, "JOB_INVOKE_TARGET_PAREN_MISMATCH",
			"system.job.invoke_target.paren_mismatch"),
	/**
	 * 仅支持无参或单字符串参数
	 */
	JOB_INVOKE_PARAM_NOT_SUPPORTED(422, 218, "JOB_INVOKE_PARAM_NOT_SUPPORTED",
			"system.job.invoke_target.param_not_supported"),
	/**
	 * 反射未找到方法
	 */
	JOB_REFLECT_METHOD_NOT_FOUND(422, 219, "JOB_REFLECT_METHOD_NOT_FOUND", "system.job.reflect.method_not_found"),
	/**
	 * 注册 Quartz Job 失败
	 */
	JOB_QUARTZ_REGISTER_FAILED(500, 220, "JOB_QUARTZ_REGISTER_FAILED", "system.job.quartz.register_failed"),
	/**
	 * 更新 Quartz Job 失败
	 */
	JOB_QUARTZ_UPDATE_FAILED(500, 221, "JOB_QUARTZ_UPDATE_FAILED", "system.job.quartz.update_failed"),
	/**
	 * 删除 Quartz Job 失败
	 */
	JOB_QUARTZ_DELETE_FAILED(500, 222, "JOB_QUARTZ_DELETE_FAILED", "system.job.quartz.delete_failed"),
	/**
	 * 立即执行失败
	 */
	JOB_QUARTZ_RUN_ONCE_FAILED(500, 223, "JOB_QUARTZ_RUN_ONCE_FAILED", "system.job.quartz.run_once_failed"),
	/**
	 * 自定义 Job 模式禁止设置 invoke_target
	 */
	JOB_MODE_CUSTOM_FORBIDS_INVOKE_TARGET(422, 224, "JOB_MODE_CUSTOM_FORBIDS_INVOKE_TARGET",
			"system.job.mode.custom_forbids_invoke"),
	/**
	 * Bean 模式 invoke_target 不能为空
	 */
	JOB_MODE_BEAN_REQUIRES_INVOKE_TARGET(422, 225, "JOB_MODE_BEAN_REQUIRES_INVOKE_TARGET",
			"system.job.mode.bean_requires_invoke"),
	/**
	 * 任务分组不存在或未启用
	 */
	JOB_GROUP_NOT_AVAILABLE(422, 226, "JOB_GROUP_NOT_AVAILABLE", "system.job.group.not_available"),
	/**
	 * job_class 为空
	 */
	JOB_CLASS_EMPTY(422, 227, "JOB_CLASS_EMPTY", "system.job.class.empty"),
	/**
	 * 任务类未在白名单
	 */
	JOB_CLASS_NOT_WHITELISTED(422, 228, "JOB_CLASS_NOT_WHITELISTED", "system.job.class.not_whitelisted"),
	/**
	 * invoke_target 格式错误
	 */
	JOB_INVOKE_TARGET_MALFORMED(422, 229, "JOB_INVOKE_TARGET_MALFORMED", "system.job.invoke_target.malformed"),
	/**
	 * 未找到 Spring Bean
	 */
	JOB_SPRING_BEAN_NOT_FOUND(422, 230, "JOB_SPRING_BEAN_NOT_FOUND", "system.job.spring.bean_not_found"),
	/**
	 * Bean 未标注白名单
	 */
	JOB_BEAN_NOT_WHITELISTED(422, 231, "JOB_BEAN_NOT_WHITELISTED", "system.job.bean.not_whitelisted"),
	/**
	 * Bean 类型与任务类不一致
	 */
	JOB_BEAN_TYPE_MISMATCH(422, 232, "JOB_BEAN_TYPE_MISMATCH", "system.job.bean.type_mismatch"),
	/**
	 * 方法名为空
	 */
	JOB_METHOD_NAME_EMPTY(422, 233, "JOB_METHOD_NAME_EMPTY", "system.job.method.name_empty"),
	/**
	 * 未找到目标方法
	 */
	JOB_TARGET_METHOD_NOT_FOUND(422, 234, "JOB_TARGET_METHOD_NOT_FOUND", "system.job.method.not_found"),
	/**
	 * 任务类无法加载
	 */
	JOB_CLASS_LOAD_FAILED(422, 235, "JOB_CLASS_LOAD_FAILED", "system.job.class.load_failed"),
	/**
	 * 未找到方法或参数不匹配
	 */
	JOB_METHOD_OR_PARAM_MISMATCH(422, 236, "JOB_METHOD_OR_PARAM_MISMATCH", "system.job.method.param_mismatch"),
	/**
	 * 自定义 Job 须实现 org.quartz.Job
	 */
	JOB_CUSTOM_MUST_IMPLEMENT_QUARTZ_JOB(422, 237, "JOB_CUSTOM_MUST_IMPLEMENT_QUARTZ_JOB",
			"system.job.custom.must_implement_job"),
	/**
	 * 分组编码已存在
	 */
	JOB_GROUP_CODE_DUPLICATE(422, 238, "JOB_GROUP_CODE_DUPLICATE", "system.job.group.code_duplicate"),
	/**
	 * 系统核心分组禁止删除
	 */
	JOB_GROUP_SYSTEM_PROTECTED(422, 239, "JOB_GROUP_SYSTEM_PROTECTED", "system.job.group.system_protected"),
	/**
	 * 分组下仍有任务
	 */
	JOB_GROUP_HAS_ACTIVE_JOBS(422, 240, "JOB_GROUP_HAS_ACTIVE_JOBS", "system.job.group.has_active_jobs"),

	// --- Email / 模板变量
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

	// --- require_fields JSON
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
	FREEMARKER_MODEL_BUILD_FAILED(500, 262, "FREEMARKER_MODEL_BUILD_FAILED", "system.email.freemarker.model_failed"),

	// --- 统一消息发送
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
	SMS_SIGN_NAME_NOT_CONFIGURED(500, 285, "SMS_SIGN_NAME_NOT_CONFIGURED", "system.sms.sign_name.not_configured"),

	// 429: 重置密码过于频繁
	RESET_PASSWORD_TOO_FREQUENTLY(429, 302, "RESET_PASSWORD_TOO_FREQUENTLY", "reset.password.too.frequent"),

	// 5xx: 系统错误
	/**
	 * 数据库错误
	 */
	DATABASE_ERROR(500, 101, "DATABASE_ERROR", "database.error"),
	/**
	 * 服务器错误
	 */
	SERVER_ERROR(500, 199, "SERVER_ERROR", "server.error"),
	/**
	 * 服务不可用
	 */
	SERVICE_UNAVAILABLE(503, 102, "SERVICE_UNAVAILABLE", "service.unavailable"),

	/**
	 * 技术: JWT无效/过期(HTTP 401)
	 */
	JWT_INVALID(401, 405, "JWT_INVALID", "system.jwt.invalid"),

	/**
	 * 技术: Redis基础设施错误(HTTP 503)
	 */
	REDIS_SYSTEM_ERROR(503, 103, "REDIS_SYSTEM_ERROR", "system.redis.unavailable"),

	// --- Role Management (263+)
	/**
	 * 角色不存在
	 */
	ROLE_NOT_FOUND(404, 263, "ROLE_NOT_FOUND", "system.role.not_found"),
	/**
	 * 角色编码已存在
	 */
	ROLE_CODE_DUPLICATE(409, 264, "ROLE_CODE_DUPLICATE", "system.role.code_duplicate"),
	/**
	 * 系统角色不可删除
	 */
	ROLE_SYSTEM_CANNOT_DELETE(400, 265, "ROLE_SYSTEM_CANNOT_DELETE", "system.role.system_cannot_delete"),
	/**
	 * 系统角色不可禁用
	 */
	ROLE_SYSTEM_CANNOT_DISABLE(400, 266, "ROLE_SYSTEM_CANNOT_DISABLE", "system.role.system_cannot_disable"),
	/**
	 * 系统角色不可修改类型
	 */
	ROLE_SYSTEM_CANNOT_CHANGE_TYPE(400, 267, "ROLE_SYSTEM_CANNOT_CHANGE_TYPE", "system.role.system_cannot_change_type"),
	/**
	 * 角色已被引用，不可删除
	 */
	ROLE_IN_USE(409, 268, "ROLE_IN_USE", "system.role.in_use"),

	// --- User Profile (269–)
	/**
	 * 用户已存在
	 */
	USER_ALREADY_EXISTS(409, 269, "USER_ALREADY_EXISTS", "user.already.exists"),

	// --- Menu (270+)
	/**
	 * 菜单不存在
	 */
	MENU_NOT_FOUND(404, 270, "MENU_NOT_FOUND", "system.menu.not_found"),
	/**
	 * 角色编码在系统中不存在
	 */
	MENU_ROLE_CODE_INVALID(422, 271, "MENU_ROLE_CODE_INVALID", "system.menu.role_code_invalid"),
	/**
	 * 权限编码在系统中不存在
	 */
	MENU_PERMISSION_CODE_INVALID(422, 272, "MENU_PERMISSION_CODE_INVALID", "system.menu.permission_code_invalid"),

	// --- Permission Management (273+)
	/**
	 * 权限不存在
	 */
	PERMISSION_NOT_FOUND(404, 273, "PERMISSION_NOT_FOUND", "system.permission.not_found"),
	/**
	 * 权限编码已存在
	 */
	PERMISSION_CODE_DUPLICATE(409, 274, "PERMISSION_CODE_DUPLICATE", "system.permission.code_duplicate"),
	/**
	 * 权限编码格式不符合约定（全小写，1～3 段，段为 * 或 [a-z][a-z0-9]*）
	 */
	PERMISSION_CODE_FORMAT_INVALID(422, 277, "PERMISSION_CODE_FORMAT_INVALID", "system.permission.code_format_invalid"),
	/**
	 * 角色编码格式不符合约定（全大写，字母与下划线，首字符为字母）
	 */
	ROLE_CODE_FORMAT_INVALID(422, 278, "ROLE_CODE_FORMAT_INVALID", "system.role.code_format_invalid"),

	// --- Post Management (275+)
	/**
	 * 部门不存在、已停用或已删除，不可挂载岗位
	 */
	POST_DEPT_NOT_FOUND(404, 275, "POST_DEPT_NOT_FOUND", "system.post.dept_not_found"),
	/**
	 * 同一部门下岗位编码已存在
	 */
	POST_CODE_DUPLICATE_IN_DEPT(409, 276, "POST_CODE_DUPLICATE_IN_DEPT", "system.post.code_duplicate_in_dept");

	private final int httpStatus;

	/**
	 * 3位数字组合: {@code CAT*100+SEQ} (模块10是单独前缀的)
	 */
	private final int code;

	/**
	 * 错误信息
	 */
	private final String error;

	/**
	 * 国际化键
	 */
	private final String messageKey;

	/**
	 * 构造函数
	 * @param httpStatus HTTP状态码
	 * @param code 模块序号
	 * @param error 错误信息
	 * @param messageKey 国际化键
	 */
	SystemResultCodeEnum(int httpStatus, int code, String error, String messageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

	/**
	 * 获取业务代码
	 * @return 业务代码
	 */
	public int getBizCode() {
		return 100_000 + code;
	}

	/**
	 * 获取消息
	 * @param locale 区域
	 * @param args 参数
	 * @return 消息
	 */
	public String getMessage(Locale locale, Object... args) {
		return SpringContextHolder.getMessage(messageKey, args, locale);
	}

	/**
	 * 获取消息
	 * @param args 参数
	 * @return 消息
	 */
	public String getMessage(Object... args) {
		Locale locale = LocaleContextHolder.getLocale();
		return getMessage(locale, args);
	}

	/**
	 * 便捷方法: 渲染当前区域的消息，无需额外参数
	 */
	@SuppressWarnings("java:S3878")
	public String getMessage() {
		return getMessage(new Object[0]);
	}

}
