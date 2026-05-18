package com.auth.service.system.schedule.controller;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.schedule.model.form.SysJobStatusForm;
import com.auth.service.system.schedule.service.SysJobOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.auth.service.system.common.exception.code.common.SystemCommonResultCode.UPDATE_SUCCESS;

/**
 * 定时任务运行态操作接口
 *
 * @author Bunny
 */
@Tag(name = "定时任务-操作", description = "启停、立即执行、分组操作")
@RequestMapping("/api/system/job")
@RestController
public class SysJobOperationController {

	private final SysJobOperationService sysJobOperationService;

	public SysJobOperationController(SysJobOperationService sysJobOperationService) {
		this.sysJobOperationService = sysJobOperationService;
	}

	@Operation(summary = "批量启停任务")
	@PreAuthorize("@auth.decide('sysJob:update')")
	@PutMapping("status")
	public Result<String> batchStatus(@Validated @RequestBody SysJobStatusForm form) {
		sysJobOperationService.batchUpdateStatus(form);
		return Result.success(null, UPDATE_SUCCESS.getMessage());
	}

	@Operation(summary = "立即执行一次")
	@PreAuthorize("@auth.decide('sysJob:update')")
	@PostMapping("{id}/run")
	public Result<String> runOnce(@PathVariable("id") Long id) {
		sysJobOperationService.runOnce(id);
		return Result.success(null, UPDATE_SUCCESS.getMessage());
	}

	@Operation(summary = "按分组一键暂停")
	@PreAuthorize("@auth.decide('sysJob:update')")
	@PutMapping("group/{groupCode}/pause")
	public Result<String> pauseGroup(@PathVariable("groupCode") String groupCode) {
		sysJobOperationService.pauseAllInGroup(groupCode);
		return Result.success(null, UPDATE_SUCCESS.getMessage());
	}

	@Operation(summary = "按分组一键恢复")
	@PreAuthorize("@auth.decide('sysJob:update')")
	@PutMapping("group/{groupCode}/resume")
	public Result<String> resumeGroup(@PathVariable("groupCode") String groupCode) {
		sysJobOperationService.resumeAllInGroup(groupCode);
		return Result.success(null, UPDATE_SUCCESS.getMessage());
	}

}
