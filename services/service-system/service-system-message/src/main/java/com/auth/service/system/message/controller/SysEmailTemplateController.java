package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.common.model.SystemAuditBizModule;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.*;
import com.auth.service.system.message.model.query.EmailTemplateQuery;
import com.auth.service.system.message.model.vo.EmailTemplateDetailVO;
import com.auth.service.system.message.model.vo.EmailTemplatePageResponse;
import com.auth.service.system.message.service.SysEmailTemplateService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 邮件模板 前端控制器
 *
 * @author Bunny
 */
@Tag(name = "邮件模板相关", description = "邮件模板相关接口")
@RequestMapping("/api/system/email-template")
@RestController
public class SysEmailTemplateController {

	private final SysEmailTemplateService sysEmailTemplateService;

	public SysEmailTemplateController(SysEmailTemplateService sysEmailTemplateService) {
		this.sysEmailTemplateService = sysEmailTemplateService;
	}

	@Operation(summary = "分页查询邮件模板", description = "分页查询邮件模板")
	@PreAuthorize("@auth.decide('emailTemplate:query')")
	@GetMapping()
	public Result<PageResponse<EmailTemplatePageResponse>> getEmailTemplatePage(EmailTemplateQuery query) {
		Page<MessageTemplateEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<EmailTemplatePageResponse> pageResponse = sysEmailTemplateService.getEmailTemplatePage(pageParams,
				query);
		return Result.success(pageResponse);
	}

	@Operation(summary = "离线渲染模板（示例值）", description = "用于编辑态实时预览")
	@PreAuthorize("@auth.decide('emailTemplate:detail')")
	@PostMapping("render")
	public Result<String> render(@Validated @RequestBody EmailTemplateRenderForm form) {
		String html = sysEmailTemplateService.render(form);
		return Result.success(html);
	}

	@Operation(summary = "测试发送邮件", description = "使用模板与 require_fields 示例值渲染后发送")
	@PreAuthorize("@auth.decide('emailTemplate:update')")
	@PostMapping("test-send")
	public Result<String> testSend(@Validated @RequestBody EmailTemplateTestSendForm form) {
		sysEmailTemplateService.testSend(form);

		return Result.success();
	}

	@Operation(summary = "邮件模板详情", description = "邮件模板详情")
	@PreAuthorize("@auth.decide('emailTemplate:detail')")
	@GetMapping("{id}")
	public Result<EmailTemplateDetailVO> getEmailTemplateById(@PathVariable("id") Long id) {
		EmailTemplateDetailVO detailVO = sysEmailTemplateService.getEmailTemplateById(id);
		return Result.success(detailVO);
	}

	@Operation(summary = "预览邮件模板（FreeMarker 按示例值渲染为 HTML）", description = "预览邮件模板（渲染后 HTML）")
	@PreAuthorize("@auth.decide('emailTemplate:detail')")
	@GetMapping("{id}/preview")
	public Result<String> preview(@PathVariable("id") Long id) {
		String html = sysEmailTemplateService.previewRenderedHtml(id);
		return Result.success(html);
	}

	@Operation(summary = "添加邮件模板", description = "添加邮件模板")
	@PreAuthorize("@auth.decide('emailTemplate:create')")
	@PostMapping()
	public Result<String> create(@Validated(CreateGroup.class) @RequestBody EmailTemplateForm dto) {
		sysEmailTemplateService.create(dto);

		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "EMAIL_TEMPLATE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_EMAIL_TEMPLATE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新邮件模板", description = "更新邮件模板")
	@PreAuthorize("@auth.decide('emailTemplate:update')")
	@PutMapping()
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody EmailTemplateForm dto) {
		sysEmailTemplateService.update(dto);

		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "EMAIL_TEMPLATE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_EMAIL_TEMPLATE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "仅更新邮件模板正文", description = "仅更新 content 字段")
	@PreAuthorize("@auth.decide('emailTemplate:update')")
	@PutMapping("{id}/content")
	public Result<String> updateContent(@PathVariable("id") Long id,
			@Validated @RequestBody EmailTemplateContentForm dto) {
		sysEmailTemplateService.updateContent(id, dto);

		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "EMAIL_TEMPLATE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_EMAIL_TEMPLATE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "仅更新变量列表 require_fields", description = "仅更新变量列表 require_fields")
	@PreAuthorize("@auth.decide('emailTemplate:update')")
	@PutMapping("{id}/require-fields")
	public Result<String> updateRequireFields(@PathVariable("id") Long id,
			@Validated @RequestBody EmailTemplateRequireFieldsForm form) {
		sysEmailTemplateService.updateRequireFields(id, form);

		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "EMAIL_TEMPLATE_DELETE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_EMAIL_TEMPLATE, operation = OperationLogKind.DELETE)
	@Operation(summary = "删除邮件模板", description = "删除邮件模板")
	@PreAuthorize("@auth.decide('emailTemplate:delete')")
	@DeleteMapping()
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		sysEmailTemplateService.removeBatchByIds(ids);

		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "EMAIL_TEMPLATE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_EMAIL_TEMPLATE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量启用/禁用邮件模板", description = "批量启用/禁用邮件模板")
	@PreAuthorize("@auth.decide('emailTemplate:update')")
	@PutMapping("status")
	public Result<String> batchUpdateStatus(@Validated @RequestBody EmailTemplateStatusForm form) {
		sysEmailTemplateService.update(Wrappers.<MessageTemplateEntity>lambdaUpdate()
			.in(MessageTemplateEntity::getId, form.getIds())
			.eq(MessageTemplateEntity::getChannel, MessageChannel.EMAIL.name())
			.set(MessageTemplateEntity::getStatus, form.getStatus()));

		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
