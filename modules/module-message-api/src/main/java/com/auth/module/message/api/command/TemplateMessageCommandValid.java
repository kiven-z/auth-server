package com.auth.module.message.api.command;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * {@link TemplateMessageCommand} 业务校验：模板模式需 templateCode；自定义正文模式需 body
 *
 * @author Bunny
 */
@Constraint(validatedBy = TemplateMessageCommandValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TemplateMessageCommandValid {

	/**
	 * 校验失败提示
	 * @return 消息
	 */
	String message() default "Invalid template message command";

	/**
	 * 分组
	 * @return 分组
	 */
	Class<?>[] groups() default {};

	/**
	 * 负载
	 * @return 负载
	 */
	Class<? extends Payload>[] payload() default {};

}
