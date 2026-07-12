package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.query.MessageTemplateQuery;
import com.auth.service.system.message.model.vo.MessageTemplateDetailVO;
import com.auth.service.system.message.model.vo.MessageTemplatePageResponse;
import com.auth.service.system.message.service.SysMessageTemplateService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息模板公共读接口（跨渠道分页、详情）
 *
 * @author Bunny
 */
@Tag(name = "消息模板相关", description = "消息模板跨渠道公共查询")
@RequestMapping("/api/system/message-template")
@RestController
@Validated
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

	@Operation(summary = "消息模板详情", description = "按主键查询；channel 须与库中渠道一致")
	@PreAuthorize("@auth.decide('message:template:detail')")
	@GetMapping("{id}")
	public Result<MessageTemplateDetailVO> getMessageTemplateById(@PathVariable("id") Long id,
			@RequestParam @NotBlank(message = "消息渠道不能为空") String channel) {
		MessageTemplateDetailVO detailVO = sysMessageTemplateService.getMessageTemplateById(id, channel);
		return Result.success(detailVO);
	}

}
