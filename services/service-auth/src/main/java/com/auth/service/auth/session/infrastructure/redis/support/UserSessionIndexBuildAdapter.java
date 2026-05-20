package com.auth.service.auth.session.infrastructure.redis.support;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.authentication.application.port.UserSessionIndexBuildPort;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * {@link UserSessionIndexBuildPort} 适配器：委托 {@link UserSessionIndexFactory}。
 *
 * @author Bunny
 */
@Component
public class UserSessionIndexBuildAdapter implements UserSessionIndexBuildPort {

	private final UserSessionIndexFactory userSessionIndexFactory;

	public UserSessionIndexBuildAdapter(UserSessionIndexFactory userSessionIndexFactory) {
		this.userSessionIndexFactory = userSessionIndexFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserSessionIndex build(@NotNull HttpServletRequest request, Long userId, String jti, String refreshToken,
			boolean rememberMe) {
		return userSessionIndexFactory.buildUserSessionIndex(request, userId, jti, refreshToken, rememberMe);
	}

}
