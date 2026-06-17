package com.auth.module.security.autoconfigure.feign;

import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.authorization.AuthProfile;
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
 * 智能签发策略：
 * <ul>
 * <li>有用户上下文：签发用户身份令牌（principal_type=USER），将用户信息透传给下游服务</li>
 * <li>无用户上下文：签发服务身份令牌（principal_type=SERVICE），用于定时任务、MQ消费者等场景</li>
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
	 * 应用拦截器：剥离外部 {@code Authorization}，并根据当前上下文智能签发内部令牌
	 * @param template 请求模板
	 */
	@Override
	public void apply(RequestTemplate template) {
		template.removeHeader(HttpHeaders.AUTHORIZATION);
		template.removeHeader(INTERNAL_HEADER);

		String jti = UUID.randomUUID().toString();
		String token;

		// 尝试从当前 SecurityContext 获取用户信息
		AuthProfile currentUser = SecurityUserUtils.currentAuthProfile();

		if (currentUser != null && currentUser.getUserId() != null) {
			// 场景：用户请求 → Service A → Service B（内部调用）
			// 将用户身份透传给下游服务，支持数据权限等功能
			token = internalTokenProvider.buildToken(currentUser.getUserId(), jti, currentUser.getPermVersion());
		}
		else {
			// 场景：定时任务、MQ消费者等无用户上下文的服务间调用
			// 使用服务身份令牌
			token = internalTokenProvider.buildServiceToken(serviceId, jti);
		}

		template.header(INTERNAL_HEADER, token);
	}

}
