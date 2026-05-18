package com.auth.service.system.schedule.validation.form;

import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 修改任务表单一致性校验器
 *
 * @author Bunny
 */
public class SysJobUpdateFormConsistencyValidator implements ConstraintValidator<ValidSysJobUpdateForm, SysJobUpdateForm> {

	@Override
	public boolean isValid(SysJobUpdateForm form, ConstraintValidatorContext context) {
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

	private boolean validateBeanInvoke(SysJobUpdateForm form, ConstraintValidatorContext context) {
		if (SysJobFormConstraintSupport.isBlank(form.getInvokeTarget())) {
			SysJobFormConstraintSupport.addViolation(context, "invokeTarget", "指定 Bean 调用模式时必须填写 invokeTarget");
			return false;
		}
		return true;
	}

	private boolean validateCustomClass(SysJobUpdateForm form, ConstraintValidatorContext context) {
		if (!SysJobFormConstraintSupport.isBlank(form.getInvokeTarget())) {
			SysJobFormConstraintSupport.addViolation(context, "invokeTarget", "指定自定义类模式时不允许填写 invokeTarget");
			return false;
		}
		return true;
	}

	private boolean validateMessageDispatch(SysJobUpdateForm form, ConstraintValidatorContext context) {
		if (SysJobFormConstraintSupport.isBlank(form.getPayloadJson())) {
			SysJobFormConstraintSupport.addViolation(context, "payloadJson", "指定消息派发模式时必须填写 payloadJson");
			return false;
		}
		return true;
	}

}
