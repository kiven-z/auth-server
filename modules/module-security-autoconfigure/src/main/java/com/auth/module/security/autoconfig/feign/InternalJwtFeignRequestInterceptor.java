package com.auth.module.security.autoconfig.feign;

import com.auth.module.security.autoconfig.web.SecurityUserUtils;
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
 * 双模签发：
 * <ul>
 * <li>当前线程已持有 AuthProfile 且 userId 不为空 → 用户身份令牌</li>
 * <li>其余情况（如调度器/消息消费等无登录用户的内部互调）→ 服务身份令牌（sub 占位 + service_id claim）</li>
 * </ul>
 * 不要将内部令牌传播回客户端
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
	 * 应用拦截器：剥离外部 {@code Authorization}；按当前是否存在用户上下文决定签发用户/服务身份内部令牌
	 * @param template 请求模板
	 */
	@Override
	public void apply(RequestTemplate template) {
		template.removeHeader(HttpHeaders.AUTHORIZATION);

		// 当前用户的 jti
		String jti = UUID.randomUUID().toString();
		Long userId = SecurityUserUtils.getUserId();

		// 构建内部Token 如果不存在用户使用服务的Id进行构建
		String token = (userId != null) ? internalTokenProvider.buildToken(userId, jti)
				: internalTokenProvider.buildServiceToken(serviceId, jti);

		template.header(INTERNAL_HEADER, token);
	}

}
