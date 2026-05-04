package com.auth.service.system.schedule.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobStatusForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.query.SysJobQuery;
import com.auth.service.system.schedule.model.vo.QuartzTaskClassVO;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.auth.service.system.schedule.service.SysJobService;
import com.auth.service.system.schedule.support.QuartzTaskRegistry;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.*;

/**
 * 定时任务与 Quartz 操作接口
 *
 * @author Bunny
 */
@Tag(name = "定时任务-任务", description = "sys_job + Quartz")
@RequestMapping("/api/system/job")
@RestController
public class SysJobController {

	private final SysJobService sysJobService;

	private final QuartzTaskRegistry quartzTaskRegistry;

	public SysJobController(SysJobService sysJobService, QuartzTaskRegistry quartzTaskRegistry) {
		this.sysJobService = sysJobService;
		this.quartzTaskRegistry = quartzTaskRegistry;
	}

	@Operation(summary = "@QuartzTask 白名单类列表")
	@PreAuthorize("@auth.decide('sysJob:query')")
	@GetMapping("quartz-classes")
	public Result<List<QuartzTaskClassVO>> quartzClasses() {
		List<QuartzTaskClassVO> list = quartzTaskRegistry.listAll();

		return Result.success(list);
	}

	@Operation(summary = "分页查询定时任务")
	@PreAuthorize("@auth.decide('sysJob:query')")
	@GetMapping
	public Result<PageResponse<SysJobPageResponse>> page(SysJobQuery query) {
		Page<SysJobEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysJobPageResponse> response = sysJobService.getPage(pageParams, query);

		return Result.success(response);
	}

	@Operation(summary = "任务详情")
	@PreAuthorize("@auth.decide('sysJob:detail')")
	@GetMapping("{id}")
	public Result<SysJobDetailVO> detail(@PathVariable("id") Long id) {
		SysJobDetailVO detail = sysJobService.getDetail(id);

		return Result.success(detail);
	}

	@Operation(summary = "新增定时任务")
	@PreAuthorize("@auth.decide('sysJob:create')")
	@PostMapping
	public Result<String> create(@Validated(CreateGroup.class) @RequestBody SysJobCreateForm form) {
		sysJobService.create(form);

		return Result.success(ADD_SUCCESS.getBizCode(), ADD_SUCCESS.getMessage());
	}

	@Operation(summary = "修改定时任务")
	@PreAuthorize("@auth.decide('sysJob:update')")
	@PutMapping
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody SysJobUpdateForm form) {
		sysJobService.update(form);

		return Result.success(UPDATE_SUCCESS.getBizCode(), UPDATE_SUCCESS.getMessage());
	}

	@Operation(summary = "删除定时任务")
	@PreAuthorize("@auth.decide('sysJob:delete')")
	@DeleteMapping("{id}")
	public Result<String> delete(@PathVariable("id") Long id) {
		sysJobService.deleteById(id);

		return Result.success(DELETE_SUCCESS.getBizCode(), DELETE_SUCCESS.getMessage());
	}

	@Operation(summary = "批量启停任务")
	@PreAuthorize("@auth.decide('sysJob:update')")
	@PutMapping("status")
	public Result<String> batchStatus(@Validated @RequestBody SysJobStatusForm form) {
		sysJobService.batchUpdateStatus(form);

		return Result.success(UPDATE_SUCCESS.getBizCode(), UPDATE_SUCCESS.getMessage());
	}

	@Operation(summary = "立即执行一次")
	@PreAuthorize("@auth.decide('sysJob:update')")
	@PostMapping("{id}/run")
	public Result<String> runOnce(@PathVariable("id") Long id) {
		sysJobService.runOnce(id);

		return Result.success(UPDATE_SUCCESS.getBizCode(), UPDATE_SUCCESS.getMessage());
	}

	@Operation(summary = "按分组一键暂停")
	@PreAuthorize("@auth.decide('sysJob:update')")
	@PutMapping("group/{groupCode}/pause")
	public Result<String> pauseGroup(@PathVariable("groupCode") String groupCode) {
		sysJobService.pauseAllInGroup(groupCode);

		return Result.success(UPDATE_SUCCESS.getBizCode(), UPDATE_SUCCESS.getMessage());
	}

	@Operation(summary = "按分组一键恢复")
	@PreAuthorize("@auth.decide('sysJob:update')")
	@PutMapping("group/{groupCode}/resume")
	public Result<String> resumeGroup(@PathVariable("groupCode") String groupCode) {
		sysJobService.resumeAllInGroup(groupCode);

		return Result.success(UPDATE_SUCCESS.getBizCode(), UPDATE_SUCCESS.getMessage());
	}

}
