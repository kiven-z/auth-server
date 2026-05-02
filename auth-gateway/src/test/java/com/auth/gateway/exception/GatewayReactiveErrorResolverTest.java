package com.auth.gateway.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link GatewayReactiveErrorResolver} 单元测试
 */
@DisplayName("GatewayReactiveErrorResolver")
class GatewayReactiveErrorResolverTest {

	@Test
	@DisplayName("直接 GatewayBusinessException：返回对应枚举与 i18n 参数")
	void resolve_directGatewayBusinessException_returnsEnumAndArgs() {
		// 带 i18n 占位参数时，解析结果应保留顺序与内容
		Object[] args = { "t1", "e1" };
		GatewayBusinessException ex = new GatewayBusinessException(GatewayResultCodeEnum.INTERNAL_ERROR, args);

		GatewayReactiveErrorResolver.GatewayReactiveErrorResolution resolution = GatewayReactiveErrorResolver
			.resolve(ex);

		assertSame(GatewayResultCodeEnum.INTERNAL_ERROR, resolution.resultCode());
		assertEquals(List.of("t1", "e1"), resolution.i18nArgs());
	}

	@Test
	@DisplayName("包装在 cause 链上的 GatewayBusinessException：仍能解析")
	void resolve_wrappedGatewayBusinessException_unwraps() {
		// Spring 等框架可能用外层异常包装业务异常
		GatewayBusinessException inner = new GatewayBusinessException(GatewayResultCodeEnum.UNAUTHORIZED_TOKEN_MISSING);
		RuntimeException wrapper = new RuntimeException("wrap", inner);

		GatewayReactiveErrorResolver.GatewayReactiveErrorResolution resolution = GatewayReactiveErrorResolver
			.resolve(wrapper);

		assertSame(GatewayResultCodeEnum.UNAUTHORIZED_TOKEN_MISSING, resolution.resultCode());
		assertEquals(List.of(), resolution.i18nArgs());
	}

	@Test
	@DisplayName("NotFoundException：映射为 ROUTE_NOT_FOUND")
	void resolve_notFoundException_mapsRouteNotFound() {
		// 网关路由未匹配时的框架异常
		GatewayReactiveErrorResolver.GatewayReactiveErrorResolution resolution = GatewayReactiveErrorResolver
			.resolve(new NotFoundException("no route"));

		assertSame(GatewayResultCodeEnum.ROUTE_NOT_FOUND, resolution.resultCode());
		assertEquals(List.of(), resolution.i18nArgs());
	}

	@Test
	@DisplayName("ResponseStatusException 404：映射为 ROUTE_NOT_FOUND")
	void resolve_responseStatusNotFound_mapsRouteNotFound() {
		// WebFlux 常见 404 表达
		ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND);

		GatewayReactiveErrorResolver.GatewayReactiveErrorResolution resolution = GatewayReactiveErrorResolver
			.resolve(ex);

		assertSame(GatewayResultCodeEnum.ROUTE_NOT_FOUND, resolution.resultCode());
	}

	@Test
	@DisplayName("cause 链含 TimeoutException：映射为 UPSTREAM_TIMEOUT")
	void resolve_timeoutInCause_mapsUpstreamTimeout() {
		// 上游超时往往在嵌套 cause 中
		Throwable root = new RuntimeException("outer", new RuntimeException("mid", new TimeoutException("t")));

		GatewayReactiveErrorResolver.GatewayReactiveErrorResolution resolution = GatewayReactiveErrorResolver
			.resolve(root);

		assertSame(GatewayResultCodeEnum.UPSTREAM_TIMEOUT, resolution.resultCode());
	}

	@Test
	@DisplayName("cause 链含 ConnectException：映射为 UPSTREAM_CONNECT_FAILED")
	void resolve_connectExceptionInCause_mapsUpstreamConnectFailed() {
		// 上游拒绝连接
		Throwable root = new RuntimeException("outer", new ConnectException("refused"));

		GatewayReactiveErrorResolver.GatewayReactiveErrorResolution resolution = GatewayReactiveErrorResolver
			.resolve(root);

		assertSame(GatewayResultCodeEnum.UPSTREAM_CONNECT_FAILED, resolution.resultCode());
	}

	@Test
	@DisplayName("未知异常：映射为 INTERNAL_ERROR")
	void resolve_unknown_mapsInternalError() {
		// 无专门映射时兜底
		GatewayReactiveErrorResolver.GatewayReactiveErrorResolution resolution = GatewayReactiveErrorResolver
			.resolve(new IllegalStateException("x"));

		assertSame(GatewayResultCodeEnum.INTERNAL_ERROR, resolution.resultCode());
		assertEquals(List.of(), resolution.i18nArgs());
	}

	@Test
	@DisplayName("findGatewayBusinessException：无匹配时返回 null")
	void findGatewayBusinessException_none_returnsNull() {
		// 非网关业务异常
		assertNull(GatewayReactiveErrorResolver.findGatewayBusinessException(new IllegalStateException()));
	}

	@Test
	@DisplayName("findGatewayBusinessException：首层即匹配")
	void findGatewayBusinessException_direct_returnsSame() {
		// Filter 直接抛出时通常无包装
		GatewayBusinessException ex = new GatewayBusinessException(GatewayResultCodeEnum.ROUTE_NOT_FOUND);

		assertSame(ex, GatewayReactiveErrorResolver.findGatewayBusinessException(ex));
	}

}
