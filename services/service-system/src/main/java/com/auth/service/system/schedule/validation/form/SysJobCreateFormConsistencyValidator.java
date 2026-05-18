package com.auth.service.system.schedule.validation.form;

import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 新增任务表单一致性校验器
 *
 * @author Bunny
 */
public class SysJobCreateFormConsistencyValidator implements ConstraintValidator<ValidSysJobCreateForm, SysJobCreateForm> {

	@Override
	public boolean isValid(SysJobCreateForm form, ConstraintValidatorContext context) {
		if (form == null) {
			return true;
		}
		if (SysJobFormConstraintSupport.isBlank(form.getTaskType())) {
			return true;
		}

		SysJobTaskType taskType = SysJobFormConstraintSupport.resolveTaskType(form.getTaskType());
		return switch (taskType) {
			case BEAN_INVOKE -> validateBeanInvoke(form, context);
			case CUSTOM_CLASS -> validateCustomClass(form, context);
			case MESSAGE_DISPATCH -> validateMessageDispatch(form, context);
		};
	}

	private boolean validateBeanInvoke(SysJobCreateForm form, ConstraintValidatorContext context) {
		if (SysJobFormConstraintSupport.isBlank(form.getInvokeTarget())) {
			SysJobFormConstraintSupport.addViolation(context, "invokeTarget", "Bean 调用模式必须填写 invokeTarget");
			return false;
		}
		return true;
	}

	private boolean validateCustomClass(SysJobCreateForm form, ConstraintValidatorContext context) {
		if (SysJobFormConstraintSupport.isBlank(form.getJobClass())) {
			SysJobFormConstraintSupport.addViolation(context, "jobClass", "自定义类模式必须填写 jobClass");
			return false;
		}
		if (!SysJobFormConstraintSupport.isBlank(form.getInvokeTarget())) {
			SysJobFormConstraintSupport.addViolation(context, "invokeTarget", "自定义类模式不允许填写 invokeTarget");
			return false;
		}
		return true;
	}

	private boolean validateMessageDispatch(SysJobCreateForm form, ConstraintValidatorContext context) {
		if (SysJobFormConstraintSupport.isBlank(form.getPayloadJson())) {
			SysJobFormConstraintSupport.addViolation(context, "payloadJson", "消息派发模式必须填写 payloadJson");
			return false;
		}
		return true;
	}

}
