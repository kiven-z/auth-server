package com.auth.module.security.core.autoconfigure;

import com.auth.common.jwt.api.JwtTokenProvider;
import com.auth.common.jwt.autoconfigure.JwtAutoConfiguration;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.module.security.core.token.facade.SecurityTokenHandlerFacade;
import com.auth.module.security.core.token.provider.AccessTokenProvider;
import com.auth.module.security.core.token.provider.InternalTokenProvider;
import com.auth.module.security.core.token.provider.RefreshTokenProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 模块安全核心自动配置
 *
 * @author Bunny
 */
@AutoConfiguration(after = JwtAutoConfiguration.class)
public class ModuleSecurityCore {

	@ConditionalOnMissingBean
	@Bean
	public AccessTokenProvider accessTokenProvider(JwtTokenProvider jwtTokenProvider, JwtProperties properties) {
		return new AccessTokenProvider(jwtTokenProvider, properties);
	}

	@ConditionalOnMissingBean
	@Bean
	public RefreshTokenProvider refreshTokenProvider(JwtTokenProvider jwtTokenProvider, JwtProperties properties) {
		return new RefreshTokenProvider(jwtTokenProvider, properties);
	}

	@ConditionalOnMissingBean
	@Bean
	public InternalTokenProvider internalTokenProvider(JwtTokenProvider jwtTokenProvider, JwtProperties properties) {
		return new InternalTokenProvider(jwtTokenProvider, properties);
	}

	@ConditionalOnMissingBean
	@Bean
	public SecurityTokenHandlerFacade securityTokenHandlerFacade(AccessTokenProvider accessTokenProvider,
			RefreshTokenProvider refreshTokenProvider, InternalTokenProvider internalTokenProvider) {
		return new SecurityTokenHandlerFacade(accessTokenProvider, refreshTokenProvider, internalTokenProvider);
	}

}
