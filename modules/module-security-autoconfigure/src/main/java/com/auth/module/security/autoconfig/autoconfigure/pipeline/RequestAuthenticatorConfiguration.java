package com.auth.module.security.autoconfig.autoconfigure.pipeline;

import com.auth.module.security.autoconfig.config.security.SecurityConfigProperties;
import com.auth.module.security.autoconfig.pipeline.authenticate.ExternalRequestAuthenticator;
import com.auth.module.security.autoconfig.pipeline.authenticate.InternalRequestAuthenticator;
import com.auth.module.security.autoconfig.pipeline.authenticate.RequestAuthenticator;
import com.auth.module.security.autoconfig.pipeline.authenticate.SessionCountChecker;
import com.auth.module.security.autoconfig.service.AuthProfileCacheService;
import com.auth.module.security.core.token.facade.SecurityTokenHandlerFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 请求认证器装配：内部/外部请求的认证入口。
 *
 * @author Bunny
 */
@Configuration
class RequestAuthenticatorConfiguration {

	@Bean
	InternalRequestAuthenticator internalRequestAuthenticator(SecurityTokenHandlerFacade tokenHandlerFacade,
			AuthProfileCacheService authProfileCacheService) {
		return new InternalRequestAuthenticator(tokenHandlerFacade, authProfileCacheService);
	}

	@Bean
	ExternalRequestAuthenticator externalRequestAuthenticator(SecurityTokenHandlerFacade tokenHandlerFacade,
			AuthProfileCacheService authProfileCacheService, SessionCountChecker sessionCountChecker,
			SecurityConfigProperties securityConfigProperties) {
		return new ExternalRequestAuthenticator(tokenHandlerFacade, authProfileCacheService, sessionCountChecker,
				securityConfigProperties);
	}

	/**
	 * 认证器集合：按顺序尝试匹配请求来源并完成认证。
	 */
	@Bean
	List<RequestAuthenticator> requestAuthenticators(InternalRequestAuthenticator internalRequestAuthenticator,
			ExternalRequestAuthenticator externalRequestAuthenticator) {
		return List.of(internalRequestAuthenticator, externalRequestAuthenticator);
	}

}
