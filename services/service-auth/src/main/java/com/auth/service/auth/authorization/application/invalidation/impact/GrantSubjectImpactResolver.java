package com.auth.service.auth.authorization.application.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.GrantInvalidatePayload;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.auth.authorization.domain.invalidation.port.AuthorizationImpactQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 按 grant_table 授权主体反查受影响用户 ID（P2）
 *
 * @author Bunny
 */
@Component
public class GrantSubjectImpactResolver implements ImpactResolver<GrantInvalidatePayload> {

	private final AuthorizationImpactQueryPort authorizationImpactQueryPort;

	public GrantSubjectImpactResolver(AuthorizationImpactQueryPort authorizationImpactQueryPort) {
		this.authorizationImpactQueryPort = authorizationImpactQueryPort;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.GRANT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<GrantInvalidatePayload> payloadType() {
		return GrantInvalidatePayload.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> resolve(GrantInvalidatePayload payload) {
		List<GrantSubjectKey> subjects = payload.subjects();

		return authorizationImpactQueryPort.findUserIdsByGrantSubjects(subjects);
	}

}
