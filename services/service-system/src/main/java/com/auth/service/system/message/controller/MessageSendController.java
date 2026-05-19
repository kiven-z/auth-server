package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.message.api.command.TemplateMessageCommand;
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
 * 统一消息发送 HTTP 入口（邮件、短信、钉钉等均经此接口按 channel 路由）
 *
 * @author Bunny
 */
@Tag(name = "统一消息发送", description = "多渠道消息发送唯一入口；钉钉直发示例：channel=DING_TALK、customBody=true、body、options")
@Validated
@RequestMapping("/api/system/message")
@RestController
public class MessageSendController {

    private final MessageDispatchService messageDispatchService;

    public MessageSendController(MessageDispatchService messageDispatchService) {
        this.messageDispatchService = messageDispatchService;
    }

    @Operation(summary = "发送消息",
            description = "按 channel 路由（EMAIL、SMS_DYPNS_VERIFY_CODE、DING_TALK 等）；模板发送用 templateCode+variables；钉钉等支持 customBody 直发")
    @PostMapping("/send")
    public Result<String> sendByTemplate(@Valid @RequestBody TemplateMessageCommand command) {
        messageDispatchService.sendByTemplate(command);
        return Result.success("Message send request accepted");
    }

}
