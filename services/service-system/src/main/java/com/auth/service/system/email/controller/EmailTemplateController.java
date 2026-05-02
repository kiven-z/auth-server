package com.auth.service.system.email.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.service.system.email.model.EmailTemplateEntity;
import com.auth.service.system.email.model.form.*;
import com.auth.service.system.email.model.query.EmailTemplateQuery;
import com.auth.service.system.email.model.vo.EmailTemplateDetailVO;
import com.auth.service.system.email.model.vo.EmailTemplatePageResponse;
import com.auth.service.system.email.service.EmailTemplateService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.*;

/**
 * 邮件模板 前端控制器
 *
 * @author Bunny
 */
@Tag(name = "邮件模板相关", description = "邮件模板相关接口")
@RequestMapping("/api/system/v1/email-template")
@RestController
public class EmailTemplateController {

	private final EmailTemplateService emailTemplateService;

	public EmailTemplateController(EmailTemplateService emailTemplateService) {
		this.emailTemplateService = emailTemplateService;
	}

	@Operation(summary = "分页查询邮件模板", description = "分页查询邮件模板")
	@PreAuthorize("@auth.decide('emailTemplate:query')")
	@GetMapping()
	public Result<PageResponse<EmailTemplatePageResponse>> getEmailTemplatePage(EmailTemplateQuery query) {
		Page<EmailTemplateEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<EmailTemplatePageResponse> pageResponse = emailTemplateService.getEmailTemplatePage(pageParams,
				query);
		return Result.success(pageResponse);
	}

	@Operation(summary = "离线渲染模板（示例值）", description = "用于编辑态实时预览")
	@PreAuthorize("@auth.decide('emailTemplate:detail')")
	@PostMapping("render")
	public Result<String> render(@Validated @RequestBody EmailTemplateRenderForm form) {
		String html = emailTemplateService.render(form);
		return Result.success(html);
	}

	@Operation(summary = "测试发送邮件", description = "使用模板与 require_fields 示例值渲染后发送")
	@PreAuthorize("@auth.decide('emailTemplate:update')")
	@PostMapping("test-send")
	public Result<String> testSend(@Validated @RequestBody EmailTemplateTestSendForm form) {
		emailTemplateService.testSend(form);

		Integer code = UPDATE_SUCCESS.getBizCode();
		String message = UPDATE_SUCCESS.getMessage();
		return Result.success(code, message);
	}

	@Operation(summary = "邮件模板详情", description = "邮件模板详情")
	@PreAuthorize("@auth.decide('emailTemplate:detail')")
	@GetMapping("{id}")
	public Result<EmailTemplateDetailVO> getEmailTemplateById(@PathVariable("id") Long id) {
		EmailTemplateDetailVO detailVO = emailTemplateService.getEmailTemplateById(id);
		return Result.success(detailVO);
	}

	@Operation(summary = "预览邮件模板（FreeMarker 按示例值渲染为 HTML）", description = "预览邮件模板（渲染后 HTML）")
	@PreAuthorize("@auth.decide('emailTemplate:detail')")
	@GetMapping("{id}/preview")
	public Result<String> preview(@PathVariable("id") Long id) {
		String html = emailTemplateService.previewRenderedHtml(id);
		return Result.success(html);
	}

	@Operation(summary = "添加邮件模板", description = "添加邮件模板")
	@PreAuthorize("@auth.decide('emailTemplate:create')")
	@PostMapping()
	public Result<String> create(@Validated(CreateGroup.class) @RequestBody EmailTemplateForm dto) {
		emailTemplateService.create(dto);

		Integer code = ADD_SUCCESS.getBizCode();
		String message = ADD_SUCCESS.getMessage();
		return Result.success(code, message);
	}

	@Operation(summary = "更新邮件模板元数据", description = "仅更新编码、名称、描述、主题、优先级、状态，不修改正文与变量")
	@PreAuthorize("@auth.decide('emailTemplate:update')")
	@PutMapping()
	public Result<String> updateMetadata(@Validated(UpdateGroup.class) @RequestBody EmailTemplateMetadataForm dto) {
		emailTemplateService.updateMetadata(dto);

		Integer code = UPDATE_SUCCESS.getBizCode();
		String message = UPDATE_SUCCESS.getMessage();
		return Result.success(code, message);
	}

	@Operation(summary = "仅更新邮件模板正文", description = "仅更新 content 字段")
	@PreAuthorize("@auth.decide('emailTemplate:update')")
	@PutMapping("{id}/content")
	public Result<String> updateContent(@PathVariable("id") Long id,
			@Validated @RequestBody EmailTemplateContentForm dto) {
		emailTemplateService.updateContent(id, dto);

		Integer code = UPDATE_SUCCESS.getBizCode();
		String message = UPDATE_SUCCESS.getMessage();
		return Result.success(code, message);
	}

	@Operation(summary = "仅更新变量列表 require_fields", description = "仅更新变量列表 require_fields")
	@PreAuthorize("@auth.decide('emailTemplate:update')")
	@PutMapping("{id}/require-fields")
	public Result<String> updateRequireFields(@PathVariable("id") Long id,
			@Validated @RequestBody EmailTemplateRequireFieldsForm form) {
		emailTemplateService.updateRequireFields(id, form);

		Integer code = UPDATE_SUCCESS.getBizCode();
		String message = UPDATE_SUCCESS.getMessage();
		return Result.success(code, message);
	}

	@Operation(summary = "删除邮件模板", description = "删除邮件模板")
	@PreAuthorize("@auth.decide('emailTemplate:delete')")
	@DeleteMapping()
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		emailTemplateService.removeBatchByIds(ids);

		Integer code = DELETE_SUCCESS.getBizCode();
		String message = DELETE_SUCCESS.getMessage();
		return Result.success(code, message);
	}

	@Operation(summary = "批量启用/禁用邮件模板", description = "批量启用/禁用邮件模板")
	@PreAuthorize("@auth.decide('emailTemplate:update')")
	@PutMapping("status")
	public Result<String> batchUpdateStatus(@Validated @RequestBody EmailTemplateStatusForm form) {
		emailTemplateService.batchUpdateStatus(form);

		Integer code = UPDATE_SUCCESS.getBizCode();
		String message = UPDATE_SUCCESS.getMessage();
		return Result.success(code, message);
	}

}
