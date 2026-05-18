package com.auth.module.message.command;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 校验 {@link TemplateMessageCommand#getChannel()} 与
 * {@link TemplateMessageCommand#getOptions()} 类型一致
 *
 * @author Bunny
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ChannelOptionsMatchValidator.class)
public @interface ChannelOptionsMatch {

	/**
	 * 校验失败消息
	 */
	String message() default "channel and options type mismatch";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
