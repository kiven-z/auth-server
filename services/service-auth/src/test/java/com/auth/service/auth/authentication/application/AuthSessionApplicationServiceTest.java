package com.auth.service.auth.authentication.application;

import com.auth.service.auth.authentication.api.dto.request.UsernamePasswordLoginRequest;
import com.auth.service.auth.authentication.api.dto.response.RefreshTokenResponse;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.domain.model.RefreshSessionResult;
import com.auth.service.auth.authentication.infrastructure.cookie.AuthRefreshCookieService;
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
class AuthSessionApplicationServiceTest {

    @Mock
    private AuthService authService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthRefreshCookieService authRefreshCookieService;

    @InjectMocks
    private AuthSessionApplicationService authSessionApplicationService;

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

        UserLoginResponse actual = authSessionApplicationService.completeLogin(auth, request, response);

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

        RefreshTokenResponse actual = authSessionApplicationService.completeRefresh(request, response);

        assertSame(body, actual);
        verify(authRefreshCookieService).writeRefreshToken(response, request, "new-refresh", false);
    }

    @Test
    @DisplayName("completeLogout：撤销 Access/Refresh 会话并始终清除 Cookie")
    void completeLogout_shouldRevokeSessionsAndClearCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer access-token");
        when(authRefreshCookieService.readRefreshToken(request)).thenReturn("refresh-token");

        authSessionApplicationService.completeLogout(request, response);

        verify(authService).logout("access-token", request);
        verify(refreshTokenService).revokeByRefreshToken("refresh-token", request);
        verify(authRefreshCookieService).clearRefreshToken(response, request);
    }

    @Test
    @DisplayName("completeLogout：无 Authorization 时仍按 Refresh Cookie 撤销会话")
    void completeLogout_shouldRevokeByRefresh_whenNoAuthorizationHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(authRefreshCookieService.readRefreshToken(request)).thenReturn("refresh-only");

        authSessionApplicationService.completeLogout(request, response);

        verify(authService, never()).logout(anyString(), any());
        verify(refreshTokenService).revokeByRefreshToken("refresh-only", request);
        verify(authRefreshCookieService).clearRefreshToken(response, request);
    }

}
