package com.auth.service.auth.authentication.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.PublicApi;
import com.auth.service.auth.authentication.service.AuthMessageService;
import com.auth.service.auth.support.ratelimit.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证消息控制器
 *
 * @author Bunny
 */
@RequestMapping("/api/auth/message")
@RestController
public class AuthMessageController {

	private final AuthMessageService messageService;

	public AuthMessageController(AuthMessageService messageService) {
		this.messageService = messageService;
	}

	@PublicApi
	@RateLimit(principal = "#email")
	@Operation(summary = "发送邮箱验证码", description = "验证码需要用MD5加密")
	@PostMapping("login/email/send-code")
	public Result<String> sendEmailCode(@RequestParam("email") String email, HttpServletRequest request) {
		messageService.sendEmailCode(email, request);
		return Result.success();
	}

}
