package com.auth.gateway.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link GatewayBusinessException} 单元测试
 */
@DisplayName("GatewayBusinessException")
class GatewayBusinessExceptionTest {

	@Test
	@DisplayName("构造后暴露枚举语义字段")
	void exposesEnumSemantics() {
		GatewayBusinessException ex = new GatewayBusinessException(GatewayResultCodeEnum.UNAUTHORIZED);

		assertEquals(GatewayResultCodeEnum.UNAUTHORIZED, ex.getResultCode());
		assertEquals(GatewayResultCodeEnum.UNAUTHORIZED.getHttpStatus(), ex.getHttpStatus());
		assertEquals(GatewayResultCodeEnum.UNAUTHORIZED.getError(), ex.getError());
		assertEquals(GatewayResultCodeEnum.UNAUTHORIZED.getError(), ex.getMessage());
	}

	@Test
	@DisplayName("resultCode 为 null 时抛出 IllegalArgumentException")
	void rejectsNullResultCode() {
		assertThrows(IllegalArgumentException.class, () -> new GatewayBusinessException(null));
	}

}
