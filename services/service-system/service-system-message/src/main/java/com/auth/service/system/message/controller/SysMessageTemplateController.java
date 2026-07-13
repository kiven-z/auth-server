package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.message.model.constants.MessageAuditBizModule;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.MessageTemplateRequireFieldsForm;
import com.auth.service.system.message.model.form.MessageTemplateStatusForm;
import com.auth.service.system.message.model.form.MessageTemplateTestSendForm;
import com.auth.service.system.message.model.query.MessageTemplateQuery;
import com.auth.service.system.message.model.vo.MessageTemplateDetailVO;
import com.auth.service.system.message.model.vo.MessageTemplatePageResponse;
import com.auth.service.system.message.service.SysMessageTemplateService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 消息模板公共接口
 *
 * @author Bunny
 */
@Tag(name = "消息模板相关", description = "消息模板跨渠道公共查询、删除、启停等")
@Validated
@RequestMapping("/api/system/message-template")
@RestController
public class SysMessageTemplateController {

	private final SysMessageTemplateService sysMessageTemplateService;

	public SysMessageTemplateController(SysMessageTemplateService sysMessageTemplateService) {
		this.sysMessageTemplateService = sysMessageTemplateService;
	}

	@Operation(summary = "分页查询消息模板", description = "按 channel 过滤；前端传入固定渠道参数即可复用本接口")
	@PreAuthorize("@auth.decide('message:template:query')")
	@GetMapping()
	public Result<PageResponse<MessageTemplatePageResponse>> getMessageTemplatePage(@Valid MessageTemplateQuery query) {
		Page<MessageTemplateEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<MessageTemplatePageResponse> pageResponse = sysMessageTemplateService
			.getMessageTemplatePage(pageParams, query);
		return Result.success(pageResponse);
	}

	@Operation(summary = "消息模板详情", description = "按主键查询；channel 须与库中渠道一致；返回原文 content 与示例渲染 previewContent/contentType")
	@PreAuthorize("@auth.decide('message:template:detail')")
	@GetMapping("{id}")
	public Result<MessageTemplateDetailVO> getMessageTemplateById(@PathVariable("id") Long id,
			@RequestParam @NotBlank(message = "消息渠道不能为空") String channel) {
		MessageTemplateDetailVO detailVO = sysMessageTemplateService.getMessageTemplateById(id, channel);
		return Result.success(detailVO);
	}

	@OperationLog(targetType = "MESSAGE_TEMPLATE_DELETE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_MESSAGE_TEMPLATE, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除消息模板", description = "按 ids 删除；channel 限定渠道，避免跨渠道误删")
	@PreAuthorize("@auth.decide('message:template:delete')")
	@DeleteMapping()
	public Result<String> batchDelete(@RequestBody @NotEmpty(message = "模板ID列表不能为空") List<Long> ids,
			@RequestParam @NotBlank(message = "消息渠道不能为空") String channel) {
		sysMessageTemplateService.batchDelete(ids, channel);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "MESSAGE_TEMPLATE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_MESSAGE_TEMPLATE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量启用/禁用消息模板", description = "按 ids 与 channel 启停，避免跨渠道误改")
	@PreAuthorize("@auth.decide('message:template:update')")
	@PutMapping("status")
	public Result<String> batchUpdateStatus(@Validated @RequestBody MessageTemplateStatusForm form) {
		sysMessageTemplateService.batchUpdateStatus(form.getIds(), form.getStatus(), form.getChannel());
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "MESSAGE_TEMPLATE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_MESSAGE_TEMPLATE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "仅更新变量列表 require_fields", description = "按主键与 channel 更新，避免跨渠道误改")
	@PreAuthorize("@auth.decide('message:template:update')")
	@PutMapping("{id}/require-fields")
	public Result<String> updateRequireFields(@PathVariable("id") Long id,
			@RequestParam @NotBlank(message = "消息渠道不能为空") String channel,
			@Validated @RequestBody MessageTemplateRequireFieldsForm form) {
		sysMessageTemplateService.updateRequireFields(id, channel, form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@Operation(summary = "测试发送", description = "按模板 require_fields 示例值组装变量并发送；target 语义由 channel 解释")
	@PreAuthorize("@auth.decide('message:template:update')")
	@PostMapping("test-send")
	public Result<String> testSend(@Validated @RequestBody MessageTemplateTestSendForm form) {
		sysMessageTemplateService.testSend(form);
		return Result.success();
	}

}
