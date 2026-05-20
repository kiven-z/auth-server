package com.auth.service.auth.authentication.infrastructure.audit;

import com.auth.service.auth.authentication.domain.model.LoginAuditSnapshot;
import com.auth.service.auth.authentication.domain.port.LoginAuditSnapshotPort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * {@link LoginAuditSnapshotPort} 适配器：委托 {@link LoginAuditSnapshotFactory}。
 *
 * @author Bunny
 */
@Component
public class LoginAuditSnapshotAdapter implements LoginAuditSnapshotPort {

	private final LoginAuditSnapshotFactory loginAuditSnapshotFactory;

	public LoginAuditSnapshotAdapter(LoginAuditSnapshotFactory loginAuditSnapshotFactory) {
		this.loginAuditSnapshotFactory = loginAuditSnapshotFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LoginAuditSnapshot build(HttpServletRequest request) {
		return loginAuditSnapshotFactory.build(request);
	}

}
