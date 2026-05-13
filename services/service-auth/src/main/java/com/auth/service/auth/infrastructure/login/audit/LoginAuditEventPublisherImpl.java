package com.auth.service.auth.infrastructure.login.audit;

import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * {@link LoginAuditEventPublisher} 默认实现：构建快照后发布 Spring 应用事件。
 *
 * @author Bunny
 */
@Service
public class LoginAuditEventPublisherImpl implements LoginAuditEventPublisher {

	private final ApplicationEventPublisher applicationEventPublisher;

	private final LoginAuditSnapshotFactory loginAuditSnapshotFactory;

	public LoginAuditEventPublisherImpl(ApplicationEventPublisher applicationEventPublisher,
			LoginAuditSnapshotFactory loginAuditSnapshotFactory) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.loginAuditSnapshotFactory = loginAuditSnapshotFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void publish(HttpServletRequest request, AuthLoginLogType eventType, AuthLoginLogResult result, Long userId,
			String principal, String failureReason, String sessionId) {
		LoginAuditSnapshot snapshot = loginAuditSnapshotFactory.build(request);
		applicationEventPublisher.publishEvent(
				new LoginAuditEvent(snapshot, eventType, result, userId, principal, failureReason, sessionId));
	}

}
