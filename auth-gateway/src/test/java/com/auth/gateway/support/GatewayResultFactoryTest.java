package com.auth.gateway.support;

import com.auth.gateway.exception.GatewayResultCodeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link GatewayResultFactory#resolveHttpStatus} 单元测试
 */
@DisplayName("GatewayResultFactory")
class GatewayResultFactoryTest {

	@Test
	@DisplayName("resolveHttpStatus：路由未找到 -> 404")
	void resolveHttpStatus_routeNotFound() {
		assertEquals(HttpStatus.NOT_FOUND,
				GatewayResultFactory.resolveHttpStatus(GatewayResultCodeEnum.ROUTE_NOT_FOUND));
	}

	@Test
	@DisplayName("resolveHttpStatus：上游超时 -> 504")
	void resolveHttpStatus_upstreamTimeout() {
		assertEquals(HttpStatus.GATEWAY_TIMEOUT,
				GatewayResultFactory.resolveHttpStatus(GatewayResultCodeEnum.UPSTREAM_TIMEOUT));
	}

	@Test
	@DisplayName("resolveHttpStatus：网关内部错误 -> 500")
	void resolveHttpStatus_internalError() {
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
				GatewayResultFactory.resolveHttpStatus(GatewayResultCodeEnum.INTERNAL_ERROR));
	}

}
