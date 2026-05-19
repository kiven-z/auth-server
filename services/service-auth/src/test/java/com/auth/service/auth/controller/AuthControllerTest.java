package com.auth.service.auth.controller;

import com.auth.service.auth.authentication.controller.AuthController;
import com.auth.service.auth.authentication.facade.AuthSessionFacade;
import com.auth.service.auth.support.model.request.login.EmailLoginRequest;
import com.auth.service.auth.support.model.request.login.UsernamePasswordLoginRequest;
import com.auth.service.auth.support.model.response.RefreshTokenResponse;
import com.auth.service.auth.support.model.response.UserLoginResponse;
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
	private AuthSessionFacade authSessionFacade;

	private AuthController controller;

	@BeforeEach
	void setUp() {
		controller = new AuthController(authSessionFacade);
	}

	@Test
	@DisplayName("用户名密码登录：委托 completeLogin")
	void loginByUsername_shouldDelegateToFacade() {
		UsernamePasswordLoginRequest auth = new UsernamePasswordLoginRequest();
		UserLoginResponse loginResponse = new UserLoginResponse();
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(authSessionFacade.completeLogin(same(auth), same(request), same(response))).thenReturn(loginResponse);

		var result = controller.loginByUsername(auth, request, response);

		assertSame(loginResponse, result.getData());
		verify(authSessionFacade).completeLogin(auth, request, response);
	}

	@Test
	@DisplayName("邮箱登录：委托 completeLogin")
	void loginByEmail_shouldDelegateToFacade() {
		EmailLoginRequest auth = new EmailLoginRequest();
		UserLoginResponse loginResponse = new UserLoginResponse();
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(authSessionFacade.completeLogin(same(auth), same(request), same(response))).thenReturn(loginResponse);

		var result = controller.loginByEmail(auth, request, response);

		assertSame(loginResponse, result.getData());
	}

	@Test
	@DisplayName("刷新令牌：委托 completeRefresh")
	void refreshToken_shouldDelegateToFacade() {
		RefreshTokenResponse data = new RefreshTokenResponse();
		data.setAccessToken("new-access-token");
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(authSessionFacade.completeRefresh(same(request), same(response))).thenReturn(data);

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
		verify(authSessionFacade).completeLogout(request, response);
	}

}
