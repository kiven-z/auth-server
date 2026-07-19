package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.message.model.constants.MessageAuditBizModule;
import com.auth.service.system.message.model.form.inapp.InAppTemplateForm;
import com.auth.service.system.message.service.InAppTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 站内信模板 前端控制器
 *
 * @author Bunny
 */
@Tag(name = "站内信模板相关", description = "站内信模板相关接口")
@RequestMapping("/api/system/message/in-app-template")
@RestController
public class InAppTemplateController {

	private final InAppTemplateService inAppTemplateService;

	public InAppTemplateController(InAppTemplateService inAppTemplateService) {
		this.inAppTemplateService = inAppTemplateService;
	}

	@Operation(summary = "添加站内信模板", description = "channel 固定为 IN_APP；contentType 写入 im_message_type；变量列表创建后单独维护")
	@PreAuthorize("@auth.decide('message:template:create')")
	@PostMapping()
	public Result<String> create(@Validated(CreateGroup.class) @RequestBody InAppTemplateForm form) {
		inAppTemplateService.create(form);

		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "IN_APP_TEMPLATE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_MESSAGE_TEMPLATE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新站内信模板", description = "更新元数据与正文；不修改 channel、require_fields")
	@PreAuthorize("@auth.decide('message:template:update')")
	@PutMapping()
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody InAppTemplateForm form) {
		inAppTemplateService.update(form);

		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
