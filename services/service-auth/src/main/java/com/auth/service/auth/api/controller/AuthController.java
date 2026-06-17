package com.auth.service.auth.api.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.PublicApi;
import com.auth.service.auth.api.dto.authentication.request.EmailLoginRequest;
import com.auth.service.auth.api.dto.authentication.request.SmsLoginRequest;
import com.auth.service.auth.api.dto.authentication.request.UsernamePasswordLoginRequest;
import com.auth.service.auth.api.dto.authentication.response.RefreshTokenResponse;
import com.auth.service.auth.api.dto.authentication.response.UserLoginResponse;
import com.auth.service.auth.application.login.command.AuthSessionApplicationService;
import com.auth.service.auth.application.login.input.EmailCommand;
import com.auth.service.auth.application.login.input.SmsCommand;
import com.auth.service.auth.application.login.input.UsernamePasswordCommand;
import com.auth.service.auth.convert.LoginSessionConverter;
import com.auth.service.auth.domain.authentication.model.CompletedLoginSession;
import com.auth.service.auth.domain.authentication.model.CompletedRefreshSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录接口
 *
 * @author Bunny
 */
@Tag(name = "登录接口", description = "用户登录、退出、刷新Token接口")
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

	private final AuthSessionApplicationService authSessionApplicationService;

	@PublicApi
	@Operation(summary = "用户名密码登录")
	@PostMapping("login/username")
	public Result<UserLoginResponse> loginByUsername(@Valid @RequestBody UsernamePasswordLoginRequest request,
			HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		UsernamePasswordCommand command = new UsernamePasswordCommand();
		command.setUsername(request.getUsername());
		command.setPassword(request.getPassword());
		command.setRememberMe(request.getRememberMe());
		CompletedLoginSession session = authSessionApplicationService.completeLogin(command, servletRequest,
				servletResponse);

		return Result.success(LoginSessionConverter.INSTANCE.toUserLoginResponse(session));
	}

	@PublicApi
	@Operation(summary = "邮箱登录")
	@PostMapping("login/email")
	public Result<UserLoginResponse> loginByEmail(@Valid @RequestBody EmailLoginRequest request,
			HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		EmailCommand command = new EmailCommand();
		command.setEmail(request.getEmail());
		command.setCode(request.getCode());
		command.setRememberMe(request.getRememberMe());
		CompletedLoginSession session = authSessionApplicationService.completeLogin(command, servletRequest,
				servletResponse);

		return Result.success(LoginSessionConverter.INSTANCE.toUserLoginResponse(session));
	}

	@PublicApi
	@Operation(summary = "短信登录")
	@PostMapping("login/sms")
	public Result<UserLoginResponse> loginBySms(@Valid @RequestBody SmsLoginRequest request,
			HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		SmsCommand command = new SmsCommand();
		command.setPhone(request.getPhone());
		command.setCode(request.getCode());
		command.setRememberMe(request.getRememberMe());
		CompletedLoginSession session = authSessionApplicationService.completeLogin(command, servletRequest,
				servletResponse);

		return Result.success(LoginSessionConverter.INSTANCE.toUserLoginResponse(session));
	}

	@PublicApi
	@Operation(summary = "刷新令牌")
	@PostMapping("refresh-token")
	public Result<RefreshTokenResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		CompletedRefreshSession session = authSessionApplicationService.completeRefresh(request, response);

		return Result.success(LoginSessionConverter.INSTANCE.toRefreshTokenResponse(session));
	}

	@Operation(summary = "退出登录")
	@PostMapping("logout")
	public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		authSessionApplicationService.completeLogout(request, response);

		return Result.success();
	}

}
