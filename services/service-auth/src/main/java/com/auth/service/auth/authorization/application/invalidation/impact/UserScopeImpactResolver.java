package com.auth.service.auth.authorization.application.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.UserScopeInvalidatePayload;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 按用户 ID 直连失效（P6，无需 SQL 反查）
 *
 * @author Bunny
 */
@Component
public class UserScopeImpactResolver implements ImpactResolver<UserScopeInvalidatePayload> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.USER_SCOPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<UserScopeInvalidatePayload> payloadType() {
		return UserScopeInvalidatePayload.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> resolve(UserScopeInvalidatePayload payload) {
		// 过滤掉空和负数
		return Set.copyOf(payload.userIds());
	}

}
