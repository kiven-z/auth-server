package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.service.system.message.api.MessageDispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一模板消息发送接口（按渠道路由）
 *
 * @author Bunny
 */
@Tag(name = "统一消息发送", description = "基于模板的多渠道消息发送")
@Validated
@RequestMapping("/api/system/message")
@RestController
public class MessageSendController {

	private final MessageDispatchService messageDispatchService;

	public MessageSendController(MessageDispatchService messageDispatchService) {
		this.messageDispatchService = messageDispatchService;
	}

	@Operation(summary = "按模板发送消息", description = "根据 channel 路由至对应渠道发送实现")
	@PostMapping("/send")
	public Result<String> sendByTemplate(@Valid @RequestBody TemplateMessageCommand command) {
		messageDispatchService.sendByTemplate(command);
		return Result.success("Message send request accepted");
	}

}
