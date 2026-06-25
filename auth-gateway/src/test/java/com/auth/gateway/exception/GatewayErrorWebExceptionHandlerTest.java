package com.auth.gateway.exception;

import com.auth.common.core.model.response.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link GatewayErrorWebExceptionHandler} 错误映射单元测试
 */
@DisplayName("GatewayErrorWebExceptionHandler")
class GatewayErrorWebExceptionHandlerTest {

	@Test
	@DisplayName("GatewayBusinessException 保留其 resultCode")
	void businessException_preservesResultCode() {
		GatewayBusinessException gbe = new GatewayBusinessException(GatewayResultCodeEnum.UNAUTHORIZED);
		GatewayResultCodeEnum rc = gbe.getResultCode();

		assertEquals(GatewayResultCodeEnum.UNAUTHORIZED, rc);
		Result<Object> body = Result.error(rc.fullBizCode(), rc.getError(), rc.getMessage());
		assertEquals(GatewayResultCodeEnum.UNAUTHORIZED.fullBizCode(), body.getCode());
		assertEquals(GatewayResultCodeEnum.UNAUTHORIZED.getError(), body.getError());
		assertEquals(GatewayResultCodeEnum.UNAUTHORIZED.getMessage(), body.getMessage());
	}

	@Test
	@DisplayName("非 GatewayBusinessException 回退为 INTERNAL_ERROR")
	void nonGatewayException_fallsBackToInternalError() {
		GatewayResultCodeEnum rc = GatewayResultCodeEnum.INTERNAL_ERROR;

		assertEquals(GatewayResultCodeEnum.INTERNAL_ERROR, rc);
		Result<Object> body = Result.error(rc.fullBizCode(), rc.getError(), rc.getMessage());
		assertEquals("GATEWAY_INTERNAL_ERROR", body.getError());
		assertEquals("Gateway internal error.", body.getMessage());
	}

}
