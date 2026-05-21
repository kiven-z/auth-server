package com.auth.service.auth.authentication.application.login.audit;

import com.auth.service.auth.TestConstants;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.model.LoginAuditSnapshot;
import com.auth.service.auth.authentication.domain.port.LoginAuditRecorder;
import com.auth.service.auth.authentication.domain.port.LoginAuditSnapshotPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link LoginAuditCoordinator} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoginAuditCoordinatorTest {

	@Mock
	private LoginAuditSnapshotPort loginAuditSnapshotPort;

	@Mock
	private LoginAuditRecorder loginAuditRecorder;

	@Mock
	private LoginAuditPrincipalNormalizer loginAuditPrincipalNormalizer;

	@Test
	@DisplayName("刷新成功审计：请求线程构建快照并规范化主体后异步落库")
	void auditRefreshTokenSuccess_shouldBuildSnapshotAndRecordAsync() {
		LoginAuditSnapshot snapshot = LoginAuditSnapshot.empty();
		when(loginAuditSnapshotPort.build(null)).thenReturn(snapshot);
		when(loginAuditPrincipalNormalizer.normalize(TestConstants.USER_ID, null)).thenReturn("Administrator");

		LoginAuditCoordinator coordinator = new LoginAuditCoordinator(loginAuditSnapshotPort, loginAuditRecorder,
				loginAuditPrincipalNormalizer);

		coordinator.auditRefreshTokenSuccess(null, TestConstants.USER_ID, TestConstants.JTI);

		verify(loginAuditSnapshotPort).build(null);
		verify(loginAuditRecorder).recordLoginLog(same(snapshot), eq(AuthLoginLogType.REFRESH_TOKEN),
				eq(AuthLoginLogResult.SUCCESS), eq(TestConstants.USER_ID), eq("Administrator"), isNull(),
				eq(TestConstants.JTI));
	}

}
