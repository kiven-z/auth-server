package com.auth.service.auth.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.PublicApi;
import com.auth.service.auth.service.AuthMessageService;
import com.auth.service.auth.support.login.ratelimit.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证消息控制器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@RequestMapping("/api/auth/message")
@RestController
public class AuthMessageController {

	private final AuthMessageService messageService;

	@PublicApi
	@RateLimit(principal = "#email")
	@Operation(summary = "发送邮箱验证码")
	@PostMapping("login/email/send-code")
	public Result<String> sendEmailCode(@RequestParam("email") String email, HttpServletRequest request) {
		messageService.sendEmailCode(email, request);
		return Result.success();
	}

	@PublicApi
	@RateLimit(principal = "#phone")
	@Operation(summary = "发送手机短信验证码")
	@PostMapping("login/sms/send-code")
	public Result<String> sendSmsCode(@RequestParam("phone") String phone) {
		messageService.sendSmsCode(phone);
		return Result.success();
	}

}
