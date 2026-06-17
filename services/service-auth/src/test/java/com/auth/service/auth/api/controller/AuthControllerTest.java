package com.auth.service.auth.api.controller;

import com.auth.service.auth.api.dto.authentication.request.EmailLoginRequest;
import com.auth.service.auth.api.dto.authentication.request.UsernamePasswordLoginRequest;
import com.auth.service.auth.application.login.command.AuthSessionApplicationService;
import com.auth.service.auth.application.login.input.EmailCommand;
import com.auth.service.auth.application.login.input.UsernamePasswordCommand;
import com.auth.service.auth.domain.authentication.model.CompletedLoginSession;
import com.auth.service.auth.domain.authentication.model.CompletedRefreshSession;
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
 * AuthController 单元测试：验证委托 AuthSessionApplicationService 及请求到命令的转换。
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
	@DisplayName("用户名密码登录：请求转换为命令后委托 completeLogin 并映射为 API 响应")
	void loginByUsername_shouldConvertRequestToCommandAndDelegate() {
		// 构造入站请求 DTO
		UsernamePasswordLoginRequest request = new UsernamePasswordLoginRequest();
		request.setUsername("user");
		request.setPassword("pass");

		CompletedLoginSession session = new CompletedLoginSession();
		session.setId(1L);
		session.setUsername("user");
		HttpServletRequest servletRequest = mock(HttpServletRequest.class);
		HttpServletResponse servletResponse = mock(HttpServletResponse.class);

		ArgumentCaptor<UsernamePasswordCommand> commandCaptor = ArgumentCaptor.forClass(UsernamePasswordCommand.class);
		when(authSessionApplicationService.completeLogin(commandCaptor.capture(), same(servletRequest),
				same(servletResponse)))
			.thenReturn(session);

		var result = controller.loginByUsername(request, servletRequest, servletResponse);

		// 验证 VO 映射
		assertEquals(1L, result.getData().getId());
		assertEquals("user", result.getData().getUsername());
		// 验证命令字段转换正确
		assertEquals("user", commandCaptor.getValue().getUsername());
		assertEquals("pass", commandCaptor.getValue().getPassword());
	}

	@Test
	@DisplayName("邮箱登录：请求转换为命令后委托 completeLogin")
	void loginByEmail_shouldConvertRequestToCommandAndDelegate() {
		// 构造入站请求 DTO
		EmailLoginRequest request = new EmailLoginRequest();
		request.setEmail("a@b.com");
		request.setCode("123456");

		CompletedLoginSession session = new CompletedLoginSession();
		session.setAccessToken("access");
		HttpServletRequest servletRequest = mock(HttpServletRequest.class);
		HttpServletResponse servletResponse = mock(HttpServletResponse.class);

		ArgumentCaptor<EmailCommand> commandCaptor = ArgumentCaptor.forClass(EmailCommand.class);
		when(authSessionApplicationService.completeLogin(commandCaptor.capture(), same(servletRequest),
				same(servletResponse)))
			.thenReturn(session);

		var result = controller.loginByEmail(request, servletRequest, servletResponse);

		assertEquals("access", result.getData().getAccessToken());
		// 验证命令字段转换正确
		assertEquals("a@b.com", commandCaptor.getValue().getEmail());
		assertEquals("123456", commandCaptor.getValue().getCode());
	}

	@Test
	@DisplayName("刷新令牌：委托 completeRefresh 并映射为 API 响应")
	void refreshToken_shouldDelegateToFacade() {
		CompletedRefreshSession session = new CompletedRefreshSession();
		session.setAccessToken("new-access-token");
		HttpServletRequest servletRequest = mock(HttpServletRequest.class);
		HttpServletResponse servletResponse = mock(HttpServletResponse.class);

		when(authSessionApplicationService.completeRefresh(same(servletRequest), same(servletResponse)))
			.thenReturn(session);

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
		verify(authSessionApplicationService).completeLogout(servletRequest, servletResponse);
	}

}
