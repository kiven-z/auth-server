package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.service.system.message.service.MessageDispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一消息发送 HTTP 入口
 *
 * @author Bunny
 */
@Tag(name = "统一消息发送", description = "按 channel 路由至邮件、短信、钉钉等渠道")
@RequestMapping("/api/system/message")
@RestController
public class MessageSendController {

	private final MessageDispatchService messageDispatchService;

	public MessageSendController(MessageDispatchService messageDispatchService) {
		this.messageDispatchService = messageDispatchService;
	}

	@InternalApi
	@Operation(summary = "发送消息")
	@PostMapping("send")
	public Result<String> sendByTemplate(@RequestBody TemplateMessageCommand command) {
		messageDispatchService.sendByTemplate(command);
		return Result.success("Message send request accepted");
	}

}
