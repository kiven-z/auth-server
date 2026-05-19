package com.auth.service.auth.authentication.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.PublicApi;
import com.auth.service.auth.authentication.facade.AuthSessionFacade;
import com.auth.service.auth.support.model.request.login.EmailLoginRequest;
import com.auth.service.auth.support.model.request.login.SmsLoginRequest;
import com.auth.service.auth.support.model.request.login.UsernamePasswordLoginRequest;
import com.auth.service.auth.support.model.response.RefreshTokenResponse;
import com.auth.service.auth.support.model.response.UserLoginResponse;
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

	private final AuthSessionFacade authSessionFacade;

	public AuthController(AuthSessionFacade authSessionFacade) {
		this.authSessionFacade = authSessionFacade;
	}

	@PublicApi
	@Operation(summary = "用户名密码登录")
	@PostMapping("login/username")
	public Result<UserLoginResponse> loginByUsername(@Valid @RequestBody UsernamePasswordLoginRequest auth,
			HttpServletRequest request, HttpServletResponse response) {
		UserLoginResponse data = authSessionFacade.completeLogin(auth, request, response);
		return Result.success(data);
	}

	@PublicApi
	@Operation(summary = "邮箱登录")
	@PostMapping("login/email")
	public Result<UserLoginResponse> loginByEmail(@Valid @RequestBody EmailLoginRequest auth,
			HttpServletRequest request, HttpServletResponse response) {
		UserLoginResponse data = authSessionFacade.completeLogin(auth, request, response);
		return Result.success(data);
	}

	@PublicApi
	@Operation(summary = "短信登录")
	@PostMapping("login/sms")
	public Result<UserLoginResponse> loginBySms(@Valid @RequestBody SmsLoginRequest auth, HttpServletRequest request,
			HttpServletResponse response) {
		UserLoginResponse data = authSessionFacade.completeLogin(auth, request, response);
		return Result.success(data);
	}

	@PublicApi
	@Operation(summary = "刷新令牌")
	@PostMapping("refresh-token")
	public Result<RefreshTokenResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		RefreshTokenResponse data = authSessionFacade.completeRefresh(request, response);
		return Result.success(data);
	}

	@Operation(summary = "退出登录")
	@PostMapping("logout")
	public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		authSessionFacade.completeLogout(request, response);
		return Result.success();
	}

}
