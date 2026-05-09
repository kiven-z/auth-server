package com.auth.service.auth.infrastructure.cookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

/**
 * RefreshTokenCookieUtil 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenCookieUtilTest {

	private static final String COOKIE_NAME = "auth-refresh-token";

	@Test
	@DisplayName("readToken：Cookie 为 null 时返回空字符串")
	void readToken_shouldReturnEmpty_whenCookiesIsNull() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getCookies()).thenReturn(null);

		String result = RefreshTokenCookieUtil.readToken(request);

		assertEquals("", result);
	}

	@Test
	@DisplayName("readToken：不含目标 Cookie 时返回空字符串")
	void readToken_shouldReturnEmpty_whenNoMatchingCookie() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("other-cookie", "other-value") });

		String result = RefreshTokenCookieUtil.readToken(request);

		assertEquals("", result);
	}

	@Test
	@DisplayName("readToken：存在目标 Cookie 时返回对应值")
	void readToken_shouldReturnValue_whenCookieExists() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		Cookie target = new Cookie(COOKIE_NAME, "test-refresh-token");
		Cookie other = new Cookie("other", "value");
		when(request.getCookies()).thenReturn(new Cookie[] { other, target });

		String result = RefreshTokenCookieUtil.readToken(request);

		assertEquals("test-refresh-token", result);
	}

	@Test
	@DisplayName("writeToken：refreshToken 为 blank 时不写入 Cookie")
	void writeToken_shouldSkip_whenTokenIsBlank() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		RefreshTokenCookieUtil.writeToken(response, request, "", 3600L);

		verify(response, never()).addHeader(anyString(), anyString());
	}

	@Test
	@DisplayName("writeToken：非安全请求时写入 Secure=false 的 Cookie")
	void writeToken_shouldIncludeHttpOnlyAndPath_whenInsecure() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.isSecure()).thenReturn(false);
		HttpServletResponse response = mock(HttpServletResponse.class);

		RefreshTokenCookieUtil.writeToken(response, request, "refresh-value", 3600L);

		ArgumentCaptor<String> headerValue = ArgumentCaptor.forClass(String.class);
		verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerValue.capture());
		String cookieStr = headerValue.getValue();
		assertTrue(cookieStr.contains("auth-refresh-token=refresh-value"));
		assertTrue(cookieStr.contains("HttpOnly"));
		assertTrue(cookieStr.contains("Path=/api/auth"));
		assertTrue(cookieStr.contains("SameSite=Lax"));
		assertTrue(cookieStr.contains("Max-Age=3600"));
		assertFalse(cookieStr.contains("Secure"));
	}

	@Test
	@DisplayName("writeToken：安全请求时写入 Secure=true 的 Cookie")
	void writeToken_shouldIncludeSecure_whenRequestIsSecure() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.isSecure()).thenReturn(true);
		HttpServletResponse response = mock(HttpServletResponse.class);

		RefreshTokenCookieUtil.writeToken(response, request, "refresh-value", 3600L);

		ArgumentCaptor<String> headerValue = ArgumentCaptor.forClass(String.class);
		verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerValue.capture());
		String cookieStr = headerValue.getValue();
		assertTrue(cookieStr.contains("Secure"));
	}

	@Test
	@DisplayName("writeToken：X-Forwarded-Proto 为 https 时写入 Secure=true")
	void writeToken_shouldIncludeSecure_whenForwardedProtoHttps() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.isSecure()).thenReturn(false);
		when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");
		HttpServletResponse response = mock(HttpServletResponse.class);

		RefreshTokenCookieUtil.writeToken(response, request, "refresh-value", 3600L);

		ArgumentCaptor<String> headerValue = ArgumentCaptor.forClass(String.class);
		verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerValue.capture());
		assertTrue(headerValue.getValue().contains("Secure"));
	}

	@Test
	@DisplayName("clearToken：应清除同名 Cookie，Max-Age=0")
	void clearToken_shouldDeleteCookie() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.isSecure()).thenReturn(false);
		HttpServletResponse response = mock(HttpServletResponse.class);

		RefreshTokenCookieUtil.clearToken(response, request);

		ArgumentCaptor<String> headerValue = ArgumentCaptor.forClass(String.class);
		verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerValue.capture());
		String cookieStr = headerValue.getValue();
		assertTrue(cookieStr.startsWith("auth-refresh-token="));
		assertTrue(cookieStr.contains("Max-Age=0"));
		assertTrue(cookieStr.contains("HttpOnly"));
	}

}
