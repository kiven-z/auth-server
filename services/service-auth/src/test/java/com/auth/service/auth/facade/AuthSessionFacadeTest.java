package com.auth.service.auth.facade;

import com.auth.service.auth.authentication.cookie.AuthRefreshCookieService;
import com.auth.service.auth.authentication.facade.AuthSessionFacade;
import com.auth.service.auth.authentication.service.AuthService;
import com.auth.service.auth.authentication.service.RefreshTokenService;
import com.auth.service.auth.authentication.token.RefreshSessionResult;
import com.auth.service.auth.support.model.request.login.UsernamePasswordLoginRequest;
import com.auth.service.auth.support.model.response.RefreshTokenResponse;
import com.auth.service.auth.support.model.response.UserLoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthSessionFacade 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthSessionFacadeTest {

	@Mock
	private AuthService authService;

	@Mock
	private RefreshTokenService refreshTokenService;

	@Mock
	private AuthRefreshCookieService authRefreshCookieService;

	@InjectMocks
	private AuthSessionFacade authSessionFacade;

	@Test
	@DisplayName("completeLogin：登录后按 rememberMe 写入 Refresh Cookie")
	void completeLogin_shouldWriteCookieWithRememberMe() {
		UsernamePasswordLoginRequest auth = new UsernamePasswordLoginRequest();
		auth.setUsername("user");
		auth.setPassword("pass");
		auth.setRememberMe(true);

		UserLoginResponse login = new UserLoginResponse();
		login.setRefreshToken("refresh-token");
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(authService.login(same(auth), same(request))).thenReturn(login);

		UserLoginResponse actual = authSessionFacade.completeLogin(auth, request, response);

		assertSame(login, actual);
		verify(authRefreshCookieService).writeRefreshToken(response, request, "refresh-token", true);
	}

	@Test
	@DisplayName("completeRefresh：刷新后按会话 rememberMe 重写 Cookie")
	void completeRefresh_shouldWriteCookieFromSessionRememberMe() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		RefreshTokenResponse body = new RefreshTokenResponse();
		body.setAccessToken("new-access");
		body.setRefreshToken("new-refresh");
		body.setExpires(LocalDateTime.now().plusHours(1));

		when(authRefreshCookieService.readRefreshToken(request)).thenReturn("old-refresh");
		when(refreshTokenService.refreshToken("old-refresh", request))
			.thenReturn(new RefreshSessionResult(body, false));

		RefreshTokenResponse actual = authSessionFacade.completeRefresh(request, response);

		assertSame(body, actual);
		verify(authRefreshCookieService).writeRefreshToken(response, request, "new-refresh", false);
	}

	@Test
	@DisplayName("completeLogout：有 Authorization 时登出并清 Cookie")
	void completeLogout_shouldLogoutAndClearCookie() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(request.getHeader("Authorization")).thenReturn("Bearer access-token");

		authSessionFacade.completeLogout(request, response);

		verify(authService).logout("access-token", request);
		verify(authRefreshCookieService).clearRefreshToken(response, request);
	}

}
