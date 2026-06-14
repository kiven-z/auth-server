package com.auth.service.auth.application.authorization.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.UserInvalidatePayload;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 按用户 ID 直连失效（用户主档变更，无需 SQL 反查）
 *
 * @author Bunny
 */
@Component
public class UserImpactResolver implements ImpactResolver<UserInvalidatePayload> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.USER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<UserInvalidatePayload> payloadType() {
		return UserInvalidatePayload.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> resolve(UserInvalidatePayload payload) {
		return Set.copyOf(payload.userIds());
	}

}
