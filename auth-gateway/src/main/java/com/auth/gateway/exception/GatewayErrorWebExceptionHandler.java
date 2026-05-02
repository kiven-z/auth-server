package com.auth.gateway.exception;

import com.auth.common.core.model.response.Result;
import com.auth.gateway.support.GatewayResultFactory;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

/**
 * 响应式全局错误处理器，用于 Spring Cloud Gateway（GlobalFilter 等链路上的错误）。
 *
 * @author Bunny
 */
@Order(-2)
@Component
public class GatewayErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

	private final MessageSource messageSource;

	public GatewayErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties resources,
			ApplicationContext applicationContext, ServerCodecConfigurer serverCodecConfigurer,
			MessageSource messageSource) {
		super(errorAttributes, resources.getResources(), applicationContext);
		this.setMessageWriters(serverCodecConfigurer.getWriters());
		this.messageSource = messageSource;
	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
		return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
	}

	/**
	 * 渲染错误响应
	 * @param request 当前请求
	 * @return 响应 Mono
	 */
	private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
		Throwable ex = getError(request);
		GatewayBusinessException gatewayBusinessException = GatewayReactiveErrorResolver
			.findGatewayBusinessException(ex);
		if (gatewayBusinessException != null) {
			Result<Object> body = GatewayResultFactory.build(request.exchange(), messageSource,
					gatewayBusinessException);
			GatewayResultCodeEnum rc = gatewayBusinessException.getResultCode();
			return ServerResponse.status(GatewayResultFactory.resolveHttpStatus(rc))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body);
		}

		GatewayReactiveErrorResolver.GatewayReactiveErrorResolution resolution = GatewayReactiveErrorResolver
			.resolve(ex);
		GatewayResultCodeEnum rc = resolution.resultCode();
		Result<Object> body = GatewayResultFactory.build(request.exchange(), messageSource, rc,
				resolution.i18nArgs().toArray());
		return ServerResponse.status(GatewayResultFactory.resolveHttpStatus(rc))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(body);
	}

}
