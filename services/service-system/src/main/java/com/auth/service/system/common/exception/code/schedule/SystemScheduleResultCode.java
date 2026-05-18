package com.auth.service.system.common.exception.code.schedule;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 定时任务 / Quartz / Job 配置结果码
 *
 * <p>
 * 号段：212–240
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemScheduleResultCode implements SystemResultCode {

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
	/**
	 * 任务类型不支持
	 */
	JOB_TASK_TYPE_UNSUPPORTED(422, 241, "JOB_TASK_TYPE_UNSUPPORTED", "system.job.task_type.unsupported"),
	/**
	 * payload_json 为空
	 */
	JOB_PAYLOAD_JSON_EMPTY(422, 242, "JOB_PAYLOAD_JSON_EMPTY", "system.job.payload.empty"),
	/**
	 * payload_json 非法
	 */
	JOB_PAYLOAD_JSON_INVALID(422, 243, "JOB_PAYLOAD_JSON_INVALID", "system.job.payload.invalid"),
	/**
	 * 消息命令参数校验失败
	 */
	JOB_MESSAGE_COMMAND_INVALID(422, 244, "JOB_MESSAGE_COMMAND_INVALID", "system.job.message.command.invalid"),
	/**
	 * payload_json 版本不支持
	 */
	JOB_PAYLOAD_VERSION_UNSUPPORTED(422, 245, "JOB_PAYLOAD_VERSION_UNSUPPORTED", "system.job.payload.version.unsupported");

	private final int httpStatus;

	private final int code;

	private final String error;

	private final String messageKey;

	SystemScheduleResultCode(int httpStatus, int code, String error, String messageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

}
