package com.auth.gateway.security;

/**
 * 网关鉴权策略：根据请求上下文做出决策
 *
 * <p>
 * 策略实现不得依赖下游服务注解元数据；其输入仅来自网关可观测信息（路径、header、是否命中 permitPaths）
 * </p>
 *
 * @author Bunny
 */
public interface GatewayAuthPolicy {

	/**
	 * 网关鉴权策略决策
	 * @param path 请求路径
	 * @param isPermit 是否命中 permitPaths
	 * @param hasToken 是否携带 Authorization
	 * @return 决策
	 */
	GatewayAuthDecision decide(String path, boolean isPermit, boolean hasToken);

}
