package com.auth.module.security.autoconfigure.feign;

import com.auth.module.security.core.token.provider.InternalTokenProvider;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.UUID;

import static com.auth.module.security.contract.constants.SecurityInternalTokenConstants.INTERNAL_HEADER;

/**
 * Feign 拦截器：出站为服务间调用签发短生命周期内部 JWT，并清除外部 {@code Authorization}，避免误传播
 *
 * <p>
 * 固定签发服务身份内部令牌（sub 占位 + service_id claim） 不要将内部令牌传播回客户端
 *
 * @author Bunny
 */
public final class InternalJwtFeignRequestInterceptor implements RequestInterceptor {

	private final InternalTokenProvider internalTokenProvider;

	private final String serviceId;

	/**
	 * 构造函数
	 * @param internalTokenProvider 内部令牌提供者
	 * @param serviceId 服务名（一般来自 {@code spring.application.name}）
	 */
	public InternalJwtFeignRequestInterceptor(InternalTokenProvider internalTokenProvider, String serviceId) {
		this.internalTokenProvider = internalTokenProvider;
		this.serviceId = serviceId;
	}

	/**
	 * 应用拦截器：剥离外部 {@code Authorization}，并签发服务身份内部令牌
	 * @param template 请求模板
	 */
	@Override
	public void apply(RequestTemplate template) {
		template.removeHeader(HttpHeaders.AUTHORIZATION);
		template.removeHeader(INTERNAL_HEADER);

		// 每次内部调用都签发新的服务身份令牌
		String jti = UUID.randomUUID().toString();
		String token = internalTokenProvider.buildServiceToken(serviceId, jti);

		template.header(INTERNAL_HEADER, token);
	}

}
