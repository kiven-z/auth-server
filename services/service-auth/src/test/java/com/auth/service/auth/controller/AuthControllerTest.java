package com.auth.service.auth.controller;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.service.auth.model.request.login.EmailLoginRequest;
import com.auth.service.auth.model.request.login.UsernamePasswordLoginRequest;
import com.auth.service.auth.model.response.RefreshTokenResponse;
import com.auth.service.auth.model.response.UserLoginResponse;
import com.auth.service.auth.service.AuthService;
import com.auth.service.auth.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthController 单元测试：验证 Cookie 读写、登录/刷新/登出流程编排。
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	private static final String BEARER_PREFIX = "Bearer ";

	@Mock
	private AuthService authService;

	@Mock
	private RefreshTokenService refreshTokenService;

	@Mock
	private JwtProperties jwtProperties;

	private AuthController controller;

	@BeforeEach
	void setUp() {
		controller = new AuthController(authService, refreshTokenService, jwtProperties);
	}

	@Test
	@DisplayName("用户名密码登录：成功后写入 Cookie 并返回登录响应")
	void loginByUsername_shouldWriteCookieAndReturnResponse() {
		// Arrange
		UsernamePasswordLoginRequest auth = new UsernamePasswordLoginRequest();
		auth.setUsername("user");
		auth.setPassword("pass");

		UserLoginResponse loginResponse = new UserLoginResponse();
		loginResponse.setAccessToken("access-token");
		loginResponse.setRefreshToken("refresh-token");
		loginResponse.setExpires(LocalDateTime.now().plusHours(1));

		when(authService.login(same(auth), any())).thenReturn(loginResponse);
		when(jwtProperties.getRefreshExpired()).thenReturn(604800L);

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		// Act
		var result = controller.loginByUsername(auth, request, response);

		// Assert
		assertSame(loginResponse, result.getData());
		ArgumentCaptor<String> headerValue = ArgumentCaptor.forClass(String.class);
		verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerValue.capture());
		String cookieStr = headerValue.getValue();
		assertTrue(cookieStr.contains("auth-refresh-token=refresh-token"));
		assertTrue(cookieStr.contains("HttpOnly"));
		assertTrue(cookieStr.contains("Path=/api/auth"));
	}

	@Test
	@DisplayName("邮箱登录：成功后写入 Cookie 并返回登录响应")
	void loginByEmail_shouldWriteCookieAndReturnResponse() {
		// Arrange
		EmailLoginRequest auth = new EmailLoginRequest();
		auth.setEmail("test@example.com");
		auth.setCode("123456");

		UserLoginResponse loginResponse = new UserLoginResponse();
		loginResponse.setAccessToken("access-token");
		loginResponse.setRefreshToken("refresh-token");
		loginResponse.setExpires(LocalDateTime.now().plusHours(1));

		when(authService.login(same(auth), any())).thenReturn(loginResponse);
		when(jwtProperties.getRefreshExpired()).thenReturn(604800L);

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		// Act
		var result = controller.loginByEmail(auth, request, response);

		// Assert
		assertSame(loginResponse, result.getData());
		verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
	}

	@Test
	@DisplayName("刷新令牌：从 Cookie 读取刷新令牌，成功时重写 Cookie 并返回新 accessToken")
	void refreshToken_shouldReadCookieAndWriteNewCookie() {
		// Arrange
		RefreshTokenResponse data = new RefreshTokenResponse();
		data.setAccessToken("new-access-token");
		data.setRefreshToken("new-refresh-token");
		data.setExpires(LocalDateTime.now().plusHours(1));

		when(jwtProperties.getRefreshExpired()).thenReturn(604800L);

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getCookies())
			.thenReturn(new Cookie[] { new Cookie("auth-refresh-token", "existing-refresh-token") });
		when(refreshTokenService.refreshToken(eq("existing-refresh-token"), same(request))).thenReturn(data);
		HttpServletResponse response = mock(HttpServletResponse.class);

		// Act
		var result = controller.refreshToken(request, response);

		// Assert
		assertSame(data, result.getData());
		ArgumentCaptor<String> headerValue = ArgumentCaptor.forClass(String.class);
		verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerValue.capture());
		String cookieStr = headerValue.getValue();
		assertTrue(cookieStr.contains("auth-refresh-token=new-refresh-token"));
		assertTrue(cookieStr.contains("Max-Age=604800"));
	}

	@Test
	@DisplayName("刷新令牌：Cookie 为空时传入空字符串，业务层抛错向上传播")
	void refreshToken_shouldPassEmpty_whenCookieMissing() {
		// Arrange
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getCookies()).thenReturn(null);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(refreshTokenService.refreshToken(eq(""), same(request)))
			.thenThrow(new RuntimeException("REFRESH_TOKEN_MISSING"));

		// Act & Assert
		assertThrows(RuntimeException.class, () -> controller.refreshToken(request, response));
	}

	@Test
	@DisplayName("退出登录：Authorization 头存在时调用 logout 并清除 Cookie")
	void logout_shouldCallLogoutAndClearCookie_whenAuthHeaderProvided() {
		// Arrange
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader("Authorization")).thenReturn(BEARER_PREFIX + "my-access-token");
		HttpServletResponse response = mock(HttpServletResponse.class);

		// Act
		var result = controller.logout(request, response);

		// Assert
		assertNotNull(result);
		verify(authService).logout("my-access-token", request);
		ArgumentCaptor<String> headerValue = ArgumentCaptor.forClass(String.class);
		verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerValue.capture());
		assertTrue(headerValue.getValue().contains("Max-Age=0"));
	}

	@Test
	@DisplayName("退出登录：Authorization 头缺失时不清除 Cookie 仍调用 clearToken")
	void logout_shouldNotCallAuthService_whenNoAuthHeader() {
		// Arrange
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader("Authorization")).thenReturn(null);
		HttpServletResponse response = mock(HttpServletResponse.class);

		// Act
		var result = controller.logout(request, response);

		// Assert
		assertNotNull(result);
		verify(authService, never()).logout(anyString(), any());
		verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
	}

}
