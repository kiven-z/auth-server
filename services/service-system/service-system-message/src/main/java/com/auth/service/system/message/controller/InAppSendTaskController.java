package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.message.model.constants.MessageAuditBizModule;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import com.auth.service.system.message.model.query.InAppSendTaskQuery;
import com.auth.service.system.message.model.vo.inapp.InAppComposeResultVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskPageResponse;
import com.auth.service.system.message.service.InAppSendTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 站内信发送任务
 *
 * @author Bunny
 */
@Tag(name = "站内信发送任务", description = "管理端发送站内信；提交后异步分批写入收件箱")
@Validated
@RequestMapping("/api/system/message/in-app")
@RestController
public class InAppSendTaskController {

	private final InAppSendTaskService inAppSendTaskService;

	public InAppSendTaskController(InAppSendTaskService inAppSendTaskService) {
		this.inAppSendTaskService = inAppSendTaskService;
	}

	@Operation(summary = "分页查询站内信发送任务")
	@PreAuthorize("@auth.decide('message:inapp:query')")
	@GetMapping("/tasks")
	public Result<PageResponse<InAppSendTaskPageResponse>> getSendTaskPage(@Valid InAppSendTaskQuery query) {
		PageResponse<InAppSendTaskPageResponse> pageResponse = inAppSendTaskService.getSendTaskPage(query);
		return Result.success(pageResponse);
	}

	@Operation(summary = "站内信发送任务详情", description = "按任务 ID 返回正文、范围快照及投递计数")
	@PreAuthorize("@auth.decide('message:inapp:detail')")
	@GetMapping("/tasks/{taskId}")
	public Result<InAppSendTaskDetailVO> getSendTaskById(@PathVariable Long taskId) {
		InAppSendTaskDetailVO detailVO = inAppSendTaskService.getSendTaskById(taskId);
		return Result.success(detailVO);
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
	public Result<String> retry(@PathVariable Long taskId) {
		inAppSendTaskService.retry(taskId);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "IN_APP_MESSAGE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_IN_APP_MESSAGE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "撤回站内信发送任务")
	@PreAuthorize("@auth.decide('message:inapp:recall')")
	@PostMapping("/tasks/{taskId}/recall")
	public Result<String> recall(@PathVariable Long taskId) {
		inAppSendTaskService.recall(taskId);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "IN_APP_MESSAGE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_IN_APP_MESSAGE, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除站内信发送任务", description = "仅终态可删；收件箱随外键 CASCADE 清理")
	@PreAuthorize("@auth.decide('message:inapp:delete')")
	@DeleteMapping("/tasks")
	public Result<String> batchDelete(@RequestBody @NotEmpty(message = "任务ID列表不能为空") List<Long> ids) {
		inAppSendTaskService.batchDelete(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
