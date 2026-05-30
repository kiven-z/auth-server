package com.auth.service.auth.authorization.application.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.PermissionInvalidatePayload;
import com.auth.service.auth.authorization.domain.invalidation.port.AuthorizationImpactQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 按权限码反查受影响用户 ID（P4→P1 桥接委托 Port）
 *
 * @author Bunny
 */
@Component
public class PermissionImpactResolver implements ImpactResolver<PermissionInvalidatePayload> {

	private final AuthorizationImpactQueryPort authorizationImpactQueryPort;

	public PermissionImpactResolver(AuthorizationImpactQueryPort authorizationImpactQueryPort) {
		this.authorizationImpactQueryPort = authorizationImpactQueryPort;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.PERMISSION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<PermissionInvalidatePayload> payloadType() {
		return PermissionInvalidatePayload.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> resolve(PermissionInvalidatePayload payload) {
		List<String> permissionCodes = payload.permissionCodes();

		return authorizationImpactQueryPort.findUserIdsByPermissionCodes(permissionCodes);
	}

}
