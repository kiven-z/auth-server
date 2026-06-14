package com.auth.service.system.schedule.validation.form;

import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.validation.SysJobTaskTypeFieldRules;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 新增任务表单一致性校验器
 *
 * @author Bunny
 */
public class SysJobCreateFormConsistencyValidator
		implements ConstraintValidator<ValidSysJobCreateForm, SysJobCreateForm> {

	@Override
	public boolean isValid(SysJobCreateForm form, ConstraintValidatorContext context) {
		if (form == null || SysJobFormConstraintSupport.isBlank(form.getTaskType())) {
			return true;
		}
		SysJobTaskType taskType = SysJobFormConstraintSupport.resolveTaskType(form.getTaskType());
		return SysJobTaskTypeFieldRules
			.detect(taskType, form.getJobClass(), form.getInvokeTarget(), form.getPayloadJson(), true)
			.map(violation -> SysJobTaskTypeFieldRules.applyFormViolation(violation, context))
			.orElse(true);
	}

}
