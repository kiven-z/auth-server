package com.auth.service.system.schedule.validation.form;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 新增任务表单一致性校验
 *
 * @author Bunny
 */
@Constraint(validatedBy = SysJobCreateFormConsistencyValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidSysJobCreateForm {

	String message() default "invalid job create form";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
