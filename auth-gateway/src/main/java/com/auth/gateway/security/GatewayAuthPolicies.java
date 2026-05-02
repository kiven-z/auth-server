package com.auth.gateway.security;

import lombok.experimental.UtilityClass;

import static com.auth.gateway.security.GatewayAuthDecision.InvalidTokenAction.ALLOW_ANONYMOUS;
import static com.auth.gateway.security.GatewayAuthDecision.InvalidTokenAction.REJECT;

/**
 * 内置策略集合（无状态）
 *
 * @author Bunny
 */
@UtilityClass
public class GatewayAuthPolicies {

	/**
	 * 根据模式获取策略
	 * @param mode 模式
	 * @return 策略
	 */
	public static GatewayAuthPolicy forMode(GatewayAuthMode mode) {
		return switch (mode) {
			// 可选凭证
			case OPTIONAL -> (path, isPermit, hasToken) -> new GatewayAuthDecision(false, hasToken, ALLOW_ANONYMOUS);

			// 携带即必须有效
			case FAIL_ON_INVALID_TOKEN ->
				(path, isPermit, hasToken) -> new GatewayAuthDecision(false, hasToken, REJECT);

			// 仅对受保护路径强制
			case ENFORCE_SECURED_ONLY -> (path, isPermit, hasToken) -> {
				if (isPermit) {
					// 公开路径：允许匿名；若携带 token 则校验但失败降级匿名
					return new GatewayAuthDecision(false, hasToken, ALLOW_ANONYMOUS);
				}
				// 受保护路径：必须携带并且必须有效
				return new GatewayAuthDecision(true, true, REJECT);
			};

			// 全局强制
			case ENFORCE -> (path, isPermit, hasToken) -> {
				if (isPermit) {
					// 公开路径：允许匿名；但一旦携带 token，则必须有效（防止“携带无效凭证”带来的不确定语义）
					return new GatewayAuthDecision(false, hasToken, REJECT);
				}
				// 受保护路径：必须携带并且必须有效
				return new GatewayAuthDecision(true, true, REJECT);
			};
		};
	}

}
