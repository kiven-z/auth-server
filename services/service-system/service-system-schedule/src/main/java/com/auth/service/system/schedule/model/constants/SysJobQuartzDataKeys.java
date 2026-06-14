package com.auth.service.system.schedule.model.constants;

import lombok.experimental.UtilityClass;

import java.time.ZoneId;

/**
 * Quartz {@link org.quartz.JobDataMap} 中与业务任务相关的键名约定
 *
 * @author Bunny
 */
@UtilityClass
public class SysJobQuartzDataKeys {

	/**
	 * 业务表 {@code sys_job.id}，用于日志与熔断关联
	 */
	public static final String SYS_JOB_ID = "sysJobId";

	/**
	 * 调用目标字符串（Bean 方法），与 {@code sys_job.invoke_target} 一致
	 */
	public static final String INVOKE_TARGET = "invokeTarget";

	/**
	 * 白名单任务类全限定名，与 {@code sys_job.job_class} 一致
	 */
	public static final String JOB_CLASS = "jobClass";

	/**
	 * 任务类型（BEAN_INVOKE/CUSTOM_CLASS/MESSAGE_DISPATCH）
	 */
	public static final String TASK_TYPE = "taskType";

	/**
	 * 处理器编码
	 */
	public static final String HANDLER_CODE = "handlerCode";

	/**
	 * 扩展载荷 JSON
	 */
	public static final String PAYLOAD_JSON = "payloadJson";

	/**
	 * 触发类型
	 */
	public static final String TRIGGER_TYPE = "triggerType";

	/**
	 * 定时触发
	 */
	public static final String TRIGGER_TYPE_SCHEDULE = "SCHEDULE";

	/**
	 * 手动触发
	 */
	public static final String TRIGGER_TYPE_MANUAL = "MANUAL";

	/**
	 * Trigger 名称规则：{@code jobName + "_TRIGGER"}
	 */
	public static final String TRIGGER_SUFFIX = "_TRIGGER";

	/**
	 * 与 {@code spring.jackson.time-zone} 及业务库一致，Cron 按东八区解析
	 */
	public static final ZoneId JOB_ZONE = ZoneId.of("Asia/Shanghai");

	/**
	 * bean名称
	 */
	public static final String BEAN_NAME = "beanName";

	/**
	 * 方法名称
	 */
	public static final String METHOD_NAME = "methodName";

}
