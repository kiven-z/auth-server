package com.auth.service.auth.authorization.application.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.PolicyInvalidatePayload;
import com.auth.service.auth.authorization.domain.invalidation.port.AuthorizationImpactQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 按策略码反查受影响用户 ID（用户直连绑定 + 可选角色桥接）。
 *
 * <p>
 * 角色桥接依赖 {@code role_policy} 表，由 {@code auth.auth.invalidation.policy-role-bridge-enabled} 控制，
 * 表未落地前保持默认 {@code false}。
 * </p>
 *
 * @author Bunny
 */
@Component
public class PolicyImpactResolver implements ImpactResolver<PolicyInvalidatePayload> {

	private final AuthorizationImpactQueryPort authorizationImpactQueryPort;

	public PolicyImpactResolver(AuthorizationImpactQueryPort authorizationImpactQueryPort) {
		this.authorizationImpactQueryPort = authorizationImpactQueryPort;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.POLICY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<PolicyInvalidatePayload> payloadType() {
		return PolicyInvalidatePayload.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> resolve(PolicyInvalidatePayload payload) {
		List<String> policyCodes = payload.policyCodes();

		return authorizationImpactQueryPort.findUserIdsByPolicyCodes(policyCodes);
	}

}
