package com.auth.gateway.security;

/**
 * 网关鉴权决策（过滤器编排的最小输出）
 *
 * @param requireToken 是否要求必须携带 Token；若为 true 且未携带则应拒绝
 * @param validateToken 是否需要校验 Token；若为 true 且校验失败，按 invalidTokenAction 处理
 * @param invalidTokenAction Token 无效时动作
 * @author Bunny
 */
public record GatewayAuthDecision(boolean requireToken, boolean validateToken, InvalidTokenAction invalidTokenAction) {
	public enum InvalidTokenAction {

		/**
		 * Token 无效立即拒绝（fail-closed）
		 */
		REJECT,
		/**
		 * Token 无效降级为匿名继续（fail-open / anonymous fallback）
		 */
		ALLOW_ANONYMOUS

	}
}
