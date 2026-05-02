package com.auth.gateway.filter;

import com.auth.gateway.config.GatewaySecurityProperties;
import com.auth.gateway.exception.GatewayBusinessException;
import com.auth.gateway.exception.GatewayResultCodeEnum;
import com.auth.gateway.security.GatewayAuthMode;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.facade.SecurityTokenHandlerFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("JwtValidateGlobalFilter — gateway auth modes")
class JwtValidateGlobalFilterTest {

	/**
	 * 统一使用“空实现”的下游链路，专注验证：网关是否放行/拒绝、以及错误映射是否符合预期
	 */
	private static final GatewayFilterChain NOOP_CHAIN = ex -> Mono.empty();

	private static ServerWebExchange exchange(String url) {
		return MockServerWebExchange.from(MockServerHttpRequest.get(url).build());
	}

	private static ServerWebExchange exchange(String url, String authorization) {
		return MockServerWebExchange
			.from(MockServerHttpRequest.get(url).header(HttpHeaders.AUTHORIZATION, authorization).build());
	}

	private static ServerWebExchange exchange(String url, String headerName, String headerValue) {
		return MockServerWebExchange.from(MockServerHttpRequest.get(url).header(headerName, headerValue).build());
	}

	@Test
	@DisplayName("FAIL_ON_INVALID_TOKEN: public without token -> allow (匿名可达)")
	void failOnInvalidToken_public_withoutToken_shouldPass() {
		GatewaySecurityProperties props = new GatewaySecurityProperties();
		props.setMode(GatewayAuthMode.FAIL_ON_INVALID_TOKEN);
		props.setPermitPaths(List.of("/public/**"));
		SecurityTokenHandlerFacade facade = Mockito.mock(SecurityTokenHandlerFacade.class);
		JwtValidateGlobalFilter filter = new JwtValidateGlobalFilter(props, facade);

		StepVerifier.create(filter.filter(exchange("http://localhost/public/ping"), NOOP_CHAIN)).verifyComplete();
	}

	@Test
	@DisplayName("FAIL_ON_INVALID_TOKEN: public with invalid token -> reject (401 认证失败)")
	void failOnInvalidToken_public_withInvalidToken_shouldReject() {
		GatewaySecurityProperties props = new GatewaySecurityProperties();
		props.setMode(GatewayAuthMode.FAIL_ON_INVALID_TOKEN);
		props.setPermitPaths(List.of("/public/**"));
		SecurityTokenHandlerFacade facade = Mockito.mock(SecurityTokenHandlerFacade.class);
		when(facade.validateExternal(anyString()))
			.thenThrow(new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID, "bad"));
		JwtValidateGlobalFilter filter = new JwtValidateGlobalFilter(props, facade);

		StepVerifier.create(filter.filter(exchange("http://localhost/public/ping", "Bearer bad"), NOOP_CHAIN))
			.expectErrorMatches(t -> t instanceof GatewayBusinessException gbe
					&& GatewayResultCodeEnum.UNAUTHORIZED_TOKEN_INVALID.getError().equals(gbe.getError()))
			.verify();
	}

	@Test
	@DisplayName("FAIL_ON_INVALID_TOKEN: secured without token -> allow (网关无硬性 token 要求)")
	void failOnInvalidToken_secured_withoutToken_shouldPass() {
		GatewaySecurityProperties props = new GatewaySecurityProperties();
		props.setMode(GatewayAuthMode.FAIL_ON_INVALID_TOKEN);
		props.setPermitPaths(List.of("/public/**"));
		SecurityTokenHandlerFacade facade = Mockito.mock(SecurityTokenHandlerFacade.class);
		JwtValidateGlobalFilter filter = new JwtValidateGlobalFilter(props, facade);

		StepVerifier.create(filter.filter(exchange("http://localhost/api/users"), NOOP_CHAIN)).verifyComplete();
	}

	@Test
	@DisplayName("FAIL_ON_INVALID_TOKEN: secured with invalid token -> reject (401认证失败映射)")
	void failOnInvalidToken_secured_invalidToken_shouldMapInvalid() {
		GatewaySecurityProperties props = new GatewaySecurityProperties();
		props.setMode(GatewayAuthMode.FAIL_ON_INVALID_TOKEN);
		props.setPermitPaths(List.of());
		SecurityTokenHandlerFacade facade = Mockito.mock(SecurityTokenHandlerFacade.class);
		when(facade.validateExternal(anyString()))
			.thenThrow(new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID, "bad"));
		JwtValidateGlobalFilter filter = new JwtValidateGlobalFilter(props, facade);

		StepVerifier.create(filter.filter(exchange("http://localhost/api/users", "Bearer bad"), NOOP_CHAIN))
			.expectErrorMatches(t -> t instanceof GatewayBusinessException gbe
					&& GatewayResultCodeEnum.UNAUTHORIZED_TOKEN_INVALID.getError().equals(gbe.getError()))
			.verify();
	}

	@Test
	@DisplayName("FAIL_ON_INVALID_TOKEN: secured with expired token -> reject (401认证过期映射)")
	void failOnInvalidToken_secured_expiredToken_shouldMapExpired() {
		GatewaySecurityProperties props = new GatewaySecurityProperties();
		props.setMode(GatewayAuthMode.FAIL_ON_INVALID_TOKEN);
		props.setPermitPaths(List.of());
		SecurityTokenHandlerFacade facade = Mockito.mock(SecurityTokenHandlerFacade.class);
		when(facade.validateExternal(anyString()))
			.thenThrow(new SecurityTokenException(SecurityResultCodeEnum.TOKEN_EXPIRED, "expired"));
		JwtValidateGlobalFilter filter = new JwtValidateGlobalFilter(props, facade);

		StepVerifier.create(filter.filter(exchange("http://localhost/api/users", "Bearer expired"), NOOP_CHAIN))
			.expectErrorMatches(t -> t instanceof GatewayBusinessException gbe
					&& GatewayResultCodeEnum.UNAUTHORIZED_TOKEN_EXPIRED.getError().equals(gbe.getError()))
			.verify();
	}

	@Test
	@DisplayName("ENFORCE_SECURED_ONLY: secured without token -> reject (401 认证缺失)")
	void enforceSecuredOnly_secured_withoutToken_shouldRejectMissing() {
		GatewaySecurityProperties props = new GatewaySecurityProperties();
		props.setMode(GatewayAuthMode.ENFORCE_SECURED_ONLY);
		props.setPermitPaths(List.of("/public/**"));
		SecurityTokenHandlerFacade facade = Mockito.mock(SecurityTokenHandlerFacade.class);
		JwtValidateGlobalFilter filter = new JwtValidateGlobalFilter(props, facade);

		StepVerifier.create(filter.filter(exchange("http://localhost/api/users"), NOOP_CHAIN))
			.expectErrorMatches(t -> t instanceof GatewayBusinessException gbe
					&& GatewayResultCodeEnum.UNAUTHORIZED_TOKEN_MISSING.getError().equals(gbe.getError()))
			.verify();
	}

	@Test
	@DisplayName("ENFORCE_SECURED_ONLY: public with invalid token -> allow (匿名降级)")
	void enforceSecuredOnly_public_invalidToken_shouldPass() {
		GatewaySecurityProperties props = new GatewaySecurityProperties();
		props.setMode(GatewayAuthMode.ENFORCE_SECURED_ONLY);
		props.setPermitPaths(List.of("/public/**"));
		SecurityTokenHandlerFacade facade = Mockito.mock(SecurityTokenHandlerFacade.class);
		when(facade.validateExternal(anyString()))
			.thenThrow(new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID, "bad"));
		JwtValidateGlobalFilter filter = new JwtValidateGlobalFilter(props, facade);

		StepVerifier.create(filter.filter(exchange("http://localhost/public/ping", "Bearer bad"), NOOP_CHAIN))
			.verifyComplete();
	}

	@Test
	@DisplayName("OPTIONAL: secured with invalid token -> allow (匿名降级)")
	void optional_secured_invalidToken_shouldPass() {
		GatewaySecurityProperties props = new GatewaySecurityProperties();
		props.setMode(GatewayAuthMode.OPTIONAL);
		props.setPermitPaths(List.of("/public/**"));
		SecurityTokenHandlerFacade facade = Mockito.mock(SecurityTokenHandlerFacade.class);
		when(facade.validateExternal(anyString()))
			.thenThrow(new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID, "bad"));
		JwtValidateGlobalFilter filter = new JwtValidateGlobalFilter(props, facade);

		StepVerifier.create(filter.filter(exchange("http://localhost/api/users", "Bearer bad"), NOOP_CHAIN))
			.verifyComplete();
		Mockito.verify(facade, Mockito.times(1)).validateExternal(anyString());
	}

	@Test
	@DisplayName("OFF: secured with invalid token -> allow and do not call validator(不调用校验器)")
	void off_secured_invalidToken_shouldPassWithoutCallingFacade() {
		GatewaySecurityProperties props = new GatewaySecurityProperties();
		props.setPermitPaths(List.of());
		SecurityTokenHandlerFacade facade = Mockito.mock(SecurityTokenHandlerFacade.class);
		JwtValidateGlobalFilter filter = new JwtValidateGlobalFilter(props, facade);

		StepVerifier.create(filter.filter(exchange("http://localhost/api/users"), NOOP_CHAIN)).verifyComplete();
		Mockito.verify(facade, Mockito.never()).validateExternal(anyString());
	}

	@Test
	@DisplayName("INTERNAL_PATH: internal path without X-Internal-JWT -> reject (404 route not found)")
	void internalPath_withoutInternalJwt_shouldRejectRouteNotFound() {
		GatewaySecurityProperties props = new GatewaySecurityProperties();
		props.setMode(GatewayAuthMode.ENFORCE_SECURED_ONLY);
		props.setInternalPaths(List.of("/api/**/inner/**"));
		props.setPermitPaths(List.of());
		SecurityTokenHandlerFacade facade = Mockito.mock(SecurityTokenHandlerFacade.class);
		JwtValidateGlobalFilter filter = new JwtValidateGlobalFilter(props, facade);

		StepVerifier.create(filter.filter(exchange("http://localhost/api/auth/inner/cache/refresh"), NOOP_CHAIN))
			.expectErrorMatches(t -> t instanceof GatewayBusinessException gbe
					&& GatewayResultCodeEnum.ROUTE_NOT_FOUND.getError().equals(gbe.getError()))
			.verify();
		Mockito.verify(facade, Mockito.never()).validateExternal(anyString());
	}

	@Test
	@DisplayName("INTERNAL_PATH: internal path with X-Internal-JWT -> pass without external bearer validation")
	void internalPath_withInternalJwt_shouldPassWithoutExternalValidation() {
		GatewaySecurityProperties props = new GatewaySecurityProperties();
		props.setMode(GatewayAuthMode.ENFORCE_SECURED_ONLY);
		props.setInternalPaths(List.of("/api/**/inner/**"));
		props.setPermitPaths(List.of());
		SecurityTokenHandlerFacade facade = Mockito.mock(SecurityTokenHandlerFacade.class);
		JwtValidateGlobalFilter filter = new JwtValidateGlobalFilter(props, facade);

		ServerWebExchange ex = exchange("http://localhost/api/auth/inner/cache/refresh",
				SecurityInternalTokenConstants.INTERNAL_HEADER, "dummy.internal.jwt");

		StepVerifier.create(filter.filter(ex, NOOP_CHAIN)).verifyComplete();
		Mockito.verify(facade, Mockito.never()).validateExternal(anyString());
	}

}
