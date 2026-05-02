package com.auth.common.web.valid.sensitive;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 敏感数据注解
 *
 * @author Bunny
 */
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerialize.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sensitive {

	String message() default "Verification failed";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * 敏感数据类型
	 */
	SensitiveType type();

	/**
	 * 前缀保留长度(仅CUSTOM类型有效)
	 */
	int prefixLen() default 0;

	/**
	 * 后缀保留长度(仅CUSTOM类型有效)
	 */
	int suffixLen() default 0;

	/**
	 * 替换字符
	 */
	String symbol() default "*";

}
