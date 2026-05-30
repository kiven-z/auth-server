package com.auth.service.auth.authentication.api.controller;

import com.auth.service.auth.authentication.api.dto.response.RefreshTokenResponse;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.AuthSessionApplicationService;
import com.auth.service.auth.authentication.application.input.credential.EmailLogin;
import com.auth.service.auth.authentication.application.input.credential.UsernamePasswordLogin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

/**
 * AuthController 单元测试：验证委托 AuthSessionFacade。
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	@Mock
	private AuthSessionApplicationService authSessionApplicationService;

	private AuthController controller;

	@BeforeEach
	void setUp() {
		controller = new AuthController(authSessionApplicationService);
	}

	@Test
	@DisplayName("用户名密码登录：委托 completeLogin")
	void loginByUsername_shouldDelegateToFacade() {
		UsernamePasswordLogin credential = new UsernamePasswordLogin();
		UserLoginResponse loginResponse = new UserLoginResponse();
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(authSessionApplicationService.completeLogin(same(credential), same(request), same(response)))
			.thenReturn(loginResponse);

		var result = controller.loginByUsername(credential, request, response);

		assertSame(loginResponse, result.getData());
		verify(authSessionApplicationService).completeLogin(credential, request, response);
	}

	@Test
	@DisplayName("邮箱登录：委托 completeLogin")
	void loginByEmail_shouldDelegateToFacade() {
		EmailLogin credential = new EmailLogin();
		UserLoginResponse loginResponse = new UserLoginResponse();
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(authSessionApplicationService.completeLogin(same(credential), same(request), same(response)))
			.thenReturn(loginResponse);

		var result = controller.loginByEmail(credential, request, response);

		assertSame(loginResponse, result.getData());
	}

	@Test
	@DisplayName("刷新令牌：委托 completeRefresh")
	void refreshToken_shouldDelegateToFacade() {
		RefreshTokenResponse data = new RefreshTokenResponse();
		data.setAccessToken("new-access-token");
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(authSessionApplicationService.completeRefresh(same(request), same(response))).thenReturn(data);

		var result = controller.refreshToken(request, response);

		assertSame(data, result.getData());
	}

	@Test
	@DisplayName("退出登录：委托 completeLogout")
	void logout_shouldDelegateToFacade() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		var result = controller.logout(request, response);

		assertNotNull(result);
		verify(authSessionApplicationService).completeLogout(request, response);
	}

}
