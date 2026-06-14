package com.auth.service.auth.session.job;

import com.auth.service.auth.session.application.SessionAdminApplicationService;
import com.auth.service.auth.session.config.OnlineSessionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * {@link OnlineSessionReconcileJob} 单元测试
 */
@DisplayName("OnlineSessionReconcileJob 全局在线会话僵尸索引 reconcile")
@ExtendWith(MockitoExtension.class)
class OnlineSessionReconcileJobTest {

	@Mock
	private SessionAdminApplicationService sessionAdminApplicationService;

	@Mock
	private OnlineSessionProperties onlineSessionProperties;

	@InjectMocks
	private OnlineSessionReconcileJob onlineSessionReconcileJob;

	private OnlineSessionProperties.Reconcile reconcile;

	@BeforeEach
	void setUp() {
		reconcile = new OnlineSessionProperties.Reconcile();
		when(onlineSessionProperties.getReconcile()).thenReturn(reconcile);
	}

	@Test
	@DisplayName("未启用时不执行 reconcile")
	void reconcileStaleGlobalSessions_shouldSkipWhenDisabled() {
		reconcile.setEnabled(false);

		onlineSessionReconcileJob.reconcileStaleGlobalSessions();

		verify(sessionAdminApplicationService, never()).reconcileStaleGlobalSessions();
	}

	@Test
	@DisplayName("启用时委托 SessionAdminApplicationService 执行 reconcile")
	void reconcileStaleGlobalSessions_shouldDelegateWhenEnabled() {
		reconcile.setEnabled(true);
		when(sessionAdminApplicationService.reconcileStaleGlobalSessions()).thenReturn(3);

		onlineSessionReconcileJob.reconcileStaleGlobalSessions();

		verify(sessionAdminApplicationService).reconcileStaleGlobalSessions();
	}

}
