package com.auth.gateway.support;

import com.auth.gateway.exception.GatewayResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import lombok.experimental.UtilityClass;

/**
 * 将安全模块的 Token 异常映射为网关错误码
 *
 * @author Bunny
 */
@UtilityClass
public class SecurityTokenExceptionToGatewayResultCodeMapper {

	/**
	 * 将安全模块的 Token 异常映射为网关错误码
	 * @param ex 安全模块的 Token 异常
	 * @return 网关错误码
	 */
	public static GatewayResultCodeEnum map(SecurityTokenException ex) {
		if (ex == null || ex.getResultCode() == null) {
			return GatewayResultCodeEnum.UNAUTHORIZED_TOKEN_INVALID;
		}
		return switch (ex.getResultCode()) {
			case TOKEN_MISSING -> GatewayResultCodeEnum.UNAUTHORIZED_TOKEN_MISSING;
			case TOKEN_EXPIRED -> GatewayResultCodeEnum.UNAUTHORIZED_TOKEN_EXPIRED;
			case REDIS_UNAVAILABLE, PROFILE_DESERIALIZE_FAILED -> GatewayResultCodeEnum.INTERNAL_ERROR;
			default -> GatewayResultCodeEnum.UNAUTHORIZED_TOKEN_INVALID;
		};
	}

}
