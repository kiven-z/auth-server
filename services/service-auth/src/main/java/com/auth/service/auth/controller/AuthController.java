package com.auth.service.auth.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.module.security.autoconfigure.annotation.PublicApi;
import com.auth.service.auth.infrastructure.cookie.RefreshTokenCookieUtil;
import com.auth.service.auth.model.request.EmailAuth;
import com.auth.service.auth.model.request.UsernamePasswordAuth;
import com.auth.service.auth.model.response.RefreshTokenResponse;
import com.auth.service.auth.model.response.UserLoginResponse;
import com.auth.service.auth.service.AuthService;
import com.auth.service.auth.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.auth.common.jwt.provider.AbstractJwtTokenProvider.BEARER_PREFIX;

/**
 * 登录接口
 *
 * @author Bunny
 */
@Tag(name = "登录接口", description = "用户登录、退出、刷新Token接口")
@RequestMapping("/api/auth")
@RestController
public class AuthController {

	private final AuthService authService;

	private final RefreshTokenService refreshTokenService;

	private final JwtProperties jwtProperties;

	public AuthController(AuthService authService, RefreshTokenService refreshTokenService,
			JwtProperties jwtProperties) {
		this.authService = authService;
		this.refreshTokenService = refreshTokenService;
		this.jwtProperties = jwtProperties;
	}

	@PublicApi
	@Operation(summary = "用户名密码登录")
	@PostMapping("login/username")
	public Result<UserLoginResponse> loginByUsername(@Valid @RequestBody UsernamePasswordAuth auth,
			HttpServletRequest request, HttpServletResponse response) {
		UserLoginResponse login = authService.login(auth, request);

		RefreshTokenCookieUtil.writeToken(response, request, login.getRefreshToken(),
				jwtProperties.getRefreshExpired());

		return Result.success(login);
	}

	@PublicApi
	@Operation(summary = "邮箱登录")
	@PostMapping("login/email")
	public Result<UserLoginResponse> loginByEmail(@Valid @RequestBody EmailAuth auth, HttpServletRequest request,
			HttpServletResponse response) {
		UserLoginResponse login = authService.login(auth, request);

		RefreshTokenCookieUtil.writeToken(response, request, login.getRefreshToken(),
				jwtProperties.getRefreshExpired());

		return Result.success(login);
	}

	@PublicApi
	@Operation(summary = "刷新令牌")
	@PostMapping("refresh-token")
	public Result<RefreshTokenResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = RefreshTokenCookieUtil.readToken(request);
		RefreshTokenResponse data = refreshTokenService.refreshToken(refreshToken, request);

		RefreshTokenCookieUtil.writeToken(response, request, data.getRefreshToken(), jwtProperties.getRefreshExpired());

		return Result.success(data);
	}

	@Operation(summary = "退出登录")
	@PostMapping("logout")
	public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
			String token = authHeader.substring(7);
			authService.logout(token, request);
		}

		RefreshTokenCookieUtil.clearToken(response, request);
		return Result.success();
	}

}
