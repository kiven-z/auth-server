package com.auth.service.auth.authentication.application.port;

/**
 * 「记住我」与 Refresh Cookie / 响应 readMeDay 策略端口。
 *
 * @author Bunny
 */
public interface RememberMePolicyPort {

	/**
	 * 解析 Refresh Cookie 的 Max-Age（秒）。
	 * @param rememberMe 是否记住登录
	 * @return Cookie Max-Age（秒）
	 */
	long resolveMaxAgeSeconds(boolean rememberMe);

	/**
	 * 解析响应中的「阅读/记住」天数（与前端 multiple-tabs expires 对齐）。
	 * @return 天数（不足一天按 1 天计）
	 */
	long resolveReadMeDay();

}
