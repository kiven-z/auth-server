package com.auth.module.security.autoconfigure.security;

/**
 * 安全要求枚举
 *
 * @author Bunny
 */
public enum SecurityRequirement {

	/**
	 * 公共接口 @PublicApi
	 */
	PUBLIC,

	/**
	 * 内部接口 @InternalApi
	 */
	INTERNAL,

	/**
	 * 认证接口 @AuthenticatedApi
	 */
	AUTHENTICATED,

	/**
	 * 无注解 / 取不到 handler
	 */
	FALLBACK_TO_PATH;

	/**
	 * {@link jakarta.servlet.http.HttpServletRequest#setAttribute(String, Object)} 使用的
	 * key， 供授权阶段读取 {@link SecurityRequirement}，使注解语义压过 {@code authorizeHttpRequests} 路径规则
	 */
	public static final String REQUEST_ATTRIBUTE = "com.auth.security.requirement";

}