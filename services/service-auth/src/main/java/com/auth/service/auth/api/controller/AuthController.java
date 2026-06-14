package com.auth.service.auth.api.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.PublicApi;
import com.auth.service.auth.api.dto.authentication.response.RefreshTokenResponse;
import com.auth.service.auth.api.dto.authentication.response.UserLoginResponse;
import com.auth.service.auth.application.login.AuthSessionApplicationService;
import com.auth.service.auth.application.login.input.credential.EmailLogin;
import com.auth.service.auth.application.login.input.credential.SmsLogin;
import com.auth.service.auth.application.login.input.credential.UsernamePasswordLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/api/auth")
@RestController
public class AuthController {

	private final AuthSessionApplicationService authSessionApplicationService;

	public AuthController(AuthSessionApplicationService authSessionApplicationService) {
		this.authSessionApplicationService = authSessionApplicationService;
	}

	@PublicApi
	@Operation(summary = "用户名密码登录")
	@PostMapping("login/username")
	public Result<UserLoginResponse> loginByUsername(@Valid @RequestBody UsernamePasswordLogin credential,
			HttpServletRequest request, HttpServletResponse response) {
		UserLoginResponse data = authSessionApplicationService.completeLogin(credential, request, response);
		return Result.success(data);
	}

	@PublicApi
	@Operation(summary = "邮箱登录")
	@PostMapping("login/email")
	public Result<UserLoginResponse> loginByEmail(@Valid @RequestBody EmailLogin credential, HttpServletRequest request,
			HttpServletResponse response) {
		UserLoginResponse data = authSessionApplicationService.completeLogin(credential, request, response);
		return Result.success(data);
	}

	@PublicApi
	@Operation(summary = "短信登录")
	@PostMapping("login/sms")
	public Result<UserLoginResponse> loginBySms(@Valid @RequestBody SmsLogin credential, HttpServletRequest request,
			HttpServletResponse response) {
		UserLoginResponse data = authSessionApplicationService.completeLogin(credential, request, response);
		return Result.success(data);
	}

	@PublicApi
	@Operation(summary = "刷新令牌")
	@PostMapping("refresh-token")
	public Result<RefreshTokenResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		RefreshTokenResponse data = authSessionApplicationService.completeRefresh(request, response);
		return Result.success(data);
	}

	@Operation(summary = "退出登录")
	@PostMapping("logout")
	public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		authSessionApplicationService.completeLogout(request, response);
		return Result.success();
	}

}
