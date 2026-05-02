package com.auth.gateway.filter;

import com.auth.gateway.config.GatewaySecurityProperties;
import com.auth.gateway.exception.GatewayBusinessException;
import com.auth.gateway.exception.GatewayResultCodeEnum;
import com.auth.gateway.security.GatewayAuthDecision;
import com.auth.gateway.security.GatewayAuthPolicies;
import com.auth.gateway.support.SecurityTokenExceptionToGatewayResultCodeMapper;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.facade.SecurityTokenHandlerFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.auth.gateway.security.GatewayAuthDecision.InvalidTokenAction.ALLOW_ANONYMOUS;
import static com.auth.module.security.core.matcher.AntPathMatcher.matchesAnyAnt;

/**
 * 网关 JWT 校验：permitPaths 放行；非 permit 必须携带合法外部 Token
 *
 * <p>
 * 注意：网关无法识别下游的 {@code @PublicApi}，因此只能按路径配置决定是否拦截
 * </p>
 *
 * @author Bunny
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Component
public class JwtValidateGlobalFilter implements GlobalFilter {

	private final GatewaySecurityProperties properties;

	/**
	 * 认证模块的 Token 校验
	 */
	private final SecurityTokenHandlerFacade tokenFacade;

	public JwtValidateGlobalFilter(GatewaySecurityProperties properties, SecurityTokenHandlerFacade tokenFacade) {
		this.properties = properties;
		this.tokenFacade = tokenFacade;
	}

	/**
	 * 网关 JWT 校验
	 * @param exchange the current server exchange
	 * @param chain provides a way to delegate to the next filter
	 * @return Mono
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		if (!properties.isEnabled()) {
			return chain.filter(exchange);
		}
		ServerHttpRequest request = exchange.getRequest();
		String path = request.getURI().getPath();

		// 内部路径检查：必须携带内部 JWT
		Mono<Void> internalCheck = handleInternalPath(request, path, exchange, chain);
		if (internalCheck != null) {
			return internalCheck;
		}

		// 构建鉴权决策
		String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		boolean hasToken = StringUtils.hasText(authorization);
		GatewayAuthDecision decision = buildAuthDecision(path, hasToken);

		// 决策要求 Token 但未携带
		if (decision.requireToken() && !hasToken) {
			return Mono.error(new GatewayBusinessException(GatewayResultCodeEnum.UNAUTHORIZED_TOKEN_MISSING));
		}

		// 决策不要求校验 Token
		if (!decision.validateToken()) {
			return chain.filter(exchange);
		}

		// 校验外部 Token（含异常降级处理）
		return validateExternalToken(authorization, decision, exchange, chain);
	}

	/**
	 * 内部路径处理：必须携带内部 JWT，否则返回 404
	 * @param request 当前请求
	 * @param path 请求路径
	 * @param exchange the current server exchange
	 * @param chain provides a way to delegate to the next filter
	 * @return 已处理的 Mono，或 null 表示非内部路径需继续执行
	 */
	private Mono<Void> handleInternalPath(ServerHttpRequest request, String path, ServerWebExchange exchange,
			GatewayFilterChain chain) {
		if (!matchesAnyAnt(path, properties.getInternalPaths())) {
			return null;
		}
		String internalJwt = request.getHeaders().getFirst(SecurityInternalTokenConstants.INTERNAL_HEADER);
		if (!StringUtils.hasText(internalJwt)) {
			return Mono.error(new GatewayBusinessException(GatewayResultCodeEnum.ROUTE_NOT_FOUND));
		}
		return chain.filter(exchange);
	}

	/**
	 * 根据路径和 Token 存在性构建鉴权决策
	 * @param path 请求路径
	 * @param hasToken 是否携带 Authorization
	 * @return 鉴权决策
	 */
	private GatewayAuthDecision buildAuthDecision(String path, boolean hasToken) {
		boolean isPermit = matchesAnyAnt(path, properties.getPermitPaths());
		return GatewayAuthPolicies.forMode(properties.getMode()).decide(path, isPermit, hasToken);
	}

	/**
	 * 校验外部 Token，校验失败时按决策进行降级或拒绝
	 * @param authorization Authorization header
	 * @param decision 鉴权决策
	 * @param exchange the current server exchange
	 * @param chain provides a way to delegate to the next filter
	 * @return Mono
	 */
	private Mono<Void> validateExternalToken(String authorization, GatewayAuthDecision decision,
			ServerWebExchange exchange, GatewayFilterChain chain) {
		try {
			tokenFacade.validateExternal(authorization);
		}
		catch (SecurityTokenException ex) {
			if (decision.invalidTokenAction() == ALLOW_ANONYMOUS) {
				log.debug("Token validate failed but allowed by mode: mode={}, code={}, path={}", properties.getMode(),
						ex.getCode(), exchange.getRequest().getURI().getPath());
				return chain.filter(exchange);
			}
			return Mono.error(new GatewayBusinessException(SecurityTokenExceptionToGatewayResultCodeMapper.map(ex)));
		}
		return chain.filter(exchange);
	}

}
