package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.message.model.constants.MessageAuditBizModule;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import com.auth.service.system.message.model.vo.InAppComposeResultVO;
import com.auth.service.system.message.service.InAppSendTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 站内信发送任务（管理端按范围群发）
 *
 * @author Bunny
 */
@Tag(name = "站内信发送任务", description = "管理端按用户/岗位/部门/全员发送站内信；提交后异步分批写入收件箱")
@RequestMapping("/api/system/message/in-app")
@RestController
public class InAppSendTaskController {

	private final InAppSendTaskService inAppSendTaskService;

	public InAppSendTaskController(InAppSendTaskService inAppSendTaskService) {
		this.inAppSendTaskService = inAppSendTaskService;
	}

	@OperationLog(targetType = "IN_APP_MESSAGE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_IN_APP_MESSAGE, operation = OperationLogKind.CREATE)
	@Operation(summary = "按范围发送站内信")
	@PreAuthorize("@auth.decide('message:inapp:send')")
	@PostMapping("/send")
	public Result<InAppComposeResultVO> send(@Validated @RequestBody InAppComposeForm form) {
		InAppComposeResultVO data = inAppSendTaskService.send(form);
		return Result.success(data);
	}

	@OperationLog(targetType = "IN_APP_MESSAGE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_IN_APP_MESSAGE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "补发站内信发送任务")
	@PreAuthorize("@auth.decide('message:inapp:send')")
	@PostMapping("/tasks/{taskId}/retry")
	public Result<InAppComposeResultVO> retry(@PathVariable Long taskId) {
		InAppComposeResultVO data = inAppSendTaskService.retry(taskId);
		return Result.success(data);
	}

}
