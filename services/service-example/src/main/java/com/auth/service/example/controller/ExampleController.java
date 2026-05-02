package com.auth.service.example.controller;

import com.alibaba.fastjson2.JSON;
import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfig.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfig.annotation.PublicApi;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.core.token.model.RefreshTokenRequest;
import com.auth.service.example.feign.ExampleAuthInnerFeignClient;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 安全集成验证用 HTTP 接口
 *
 * @author Bunny
 */
@Slf4j
@RequestMapping("/api/example")
@RestController
public class ExampleController {

	private final ExampleAuthInnerFeignClient exampleAuthInnerFeignClient;

	private final SecurityProperties securityProperties;

	public ExampleController(ExampleAuthInnerFeignClient exampleAuthInnerFeignClient,
			SecurityProperties securityProperties) {
		this.exampleAuthInnerFeignClient = exampleAuthInnerFeignClient;
		this.securityProperties = securityProperties;
	}

	@Operation(summary = "1-公开路径接口")
	@GetMapping("/public")
	public Result<String> publicEndpoint() {
		return Result.success("ok-public");
	}

	@AuthenticatedApi
	@Operation(summary = "2-需要认证路径接口")
	@GetMapping("/authenticated")
	public Result<String> authenticatedEndpoint() {
		return Result.success("ok-authenticated");
	}

	@AuthenticatedApi
	@Operation(summary = "3-配置了放行路径，但是使用了认证注解")
	@GetMapping("/conflict/permit-path-but-authenticated")
	public Result<String> conflictPermitButAuthenticated() {
		log.info(JSON.toJSONString(securityProperties));
		return Result.success("ok-conflict-permit-authenticated");
	}

	@PublicApi
	@Operation(summary = "4-配置了认证路径，但是使用了公开注解")
	@GetMapping("/conflict/secured-path-but-public")
	public Result<String> conflictSecuredButPublic() {
		return Result.success("ok-conflict-secured-public");
	}

	@AuthenticatedApi
	@Operation(summary = "5-个人信息")
	@GetMapping("/me")
	public Result<AuthProfile> me() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Object principal = auth != null ? auth.getPrincipal() : null;
		if (!(principal instanceof AuthProfile profile)) {
			return Result.success(null);
		}
		return Result.success(profile);
	}

	@Operation(summary = "6-判断权限")
	@PreAuthorize("@auth.decide('sys:xxx')")
	@GetMapping("/pre-authorize-1")
	public Result<String> preAuthorize1() {
		return Result.success("ok-pre-authorize");
	}

	@Operation(summary = "7-判断权限")
	@PreAuthorize("@auth.decide('sys:user:add')")
	@GetMapping("/pre-authorize-2")
	public Result<String> preAuthorize2() {
		return Result.success("ok-pre-authorize");
	}

	@AuthenticatedApi
	@Operation(summary = "8-刷新Token")
	@PostMapping("/trigger-feign")
	public Result<Object> triggerFeign(@Valid @RequestBody RefreshTokenRequest request) {
		return exampleAuthInnerFeignClient.refreshToken(request);
	}

}
