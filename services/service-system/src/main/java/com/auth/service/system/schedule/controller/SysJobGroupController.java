package com.auth.service.system.schedule.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.service.system.schedule.model.entity.SysJobGroupEntity;
import com.auth.service.system.schedule.model.form.SysJobGroupForm;
import com.auth.service.system.schedule.model.form.SysJobGroupUpdateForm;
import com.auth.service.system.schedule.model.query.SysJobGroupQuery;
import com.auth.service.system.schedule.model.vo.SysJobGroupPageResponse;
import com.auth.service.system.schedule.service.SysJobGroupService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.*;

/**
 * 定时任务分组接口
 *
 * @author Bunny
 */
@Tag(name = "定时任务-分组", description = "sys_job_group")
@RequestMapping("/api/system/v1/job-group")
@RestController
public class SysJobGroupController {

	private final SysJobGroupService sysJobGroupService;

	public SysJobGroupController(SysJobGroupService sysJobGroupService) {
		this.sysJobGroupService = sysJobGroupService;
	}

	@Operation(summary = "分页查询任务分组")
	@PreAuthorize("@auth.decide('sysJobGroup:query')")
	@GetMapping
	public Result<PageResponse<SysJobGroupPageResponse>> page(SysJobGroupQuery query) {
		Page<SysJobGroupEntity> page = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysJobGroupPageResponse> response = sysJobGroupService.getPage(page, query);

		return Result.success(response);
	}

	@Operation(summary = "启用分组下拉/远程搜索")
	@PreAuthorize("@auth.decide('sysJobGroup:query')")
	@GetMapping("options")
	public Result<List<SysJobGroupPageResponse>> options(
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "limit", defaultValue = "20") int limit) {
		List<SysJobGroupPageResponse> responses = sysJobGroupService.listEnabledOptions(keyword, limit);

		return Result.success(responses);
	}

	@Operation(summary = "新增任务分组")
	@PreAuthorize("@auth.decide('sysJobGroup:create')")
	@PostMapping
	public Result<String> create(@Validated(CreateGroup.class) @RequestBody SysJobGroupForm form) {
		sysJobGroupService.create(form);
		return Result.success(ADD_SUCCESS.getBizCode(), ADD_SUCCESS.getMessage());
	}

	@Operation(summary = "修改任务分组")
	@PreAuthorize("@auth.decide('sysJobGroup:update')")
	@PutMapping
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody SysJobGroupUpdateForm form) {
		sysJobGroupService.update(form);
		return Result.success(UPDATE_SUCCESS.getBizCode(), UPDATE_SUCCESS.getMessage());
	}

	@Operation(summary = "删除任务分组")
	@PreAuthorize("@auth.decide('sysJobGroup:delete')")
	@DeleteMapping("{id}")
	public Result<String> delete(@PathVariable("id") Long id) {
		sysJobGroupService.deleteById(id);
		return Result.success(DELETE_SUCCESS.getBizCode(), DELETE_SUCCESS.getMessage());
	}

}
