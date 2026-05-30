package com.auth.service.system.schedule.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.query.SysJobQuery;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.auth.service.system.schedule.service.SysJobDefinitionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.*;

/**
 * 定时任务与 Quartz 操作接口
 *
 * @author Bunny
 */
@Tag(name = "定时任务-任务", description = "sys_job + Quartz")
@RequestMapping("/api/system/job")
@RestController
public class SysJobController {

	private final SysJobDefinitionService sysJobDefinitionService;

	public SysJobController(SysJobDefinitionService sysJobDefinitionService) {
		this.sysJobDefinitionService = sysJobDefinitionService;
	}

	@Operation(summary = "分页查询定时任务")
	@PreAuthorize("@auth.decide('sysJob:query')")
	@GetMapping
	public Result<PageResponse<SysJobPageResponse>> page(SysJobQuery query) {
		Page<SysJobEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysJobPageResponse> response = sysJobDefinitionService.getPage(pageParams, query);

		return Result.success(response);
	}

	@Operation(summary = "任务详情")
	@PreAuthorize("@auth.decide('sysJob:detail')")
	@GetMapping("{id}")
	public Result<SysJobDetailVO> detail(@PathVariable("id") Long id) {
		SysJobDetailVO detail = sysJobDefinitionService.getDetail(id);

		return Result.success(detail);
	}

	@Operation(summary = "新增定时任务")
	@PreAuthorize("@auth.decide('sysJob:create')")
	@PostMapping
	public Result<String> create(@Validated(CreateGroup.class) @RequestBody SysJobCreateForm form) {
		sysJobDefinitionService.create(form);

		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@Operation(summary = "修改定时任务")
	@PreAuthorize("@auth.decide('sysJob:update')")
	@PutMapping
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody SysJobUpdateForm form) {
		sysJobDefinitionService.update(form);

		return Result.success(null, MODIFY_SUCCESS.getMessage());
	}

	@Operation(summary = "删除定时任务")
	@PreAuthorize("@auth.decide('sysJob:delete')")
	@DeleteMapping("{id}")
	public Result<String> delete(@PathVariable("id") Long id) {
		sysJobDefinitionService.deleteById(id);

		return Result.success(null, DELETE_SUCCESS.getMessage());
	}

}
