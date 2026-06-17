package com.auth.service.auth.application.login.command;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.application.login.LoginAuditService;
import com.auth.service.auth.application.port.login.CredentialPort;
import com.auth.service.auth.application.port.session.SessionPort;
import com.auth.service.auth.application.port.login.TokenPort;
import com.auth.service.auth.application.port.login.AuthProfileCachePort;
import com.auth.service.auth.application.port.login.RememberMePolicyPort;
import com.auth.service.auth.domain.authentication.model.AuthenticatedUser;
import com.auth.service.auth.domain.authentication.model.CompletedLoginSession;
import com.auth.service.auth.domain.authentication.model.LoginResult;
import com.auth.service.auth.domain.authentication.model.TokenPair;
import com.auth.service.auth.domain.authentication.model.enums.AuthLoginLogType;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.AuthResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link LoginSessionOrchestrator} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoginSessionOrchestratorTest {

	@Mock
	private SessionPort sessionGateway;

	@Mock
	private AuthProfileCachePort authProfileCachePort;

	@Mock
	private TokenPort tokenGateway;

	@Mock
	private CredentialPort credentialGateway;

	@Mock
	private LoginAuditService loginAuditService;

	@Mock
	private RememberMePolicyPort rememberMePolicyPort;

	@Mock
	private HttpServletRequest httpServletRequest;

	private static LoginResult toLoginResult(AuthProfile profile) {
		AuthenticatedUser user = new AuthenticatedUser(TestConstants.USER_ID, "user", "nick", "avatar");
		return new LoginResult(user, profile, AuthLoginLogType.LOGIN_PASSWORD);
	}

	private LoginSessionOrchestrator newOrchestrator() {
		return new LoginSessionOrchestrator(sessionGateway, authProfileCachePort, tokenGateway, credentialGateway,
				loginAuditService, rememberMePolicyPort);
	}

	@Test
	@DisplayName("会话签发：按编排顺序执行并返回已填充令牌的读模型")
	void issueSession_shouldOrchestrateInOrder_whenInputsValid() {
		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getUserId()).thenReturn(TestConstants.USER_ID);
		when(profile.getPermVersion()).thenReturn(1L);
		when(profile.getRoles()).thenReturn(List.of("COMMON"));
		when(profile.getPermissions()).thenReturn(List.of("system:user:list"));

		LoginResult loginResult = toLoginResult(profile);

		UserSessionIndex sessionIndex = new UserSessionIndex();
		sessionIndex.setUserId(TestConstants.USER_ID);
		sessionIndex.setSessionId(TestConstants.JTI);

		when(sessionGateway.buildSessionIndex(any(), anyLong(), anyString(), anyString(), anyBoolean()))
			.thenReturn(sessionIndex);
		when(tokenGateway.buildTokenPair(anyLong(), anyString(), anyLong())).thenReturn(new TokenPair(
				TestConstants.ACCESS_TOKEN, TestConstants.REFRESH_TOKEN, LocalDateTime.now().plusMinutes(1)));
		when(rememberMePolicyPort.resolveReadMeDay()).thenReturn(7L);

		CompletedLoginSession actual = newOrchestrator().issueSession(loginResult, false, httpServletRequest);

		assertEquals(TestConstants.ACCESS_TOKEN, actual.getAccessToken());
		assertEquals(TestConstants.REFRESH_TOKEN, actual.getRefreshToken());
		assertNotNull(actual.getExpires());
		assertEquals(7L, actual.getReadMeDay());
		assertEquals(TestConstants.USER_ID, actual.getId());
		assertEquals("user", actual.getUsername());

		InOrder inOrder = inOrder(sessionGateway, authProfileCachePort, tokenGateway, rememberMePolicyPort,
				credentialGateway, loginAuditService);
		inOrder.verify(sessionGateway).assertSessionLimit(TestConstants.USER_ID);
		inOrder.verify(authProfileCachePort).cacheProfile(profile);
		inOrder.verify(tokenGateway).buildTokenPair(eq(TestConstants.USER_ID), anyString(), eq(1L));
		inOrder.verify(sessionGateway)
			.buildSessionIndex(same(httpServletRequest), eq(TestConstants.USER_ID), anyString(),
					eq(TestConstants.REFRESH_TOKEN), eq(false));
		inOrder.verify(sessionGateway).registerSession(sessionIndex);
		inOrder.verify(rememberMePolicyPort).resolveReadMeDay();
		inOrder.verify(credentialGateway).recordSuccess(TestConstants.USER_ID);
		inOrder.verify(loginAuditService)
			.auditLoginSuccess(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD), eq(TestConstants.USER_ID),
					eq("user"), anyString());
	}

	@Test
	@DisplayName("会话签发：rememberMe=true 时写入会话索引")
	void issueSession_shouldPassRememberMe_whenRememberMeTrue() {
		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getUserId()).thenReturn(TestConstants.USER_ID);
		when(profile.getPermVersion()).thenReturn(1L);
		when(profile.getRoles()).thenReturn(List.of());
		when(profile.getPermissions()).thenReturn(List.of());

		LoginResult loginResult = toLoginResult(profile);

		when(sessionGateway.buildSessionIndex(any(), anyLong(), anyString(), anyString(), eq(true)))
			.thenReturn(new UserSessionIndex());
		when(tokenGateway.buildTokenPair(anyLong(), anyString(), anyLong())).thenReturn(new TokenPair(
				TestConstants.ACCESS_TOKEN, TestConstants.REFRESH_TOKEN, LocalDateTime.now().plusMinutes(1)));

		newOrchestrator().issueSession(loginResult, true, httpServletRequest);

		verify(sessionGateway).buildSessionIndex(same(httpServletRequest), eq(TestConstants.USER_ID), anyString(),
				anyString(), eq(true));
	}

	@Test
	@DisplayName("会话签发：令牌构建失败时回滚画像并写编排失败审计")
	void issueSession_shouldRollbackArtifacts_whenTokenBuildFails() {
		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getUserId()).thenReturn(TestConstants.USER_ID);
		when(profile.getPermVersion()).thenReturn(1L);

		LoginResult loginResult = toLoginResult(profile);

		when(tokenGateway.buildTokenPair(anyLong(), anyString(), anyLong()))
			.thenThrow(new AuthBusinessException(AuthResultCode.SERVER_ERROR));

		LoginSessionOrchestrator orchestrator = newOrchestrator();
		assertThrows(AuthBusinessException.class,
				() -> orchestrator.issueSession(loginResult, false, httpServletRequest));

		verify(authProfileCachePort).cacheProfile(profile);
		verify(authProfileCachePort).evictProfile(TestConstants.USER_ID);
		verify(sessionGateway, never()).registerSession(any());
		verify(loginAuditService).auditOrchestrationFailure(same(httpServletRequest),
				eq(AuthLoginLogType.LOGIN_PASSWORD), eq(TestConstants.USER_ID), eq("user"),
				any(AuthBusinessException.class));
	}

}
