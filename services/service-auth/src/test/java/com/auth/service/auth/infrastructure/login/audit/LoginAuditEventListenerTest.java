package com.auth.service.auth.infrastructure.login.audit;

import com.auth.service.auth.authentication.audit.LoginAuditEvent;
import com.auth.service.auth.authentication.audit.LoginAuditEventListener;
import com.auth.service.auth.authentication.audit.LoginAuditSnapshot;
import com.auth.service.auth.authentication.service.AuthLoginAuditService;
import com.auth.service.auth.support.model.enums.AuthLoginLogResult;
import com.auth.service.auth.support.model.enums.AuthLoginLogType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

/**
 * {@link LoginAuditEventListener} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoginAuditEventListenerTest {

	@Mock
	private AuthLoginAuditService authLoginAuditService;

	@Test
	@DisplayName("监听器应将事件委托给审计服务快照写入")
	void onLoginAudit_shouldDelegateToAuditService() {
		LoginAuditEventListener listener = new LoginAuditEventListener(authLoginAuditService);
		LoginAuditSnapshot snapshot = LoginAuditSnapshot.empty();
		LoginAuditEvent event = new LoginAuditEvent(snapshot, AuthLoginLogType.LOGOUT, AuthLoginLogResult.SUCCESS, 3L,
				"name", null, "sid");

		listener.onLoginAudit(event);

		verify(authLoginAuditService).recordLoginLog(same(snapshot), eq(AuthLoginLogType.LOGOUT),
				eq(AuthLoginLogResult.SUCCESS), eq(3L), eq("name"), isNull(), eq("sid"));
	}

}
