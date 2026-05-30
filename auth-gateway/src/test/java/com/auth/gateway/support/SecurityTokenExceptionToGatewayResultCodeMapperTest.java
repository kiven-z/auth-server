package com.auth.gateway.support;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.auth.gateway.exception.GatewayResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;

class SecurityTokenExceptionToGatewayResultCodeMapperTest {

	@Test
	@DisplayName("测试会话错误映射到网关会话子码")
	void map_sessionErrors_shouldReturnSessionGatewayCode() {
		GatewayResultCodeEnum mapped = SecurityTokenExceptionToGatewayResultCodeMapper
			.map(new SecurityTokenException(SecurityResultCodeEnum.SESSION_MISSING, "session missing"));

		assertEquals(GatewayResultCodeEnum.UNAUTHORIZED_SESSION_MISSING, mapped);
	}

	@Test
	@DisplayName("测试权限版本策略映射到网关权限版本子码")
	void map_permissionVersionStrategyErrors_shouldReturnGatewayStrategyCode() {
		GatewayResultCodeEnum mapped = SecurityTokenExceptionToGatewayResultCodeMapper
			.map(new SecurityTokenException(SecurityResultCodeEnum.PERMISSION_VERSION_MISMATCH, "mismatch"));

		assertEquals(GatewayResultCodeEnum.UNAUTHORIZED_PERMISSION_VERSION_MISMATCH, mapped);
	}

}
