package com.auth.service.auth.authentication.audit.event;

import com.auth.service.auth.authentication.service.AuthLoginAuditService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 将 {@link LoginAuditEvent} 转为 {@link AuthLoginAuditService} 持久化调用
 *
 * @author Bunny
 */
@Component
public class LoginAuditEventListener {

	private final AuthLoginAuditService authLoginAuditService;

	public LoginAuditEventListener(AuthLoginAuditService authLoginAuditService) {
		this.authLoginAuditService = authLoginAuditService;
	}

	/**
	 * 同步处理登录审计事件（与发布同线程）；后续若改为异步，事件载荷须保持快照化
	 * @param event 审计事件
	 */
	@EventListener
	public void onLoginAudit(LoginAuditEvent event) {
		authLoginAuditService.recordLoginLog(event.snapshot(), event.eventType(), event.result(), event.userId(),
				event.principal(), event.failureReason(), event.sessionId());
	}

}
