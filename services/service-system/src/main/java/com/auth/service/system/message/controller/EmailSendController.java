package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.message.email.EmailSendDTO;
import com.auth.service.system.message.service.EmailSendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通用邮件发送接口（供其它微服务或前端调用）
 *
 * @author Bunny
 */
@Tag(name = "通用邮件发送", description = "基于模板的邮件发送接口")
@Validated
@RequestMapping("/api/system/email")
@RestController
public class EmailSendController {

	private final EmailSendService emailSendService;

	public EmailSendController(EmailSendService emailSendService) {
		this.emailSendService = emailSendService;
	}

	@Operation(summary = "发送模板邮件", description = "基于 sys_email_template + FreeMarker 渲染并发送邮件")
	@PostMapping("/send")
	public Result<String> sendEmail(@Valid @RequestBody EmailSendDTO dto) {
		emailSendService.sendByTemplate(dto);
		return Result.success("邮件发送请求已受理");
	}

}
