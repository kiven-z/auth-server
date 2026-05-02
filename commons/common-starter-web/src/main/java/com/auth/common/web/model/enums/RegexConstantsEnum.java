package com.auth.common.web.model.enums;

import lombok.Getter;

/**
 * 常用正则常量枚举
 *
 * @author Bunny
 */
@Getter
public enum RegexConstantsEnum {

	/**
	 * 只能是字母 整个字符串必须完全由字母组成，不能包含任何其他字符（包括空格、数字、标点符号等）
	 */
	ENGLISH_NAME("^[a-zA-Z]+$", "Letters only"),

	/**
	 * 字母 匹配任何包含至少一个字母的字符串，即使字符串中还包含其他字符
	 */
	LETTER("[a-zA-Z]", "Contains letters"),

	/**
	 * 数字
	 */
	DIGIT("[0-9]", "Digits"),

	/**
	 * 大写字母
	 */
	UPPER_CASE("[A-Z]", "Uppercase letters"),

	/**
	 * 小写字母
	 */
	LOWER_CASE("[a-z]", "Lowercase letters"),

	/**
	 * 特殊字符
	 */
	SPECIAL_CHAR("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]", "Special characters"),

	/**
	 * IPV4正则
	 */
	IPV4("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", "IPv4 address"),

	/**
	 * IPV6正则
	 */
	IPV6("^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9]))$",
			"IPv6 address"),

	;

	/**
	 * 1.包含字母 2.或者是字母和数字的组合 3.或者是字母加下划线加数字的组合 4.不能是纯数字 5.不能是纯下划线 6.不能是数字和下划线的组合
	 */
	public static final String ALPHANUMERIC_UNDERSCORE_REQUIRE_LETTER = "^(?=.*[a-zA-Z])(?!\\d+$)(?!_+$)(?![\\d_]+$)[a-zA-Z0-9_]+$";

	/**
	 * 仅字母、下划线、中划线，且必须包含至少一个字母：
	 * <ol>
	 * <li>若出现字母，整串仅允许字母、下划线、中划线（不允许数字等其他字符）</li>
	 * <li>不能包含数字</li>
	 * <li>允许字母与下划线、中划线的组合</li>
	 * <li>不能是纯数字（字符集不含数字，天然排除）</li>
	 * <li>不能是纯下划线（必须至少包含一个字母）</li>
	 * </ol>
	 */
	public static final String LETTER_UNDERSCORE_HYPHEN_REQUIRE_LETTER = "^(?=.*[a-zA-Z])[a-zA-Z_-]+$";

	/**
	 * 权限正则
	 */
	public static final String PERMISSION_REGEX = "^(?:[a-zA-Z]+)(?::[a-zA-Z]+){0,2}$";

	/**
	 * 以字母开头，包含字母、数字、下划线，长度4-20位
	 */
	public static final String USERNAME_REGEX = "^[a-zA-Z][a-zA-Z0-9_]{3,19}$";

	private final String pattern;

	private final String desc;

	RegexConstantsEnum(String pattern, String desc) {
		this.pattern = pattern;
		this.desc = desc;
	}

}