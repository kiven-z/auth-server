package com.auth.service.auth.authorization.application.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.RoleInvalidatePayload;
import com.auth.service.auth.authorization.domain.invalidation.port.AuthorizationImpactQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 按角色码反查受影响用户 ID
 *
 * @author Bunny
 */
@Component
public class RoleCodeImpactResolver implements ImpactResolver<RoleInvalidatePayload> {

	private final AuthorizationImpactQueryPort authorizationImpactQueryPort;

	public RoleCodeImpactResolver(AuthorizationImpactQueryPort authorizationImpactQueryPort) {
		this.authorizationImpactQueryPort = authorizationImpactQueryPort;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.ROLE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<RoleInvalidatePayload> payloadType() {
		return RoleInvalidatePayload.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> resolve(RoleInvalidatePayload payload) {
		List<String> roleCodes = payload.roleCodes();

		return authorizationImpactQueryPort.findUserIdsByRoleCodes(roleCodes);
	}

}
