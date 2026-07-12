package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.service.system.message.model.form.sms.SmsTemplateForm;
import com.auth.service.system.message.service.SysSmsTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 短信模板 前端控制器
 *
 * @author Bunny
 */
@Tag(name = "短信模板相关", description = "短信模板相关接口")
@RequestMapping("/api/system/sms-template")
@RestController
public class SysSmsTemplateController {

	private final SysSmsTemplateService sysSmsTemplateService;

	public SysSmsTemplateController(SysSmsTemplateService sysSmsTemplateService) {
		this.sysSmsTemplateService = sysSmsTemplateService;
	}

	@Operation(summary = "添加短信模板", description = "channel 固定为 SMS；厂商模板码写入 providerTemplateCode；变量列表创建后单独维护")
	@PreAuthorize("@auth.decide('message:template:create')")
	@PostMapping()
	public Result<String> create(@Validated(CreateGroup.class) @RequestBody SmsTemplateForm dto) {
		sysSmsTemplateService.create(dto);

		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
