package com.auth.module.security.contract.constants;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * 编译时正则表达式共享 用于在编译时检查SQL标识符是否正确
 *
 * @author Bunny
 */
@UtilityClass
public class SqlIdentifierPatterns {

	/**
	 * 标识符：以字母或下划线开头，后跟单词字符（{@code [A-Za-z0-9_]}）。
	 * <p>
	 * 适用于使用{@link Pattern#compile(String)}匹配表或列令牌时。
	 * </p>
	 */
	public static final String SAFE_IDENTIFIER = "^[A-Za-z_]\\w*$";

}
