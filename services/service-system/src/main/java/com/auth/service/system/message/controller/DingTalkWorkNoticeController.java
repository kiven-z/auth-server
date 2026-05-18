package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.message.api.DingTalkWorkNoticeService;
import com.auth.service.system.message.channel.dingtalk.model.DingTalkWorkNoticeSendForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 钉钉工作通知发送接口（手动直发，无模板）
 *
 * @author Bunny
 */
@Tag(name = "钉钉工作通知", description = "企业应用工作通知手动发送")
@ConditionalOnProperty(prefix = "auth.message.ding-talk", name = "enabled", havingValue = "true")
@Validated
@RequestMapping("/api/system/message/ding-talk")
@RestController
public class DingTalkWorkNoticeController {

	private final DingTalkWorkNoticeService dingTalkWorkNoticeService;

	public DingTalkWorkNoticeController(DingTalkWorkNoticeService dingTalkWorkNoticeService) {
		this.dingTalkWorkNoticeService = dingTalkWorkNoticeService;
	}

	@Operation(summary = "发送工作通知", description = "向指定 userid 列表发送文本或 Markdown 工作通知")
	@PostMapping("/send")
	public Result<String> send(@Valid @RequestBody DingTalkWorkNoticeSendForm form) {
		dingTalkWorkNoticeService.send(form);
		return Result.success("DingTalk work notice sent");
	}

}
