package com.auth.service.auth.authentication.application.login.audit;

import com.auth.service.auth.TestConstants;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.event.LoginAuditEventPublisher;
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
	private LoginAuditEventPublisher loginAuditEventPublisher;

	@Mock
	private LoginAuditPrincipalNormalizer loginAuditPrincipalNormalizer;

	@Test
	@DisplayName("刷新成功审计：发布前规范化主体")
	void auditRefreshTokenSuccess_shouldPublishNormalizedPrincipal() {
		when(loginAuditPrincipalNormalizer.normalize(TestConstants.USER_ID, null)).thenReturn("Administrator");

		LoginAuditCoordinator coordinator = new LoginAuditCoordinator(loginAuditEventPublisher,
				loginAuditPrincipalNormalizer);

		coordinator.auditRefreshTokenSuccess(null, TestConstants.USER_ID, TestConstants.JTI);

		verify(loginAuditEventPublisher).publish(isNull(), eq(AuthLoginLogType.REFRESH_TOKEN),
				eq(AuthLoginLogResult.SUCCESS), eq(TestConstants.USER_ID), eq("Administrator"), isNull(),
				eq(TestConstants.JTI));
	}

}
