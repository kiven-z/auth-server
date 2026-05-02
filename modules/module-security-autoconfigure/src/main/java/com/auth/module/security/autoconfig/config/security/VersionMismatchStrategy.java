package com.auth.module.security.autoconfig.config.security;

/**
 * 版本不匹配策略 用于处理“用户权限快照/权限版本号”与服务端当前权限版本不一致的情况 不同策略会影响客户端的下一步动作（例如提示刷新权限、拒绝访问或强制重新登录）
 *
 * @author Bunny
 */
public enum VersionMismatchStrategy {

	/**
	 * 提示客户端刷新权限信息（语义化名称） 实现层通常会返回一个“需要刷新”的信号（例如 409），由客户端发起刷新
	 */
	REQUIRE_REFRESH,

	/**
	 * 拒绝访问（语义化名称） 实现层通常会返回 403
	 */
	DENY,

	/**
	 * 强制重新登录（语义化名称） 实现层通常会返回 401，并清理会话/Token
	 */
	FORCE_REAUTH,

}