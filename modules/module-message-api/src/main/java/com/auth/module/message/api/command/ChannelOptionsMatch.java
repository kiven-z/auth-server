package com.auth.module.message.api.command;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 校验 {@link TemplateMessageCommand#getChannel()} 与
 * {@link TemplateMessageCommand#getOptions()} 类型一致
 *
 * @author Bunny
 */
@Constraint(validatedBy = ChannelOptionsMatchValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ChannelOptionsMatch {

    /**
     * 校验失败消息
     */
    String message() default "channel and options type mismatch";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
