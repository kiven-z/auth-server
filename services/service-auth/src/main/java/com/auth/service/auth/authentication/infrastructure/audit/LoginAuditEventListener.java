package com.auth.service.auth.authentication.infrastructure.audit;

import com.auth.service.auth.authentication.domain.event.LoginAuditEvent;
import com.auth.service.auth.authentication.domain.port.LoginAuditRecorder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 将 {@link LoginAuditEvent} 委托给 {@link LoginAuditRecorder} 落库。
 *
 * @author Bunny
 */
@Component
public class LoginAuditEventListener {

	private final LoginAuditRecorder loginAuditRecorder;

	public LoginAuditEventListener(LoginAuditRecorder loginAuditRecorder) {
		this.loginAuditRecorder = loginAuditRecorder;
	}

	/**
	 * 同步处理登录审计事件（与发布同线程）；后续若改为异步，事件载荷须保持快照化
	 * @param event 审计事件
	 */
	@EventListener
	public void onLoginAudit(LoginAuditEvent event) {
		loginAuditRecorder.recordLoginLog(event.snapshot(), event.eventType(), event.result(), event.userId(),
				event.principal(), event.failureReason(), event.sessionId());
	}

}
