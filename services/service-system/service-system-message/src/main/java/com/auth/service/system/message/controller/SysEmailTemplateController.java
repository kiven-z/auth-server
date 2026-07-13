package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.message.model.constants.MessageAuditBizModule;
import com.auth.service.system.message.model.form.email.EmailTemplateContentForm;
import com.auth.service.system.message.model.form.email.EmailTemplateForm;
import com.auth.service.system.message.model.form.email.EmailTemplateRenderForm;
import com.auth.service.system.message.service.SysEmailTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

	@Operation(summary = "离线渲染模板（示例值）", description = "用于编辑态实时预览")
	@PreAuthorize("@auth.decide('message:template:detail')")
	@PostMapping("render")
	public Result<String> render(@Validated @RequestBody EmailTemplateRenderForm form) {
		String html = sysEmailTemplateService.render(form);
		return Result.success(html);
	}

	@Operation(summary = "添加邮件模板", description = "添加邮件模板")
	@PreAuthorize("@auth.decide('message:template:create')")
	@PostMapping()
	public Result<String> create(@Validated(CreateGroup.class) @RequestBody EmailTemplateForm dto) {
		sysEmailTemplateService.create(dto);

		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "EMAIL_TEMPLATE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_MESSAGE_TEMPLATE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新邮件模板", description = "更新邮件模板")
	@PreAuthorize("@auth.decide('message:template:update')")
	@PutMapping()
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody EmailTemplateForm dto) {
		sysEmailTemplateService.update(dto);

		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "EMAIL_TEMPLATE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_MESSAGE_TEMPLATE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "仅更新邮件模板正文", description = "仅更新 content 字段")
	@PreAuthorize("@auth.decide('message:template:update')")
	@PutMapping("{id}/content")
	public Result<String> updateContent(@PathVariable("id") Long id,
			@Validated @RequestBody EmailTemplateContentForm dto) {
		sysEmailTemplateService.updateContent(id, dto);

		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
