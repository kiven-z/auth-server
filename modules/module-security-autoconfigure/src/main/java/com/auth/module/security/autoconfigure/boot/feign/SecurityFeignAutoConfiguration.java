package com.auth.module.security.autoconfigure.boot.feign;

import com.auth.module.security.autoconfigure.feign.InternalJwtFeignRequestInterceptor;
import com.auth.module.security.core.token.provider.InternalTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 出站与当前登录身份衔接的自动配置
 *
 * @author Bunny
 */
@ConditionalOnClass(name = "feign.RequestInterceptor")
@Configuration(proxyBeanMethods = false)
public class SecurityFeignAutoConfiguration {

	/**
	 * 注册 Feign 拦截器
	 * @param internalTokenProvider 内部令牌提供者
	 * @param serviceId 当前服务名（来自 {@code spring.application.name}），用于无用户上下文时签发服务身份令牌
	 * @return Feign 拦截器
	 */
	@ConditionalOnMissingBean
	@Bean
	InternalJwtFeignRequestInterceptor internalJwtFeignRequestInterceptor(InternalTokenProvider internalTokenProvider,
			@Value("${spring.application.name:unknown-service}") String serviceId) {
		return new InternalJwtFeignRequestInterceptor(internalTokenProvider, serviceId);
	}

}
