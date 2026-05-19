package com.auth.service.system.common.exception.code;

import lombok.Getter;

/**
 * 定时任务 / Quartz / Job 配置结果码
 *
 * <p>
 * 号段：212–218、220、224–245（219、221–223、229–237 已释放）
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemScheduleResultCode implements SystemResultCode {

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
	 * Quartz 操作失败（{0}=register|update|delete|runOnce，{1}=详情）
	 */
	JOB_QUARTZ_OPERATION_FAILED(500, 220, "JOB_QUARTZ_OPERATION_FAILED", "system.job.quartz.operation_failed"),

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
	 * 任务类无法加载
	 */
	JOB_CLASS_LOAD_FAILED(422, 235, "JOB_CLASS_LOAD_FAILED", "system.job.class.load_failed"),

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
	JOB_PAYLOAD_VERSION_UNSUPPORTED(422, 245, "JOB_PAYLOAD_VERSION_UNSUPPORTED",
			"system.job.payload.version.unsupported");

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
