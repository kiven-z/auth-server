package com.auth.service.system.schedule.validation;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.validation.form.SysJobFormConstraintSupport;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.Optional;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_MODE_BEAN_REQUIRES_INVOKE_TARGET;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_MODE_CUSTOM_FORBIDS_INVOKE_TARGET;

/**
 * 任务类型字段互斥/必填规则（Form 与 Entity 共用）
 *
 * @author Bunny
 */
@UtilityClass
public class SysJobTaskTypeFieldRules {

	/**
	 * 检测任务类型相关字段是否违反规则
	 * @param taskType 任务类型
	 * @param jobClass 自定义类
	 * @param invokeTarget 调用目标
	 * @param payloadJson 消息载荷
	 * @param requireJobClass 是否要求 jobClass 非空（新增表单为 true）
	 * @return 违反类型；无违反则为 empty
	 */
	public static Optional<Violation> detect(SysJobTaskType taskType, String jobClass, String invokeTarget,
			String payloadJson, boolean requireJobClass) {
		return switch (taskType) {
			case BEAN_INVOKE -> CharSequenceUtil.isBlank(invokeTarget)
					? Optional.of(Violation.BEAN_MISSING_INVOKE_TARGET) : Optional.empty();
			case CUSTOM_CLASS -> {
				if (requireJobClass && CharSequenceUtil.isBlank(jobClass)) {
					yield Optional.of(Violation.CUSTOM_MISSING_JOB_CLASS);
				}
				if (CharSequenceUtil.isNotBlank(invokeTarget)) {
					yield Optional.of(Violation.CUSTOM_FORBIDS_INVOKE_TARGET);
				}
				yield Optional.empty();
			}
			case MESSAGE_DISPATCH -> CharSequenceUtil.isBlank(payloadJson)
					? Optional.of(Violation.MESSAGE_MISSING_PAYLOAD) : Optional.empty();
		};
	}

	/**
	 * 将违反类型转为业务异常（Service 层）
	 * @param violation 违反类型
	 */
	public static void throwIfPresent(Violation violation) {
		switch (violation) {
			case BEAN_MISSING_INVOKE_TARGET -> throw new SysJobException(JOB_MODE_BEAN_REQUIRES_INVOKE_TARGET);
			case CUSTOM_MISSING_JOB_CLASS -> throw new SysJobException(PARAM_REQUIRED, "job_class");
			case CUSTOM_FORBIDS_INVOKE_TARGET -> throw new SysJobException(JOB_MODE_CUSTOM_FORBIDS_INVOKE_TARGET);
			case MESSAGE_MISSING_PAYLOAD -> throw new SysJobException(PARAM_REQUIRED, "payload_json");
			default -> {
			}
		}
	}

	/**
	 * 将违反类型写入表单校验上下文
	 * @param violation 违反类型
	 * @param context 校验上下文
	 * @return 始终 false
	 */
	public static boolean applyFormViolation(Violation violation, ConstraintValidatorContext context) {
		SysJobFormConstraintSupport.addViolation(context, violation.getProperty(), violation.getFormMessage());
		return false;
	}

	/**
	 * 字段规则违反类型
	 */
	@Getter
	public enum Violation {

		/**
		 * Bean 调用模式必须填写 invokeTarget
		 */
		BEAN_MISSING_INVOKE_TARGET("invokeTarget", "Bean 调用模式必须填写 invokeTarget"),

		/**
		 * 自定义类模式必须填写 jobClass
		 */
		CUSTOM_MISSING_JOB_CLASS("jobClass", "自定义类模式必须填写 jobClass"),

		/**
		 * 自定义类模式不允许填写 invokeTarget
		 */
		CUSTOM_FORBIDS_INVOKE_TARGET("invokeTarget", "自定义类模式不允许填写 invokeTarget"),

		/**
		 * 消息派发模式必须填写 payloadJson
		 */
		MESSAGE_MISSING_PAYLOAD("payloadJson", "消息派发模式必须填写 payloadJson");

		private final String property;

		private final String formMessage;

		Violation(String property, String formMessage) {
			this.property = property;
			this.formMessage = formMessage;
		}

	}

}
