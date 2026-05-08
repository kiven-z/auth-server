package com.auth.gateway.filter;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.jwt.exception.JwtExpiredException;
import com.auth.gateway.exception.GatewayBusinessException;
import com.auth.gateway.support.SecurityTokenExceptionToGatewayResultCodeMapper;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.provider.AccessTokenProvider;
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

    private final AccessTokenProvider accessTokenProvider;

    public JwtValidateGlobalFilter(AccessTokenProvider accessTokenProvider) {
        this.accessTokenProvider = accessTokenProvider;
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
        if (CharSequenceUtil.isBlank(authorization)) {
            return chain.filter(exchange);
        }

        try {
            // 解析外部访问令牌（允许过期）
            accessTokenProvider.parseToken(authorization);
        } catch (JwtExpiredException ex) {
            log.warn(ex.getMessage(), ex);
        } catch (SecurityTokenException ex) {
            log.debug("Gateway token parse failed: code={}, path={}", ex.getCode(),
                    exchange.getRequest().getURI().getPath());
            return Mono.error(new GatewayBusinessException(SecurityTokenExceptionToGatewayResultCodeMapper.map(ex)));
        }
        return chain.filter(exchange);
    }

}
