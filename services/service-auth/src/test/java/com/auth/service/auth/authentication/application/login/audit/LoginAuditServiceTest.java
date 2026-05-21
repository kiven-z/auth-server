package com.auth.service.auth.authentication.application.login.audit;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.authentication.application.port.AuthProfileCachePort;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.model.LoginAuditSnapshot;
import com.auth.service.auth.authentication.domain.port.LoginAuditRecorder;
import com.auth.service.auth.authentication.domain.port.LoginAuditSnapshotPort;
import com.auth.service.auth.authorization.domain.model.UserAccountSnapshot;
import com.auth.service.auth.authorization.domain.port.UserReadPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link LoginAuditService} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoginAuditServiceTest {

	@Mock
	private LoginAuditSnapshotPort loginAuditSnapshotPort;

	@Mock
	private LoginAuditRecorder loginAuditRecorder;

	@Mock
	private AuthProfileCachePort authProfileCachePort;

	@Mock
	private UserReadPort userReadPort;

	private LoginAuditService loginAuditService;

	@BeforeEach
	void setUp() {
		loginAuditService = new LoginAuditService(loginAuditSnapshotPort, loginAuditRecorder, authProfileCachePort,
				userReadPort);
	}

	@Test
	@DisplayName("刷新成功审计：请求线程构建快照并规范化主体后异步落库")
	void auditRefreshTokenSuccess_shouldBuildSnapshotAndRecordAsync() {
		LoginAuditSnapshot snapshot = LoginAuditSnapshot.empty();
		when(loginAuditSnapshotPort.build(null)).thenReturn(snapshot);
		when(authProfileCachePort.loadCachedProfile(TestConstants.USER_ID)).thenReturn(
				Optional.of(AuthProfile.builder().userId(TestConstants.USER_ID).username("Administrator").build()));

		loginAuditService.auditRefreshTokenSuccess(null, TestConstants.USER_ID, TestConstants.JTI);

		verify(loginAuditSnapshotPort).build(null);
		verify(loginAuditRecorder).recordLoginLog(same(snapshot), eq(AuthLoginLogType.REFRESH_TOKEN),
				eq(AuthLoginLogResult.SUCCESS), eq(TestConstants.USER_ID), eq("Administrator"), isNull(),
				eq(TestConstants.JTI));
	}

	@Test
	@DisplayName("无 userId：规范化时保留登录尝试主体")
	void normalizePrincipal_shouldKeepAttemptPrincipal_whenUserIdNull() {
		when(loginAuditSnapshotPort.build(null)).thenReturn(LoginAuditSnapshot.empty());

		loginAuditService.auditUserUnknownAfterResolve(null, AuthLoginLogType.LOGIN_PASSWORD, "13800138000",
				"user.not.found");

		verify(loginAuditRecorder).recordLoginLog(any(), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), isNull(), eq("13800138000"), eq("user.not.found"),
				isNull());
		verifyNoInteractions(authProfileCachePort, userReadPort);
	}

	@Test
	@DisplayName("有 userId 且入参合法：规范化保留入参登录名")
	void normalizePrincipal_shouldKeepIncoming_whenUsablePrincipal() {
		LoginAuditSnapshot snapshot = LoginAuditSnapshot.empty();
		when(loginAuditSnapshotPort.build(null)).thenReturn(snapshot);

		loginAuditService.auditLoginSuccess(null, AuthLoginLogType.LOGIN_PASSWORD, TestConstants.USER_ID,
				"Administrator", TestConstants.JTI);

		verify(loginAuditRecorder).recordLoginLog(same(snapshot), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.SUCCESS), eq(TestConstants.USER_ID), eq("Administrator"), isNull(),
				eq(TestConstants.JTI));
		verifyNoInteractions(authProfileCachePort, userReadPort);
	}

	@Test
	@DisplayName("有 userId 且入参为历史占位符：从画像缓存解析登录名")
	void normalizePrincipal_shouldResolveFromCache_whenPlaceholderPrincipal() {
		LoginAuditSnapshot snapshot = LoginAuditSnapshot.empty();
		when(loginAuditSnapshotPort.build(null)).thenReturn(snapshot);
		when(authProfileCachePort.loadCachedProfile(TestConstants.USER_ID)).thenReturn(
				Optional.of(AuthProfile.builder().userId(TestConstants.USER_ID).username("Administrator").build()));

		loginAuditService.auditLoginSuccess(null, AuthLoginLogType.LOGIN_PASSWORD, TestConstants.USER_ID, "/",
				TestConstants.JTI);

		verify(loginAuditRecorder).recordLoginLog(same(snapshot), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.SUCCESS), eq(TestConstants.USER_ID), eq("Administrator"), isNull(),
				eq(TestConstants.JTI));
		verifyNoInteractions(userReadPort);
	}

	@Test
	@DisplayName("有 userId 且缓存未命中：从 UserReadPort 解析登录名")
	void normalizePrincipal_shouldResolveFromDb_whenCacheMiss() {
		LoginAuditSnapshot snapshot = LoginAuditSnapshot.empty();
		when(loginAuditSnapshotPort.build(null)).thenReturn(snapshot);
		when(authProfileCachePort.loadCachedProfile(TestConstants.USER_ID)).thenReturn(Optional.empty());
		when(userReadPort.findById(TestConstants.USER_ID))
			.thenReturn(Optional.of(new UserAccountSnapshot(TestConstants.USER_ID, "Administrator", 1L)));

		loginAuditService.auditLoginSuccess(null, AuthLoginLogType.LOGIN_PASSWORD, TestConstants.USER_ID, null,
				TestConstants.JTI);

		verify(loginAuditRecorder).recordLoginLog(same(snapshot), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.SUCCESS), eq(TestConstants.USER_ID), eq("Administrator"), isNull(),
				eq(TestConstants.JTI));
	}

	@Test
	@DisplayName("resolvePrincipal：空白主体返回 null")
	void resolvePrincipal_shouldReturnNull_whenBlank() {
		assertNull(loginAuditService.resolvePrincipal(null));
	}

}
