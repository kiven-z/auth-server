package com.auth.service.auth.application.authorization.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.UserDeptInvalidatePayload;
import com.auth.service.auth.application.port.invalidation.AuthorizationImpactQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 按部门 ID 反查成员用户 ID（P3）
 *
 * @author Bunny
 */
@Component
public class UserDeptImpactResolver implements ImpactResolver<UserDeptInvalidatePayload> {

	private final AuthorizationImpactQueryPort authorizationImpactQueryPort;

	public UserDeptImpactResolver(AuthorizationImpactQueryPort authorizationImpactQueryPort) {
		this.authorizationImpactQueryPort = authorizationImpactQueryPort;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.USER_DEPT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<UserDeptInvalidatePayload> payloadType() {
		return UserDeptInvalidatePayload.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> resolve(UserDeptInvalidatePayload payload) {
		List<Long> deptIds = payload.deptIds();

		return authorizationImpactQueryPort.findUserIdsByDeptIds(deptIds);
	}

}
