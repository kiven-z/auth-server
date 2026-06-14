package com.auth.service.auth.application.authorization.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.UserPostInvalidatePayload;
import com.auth.service.auth.application.port.invalidation.AuthorizationImpactQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 按岗位 ID 反查成员用户 ID（P3）
 *
 * @author Bunny
 */
@Component
public class UserPostImpactResolver implements ImpactResolver<UserPostInvalidatePayload> {

	private final AuthorizationImpactQueryPort authorizationImpactQueryPort;

	public UserPostImpactResolver(AuthorizationImpactQueryPort authorizationImpactQueryPort) {
		this.authorizationImpactQueryPort = authorizationImpactQueryPort;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.USER_POST;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<UserPostInvalidatePayload> payloadType() {
		return UserPostInvalidatePayload.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> resolve(UserPostInvalidatePayload payload) {
		List<Long> postIds = payload.postIds();

		return authorizationImpactQueryPort.findUserIdsByPostIds(postIds);
	}

}
