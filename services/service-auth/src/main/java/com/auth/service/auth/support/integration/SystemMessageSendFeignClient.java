package com.auth.service.auth.support.integration;

import com.auth.common.core.model.response.Result;
import com.auth.module.message.command.TemplateMessageCommand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 system 服务消息发送接口
 *
 * @author Bunny
 */
@FeignClient(name = "service-system", contextId = "systemMessageSend", path = "/api/system")
public interface SystemMessageSendFeignClient {

	/**
	 * 按模板发送消息（多渠道统一入口）
	 * @param command 模板化发送命令
	 * @return 统一结果
	 */
	@PostMapping("/message/send")
	Result<String> sendByTemplate(@RequestBody TemplateMessageCommand command);

}
