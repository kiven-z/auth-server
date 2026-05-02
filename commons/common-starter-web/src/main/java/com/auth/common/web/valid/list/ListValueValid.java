package com.auth.common.web.valid.list;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 传入值是否在指定集合中
 *
 * @author Bunny
 */
@Constraint(validatedBy = { ListValueConstraintValidator.class })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
public @interface ListValueValid {

	String message() default "The value is not within the range.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	int[] vals() default {};

}
