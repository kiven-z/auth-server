package com.auth.service.auth.controller;

import com.auth.service.auth.model.request.EmailLoginRequest;
import com.auth.service.auth.model.request.UsernamePasswordLoginRequest;
import com.auth.service.auth.model.response.RefreshTokenResponse;
import com.auth.service.auth.model.response.UserLoginResponse;
import com.auth.service.auth.model.value.login.command.EmailCommand;
import com.auth.service.auth.model.value.login.command.UsernamePasswordCommand;
import com.auth.service.auth.service.AuthSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

/**
 * AuthController 单元测试：验证委托 AuthSessionService 及请求到命令的转换。
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	@Mock
	private AuthSessionService authSessionService;

	private AuthController controller;

	@BeforeEach
	void setUp() {
		controller = new AuthController(authSessionService);
	}

	@Test
	@DisplayName("用户名密码登录：请求转换为命令后委托 completeLogin")
	void loginByUsername_shouldConvertRequestToCommandAndDelegate() {
		UsernamePasswordLoginRequest request = new UsernamePasswordLoginRequest();
		request.setUsername("user");
		request.setPassword("pass");

		UserLoginResponse responseBody = new UserLoginResponse();
		responseBody.setId(1L);
		responseBody.setUsername("user");
		HttpServletRequest servletRequest = mock(HttpServletRequest.class);
		HttpServletResponse servletResponse = mock(HttpServletResponse.class);

		ArgumentCaptor<UsernamePasswordCommand> commandCaptor = ArgumentCaptor.forClass(UsernamePasswordCommand.class);
		when(authSessionService.completeLogin(commandCaptor.capture(), same(servletRequest), same(servletResponse)))
			.thenReturn(responseBody);

		var result = controller.loginByUsername(request, servletRequest, servletResponse);

		assertEquals(1L, result.getData().getId());
		assertEquals("user", result.getData().getUsername());
		assertEquals("user", commandCaptor.getValue().getUsername());
		assertEquals("pass", commandCaptor.getValue().getPassword());
	}

	@Test
	@DisplayName("邮箱登录：请求转换为命令后委托 completeLogin")
	void loginByEmail_shouldConvertRequestToCommandAndDelegate() {
		EmailLoginRequest request = new EmailLoginRequest();
		request.setEmail("a@b.com");
		request.setCode("123456");

		UserLoginResponse responseBody = new UserLoginResponse();
		responseBody.setAccessToken("access");
		HttpServletRequest servletRequest = mock(HttpServletRequest.class);
		HttpServletResponse servletResponse = mock(HttpServletResponse.class);

		ArgumentCaptor<EmailCommand> commandCaptor = ArgumentCaptor.forClass(EmailCommand.class);
		when(authSessionService.completeLogin(commandCaptor.capture(), same(servletRequest), same(servletResponse)))
			.thenReturn(responseBody);

		var result = controller.loginByEmail(request, servletRequest, servletResponse);

		assertEquals("access", result.getData().getAccessToken());
		assertEquals("a@b.com", commandCaptor.getValue().getEmail());
		assertEquals("123456", commandCaptor.getValue().getCode());
	}

	@Test
	@DisplayName("刷新令牌：委托 completeRefresh")
	void refreshToken_shouldDelegateToFacade() {
		RefreshTokenResponse responseBody = new RefreshTokenResponse();
		responseBody.setAccessToken("new-access-token");
		HttpServletRequest servletRequest = mock(HttpServletRequest.class);
		HttpServletResponse servletResponse = mock(HttpServletResponse.class);

		when(authSessionService.completeRefresh(same(servletRequest), same(servletResponse))).thenReturn(responseBody);

		var result = controller.refreshToken(servletRequest, servletResponse);

		assertEquals("new-access-token", result.getData().getAccessToken());
	}

	@Test
	@DisplayName("退出登录：委托 completeLogout")
	void logout_shouldDelegateToFacade() {
		HttpServletRequest servletRequest = mock(HttpServletRequest.class);
		HttpServletResponse servletResponse = mock(HttpServletResponse.class);

		var result = controller.logout(servletRequest, servletResponse);

		assertNotNull(result);
		verify(authSessionService).completeLogout(servletRequest, servletResponse);
	}

}
