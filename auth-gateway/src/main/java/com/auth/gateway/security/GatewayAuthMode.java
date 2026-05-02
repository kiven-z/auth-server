package com.auth.gateway.security;

/**
 * 网关鉴权模式
 *
 * <p>
 * 该枚举仅定义网关在“是否要求携带 Token、是否校验 Token、Token 无效如何处理”等方面的行为语义， 不依赖下游服务的 Controller
 * 注解元数据（网关不可见）
 * </p>
 *
 * @author Bunny
 */
public enum GatewayAuthMode {

	/**
	 * 携带即必须有效：若携带 Authorization，则必须校验通过；否则 401 若未携带 Authorization，则不因网关策略而拒绝
	 */
	FAIL_ON_INVALID_TOKEN,

	/**
	 * 仅对受保护路径强制：非 permitPaths 要求必须携带有效 Token； permitPaths 一律放行（可选校验失败降级匿名，由策略定义）
	 */
	ENFORCE_SECURED_ONLY,

	/**
	 * 全局强制：非 permitPaths 要求必须携带有效 Token； permitPaths 若携带 Token 也必须有效，否则 401
	 */
	ENFORCE,

	/**
	 * 可选凭证：携带则尽力校验；失败降级匿名放行；不携带也放行
	 */
	OPTIONAL,

}
