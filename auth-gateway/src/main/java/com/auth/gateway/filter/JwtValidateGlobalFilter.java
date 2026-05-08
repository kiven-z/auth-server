package com.auth.gateway.filter;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.gateway.exception.GatewayBusinessException;
import com.auth.gateway.support.SecurityTokenExceptionToGatewayResultCodeMapper;
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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关 JWT 轻量校验：仅在携带 Authorization 时校验“可解析”
 *
 * <p>
 * 过期语义由下游服务处理，网关不做会话/权限业务判断
 * </p>
 *
 * @author Bunny
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Component
public class JwtValidateGlobalFilter implements GlobalFilter {

    /**
     * 认证模块的 Token 校验
     */
    private final SecurityTokenHandlerFacade tokenFacade;

    public JwtValidateGlobalFilter(SecurityTokenHandlerFacade tokenFacade) {
        this.tokenFacade = tokenFacade;
    }

    /**
     * 网关 JWT 校验
     *
     * @param exchange the current server exchange
     * @param chain    provides a way to delegate to the next filter
     * @return Mono
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 外部Token轻量检查：仅当 Authorization 存在时进行可解析校验
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (CharSequenceUtil.isNotBlank(authorization)) {
            return chain.filter(exchange);
        }
        return validateExternalToken(authorization, exchange, chain);
    }

    /**
     * 校验外部 Token 可解析性（允许过期）
     *
     * @param authorization Authorization header
     * @param exchange      the current server exchange
     * @param chain         provides a way to delegate to the next filter
     * @return Mono
     */
    private Mono<Void> validateExternalToken(String authorization, ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            tokenFacade.parseExternalAccessAllowExpired(authorization);
        } catch (SecurityTokenException ex) {
            log.debug("Gateway token parse failed: code={}, path={}", ex.getCode(),
                    exchange.getRequest().getURI().getPath());
            return Mono.error(new GatewayBusinessException(SecurityTokenExceptionToGatewayResultCodeMapper.map(ex)));
        }
        return chain.filter(exchange);
    }

}
