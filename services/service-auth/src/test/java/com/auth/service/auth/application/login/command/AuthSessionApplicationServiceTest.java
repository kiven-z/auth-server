package com.auth.service.auth.application.login.command;

import com.auth.service.auth.TestConstants;
import com.auth.service.auth.application.login.LoginAuditService;
import com.auth.service.auth.application.login.input.BaseLoginCommand;
import com.auth.service.auth.application.login.input.UsernamePasswordCommand;
import com.auth.service.auth.application.port.login.RefreshCookiePort;
import com.auth.service.auth.domain.authentication.model.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AuthSessionApplicationService} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class AuthSessionApplicationServiceTest {

	@Mock
	private LoginApplicationService loginApplicationService;

	@Mock
	private RefreshTokenApplicationService refreshTokenService;

	@Mock
	private RefreshCookiePort refreshCookiePort;

	@Mock
	private LoginAuditService loginAuditService;

	@InjectMocks
	private AuthSessionApplicationService authSessionApplicationService;

	@Test
	@DisplayName("completeLogin：登录后按 rememberMe 写入 Refresh Cookie")
	void completeLogin_shouldWriteCookieWithRememberMe() {
		UsernamePasswordCommand command = new UsernamePasswordCommand();
		command.setUsername("user");
		command.setPassword("pass");
		command.setRememberMe(true);

		CompletedLoginSession login = new CompletedLoginSession();
		login.setRefreshToken("refresh-token");
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(loginApplicationService.login(any(BaseLoginCommand.class), same(request))).thenReturn(login);

		CompletedLoginSession actual = authSessionApplicationService.completeLogin(command, request, response);

		assertSame(login, actual);
		verify(refreshCookiePort).writeRefreshToken(response, request, "refresh-token", true);
	}

	@Test
	@DisplayName("completeRefresh：刷新后按会话 rememberMe 重写 Cookie")
	void completeRefresh_shouldWriteCookieFromSessionRememberMe() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		LocalDateTime expires = LocalDateTime.now().plusHours(1);
		TokenPair tokenPair = new TokenPair("new-access", "new-refresh", expires);

		when(refreshCookiePort.readRefreshToken(request)).thenReturn("old-refresh");
		when(refreshTokenService.refreshToken("old-refresh", request))
			.thenReturn(new RefreshSessionResult(tokenPair, false));
		when(refreshCookiePort.resolveReadMeDay()).thenReturn(5L);

		CompletedRefreshSession actual = authSessionApplicationService.completeRefresh(request, response);

		assertEquals("new-access", actual.getAccessToken());
		assertEquals("new-refresh", actual.getRefreshToken());
		assertEquals(expires, actual.getExpires());
		assertEquals(5L, actual.getReadMeDay());
		verify(refreshCookiePort).resolveReadMeDay();
		verify(refreshCookiePort).writeRefreshToken(response, request, "new-refresh", false);
	}

	@Test
	@DisplayName("completeLogout：撤销 Access/Refresh 会话并始终清除 Cookie")
	void completeLogout_shouldRevokeSessionsAndClearCookie() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(request.getHeader("Authorization")).thenReturn("Bearer access-token");
		when(refreshCookiePort.readRefreshToken(request)).thenReturn("refresh-token");
		when(loginApplicationService.logout("access-token"))
			.thenReturn(Optional.of(new LogoutSessionHint(TestConstants.USER_ID, TestConstants.JTI)));
		when(refreshTokenService.revokeByRefreshToken("refresh-token")).thenReturn(Optional.empty());

		authSessionApplicationService.completeLogout(request, response);

		verify(loginApplicationService).logout("access-token");
		verify(refreshTokenService).revokeByRefreshToken("refresh-token");
		verify(loginAuditService).auditLogoutSuccess(same(request), eq(TestConstants.USER_ID), eq(TestConstants.JTI));
		verify(refreshCookiePort).clearRefreshToken(response, request);
	}

	@Test
	@DisplayName("completeLogout：无 Authorization 时仍按 Refresh Cookie 撤销会话")
	void completeLogout_shouldRevokeByRefresh_whenNoAuthorizationHeader() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(request.getHeader("Authorization")).thenReturn(null);
		when(refreshCookiePort.readRefreshToken(request)).thenReturn("refresh-only");
		when(refreshTokenService.revokeByRefreshToken("refresh-only"))
			.thenReturn(Optional.of(new LogoutSessionHint(TestConstants.USER_ID, TestConstants.JTI)));

		authSessionApplicationService.completeLogout(request, response);

		verify(loginApplicationService, never()).logout(anyString());
		verify(refreshTokenService).revokeByRefreshToken("refresh-only");
		verify(loginAuditService).auditLogoutSuccess(same(request), eq(TestConstants.USER_ID), eq(TestConstants.JTI));
		verify(refreshCookiePort).clearRefreshToken(response, request);
	}

}
