package com.auth.service.system.schedule.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.common.model.SystemAuditBizModule;
import com.auth.service.system.schedule.model.entity.SysJobGroupEntity;
import com.auth.service.system.schedule.model.form.SysJobGroupForm;
import com.auth.service.system.schedule.model.form.SysJobGroupUpdateForm;
import com.auth.service.system.schedule.model.query.SysJobGroupQuery;
import com.auth.service.system.schedule.model.vo.SysJobGroupDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobGroupPageResponse;
import com.auth.service.system.schedule.service.SysJobGroupService;
import com.auth.service.system.schedule.service.SysJobOperationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.*;

/**
 * 定时任务分组接口
 *
 * @author Bunny
 */
@Tag(name = "定时任务-分组", description = "sys_job_group")
@RequestMapping("/api/system/job-group")
@RestController
public class SysJobGroupController {

	private final SysJobGroupService sysJobGroupService;

	private final SysJobOperationService sysJobOperationService;

	public SysJobGroupController(SysJobGroupService sysJobGroupService, SysJobOperationService sysJobOperationService) {
		this.sysJobGroupService = sysJobGroupService;
		this.sysJobOperationService = sysJobOperationService;
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
		int max = Math.max(limit, 1);
		limit = Math.min(max, 60);
		List<SysJobGroupPageResponse> responses = sysJobGroupService.listEnabledOptions(keyword, limit);

		return Result.success(responses);
	}

	@Operation(summary = "任务分组详情")
	@PreAuthorize("@auth.decide('sysJobGroup:query')")
	@GetMapping("{id}")
	public Result<SysJobGroupDetailVO> detail(@PathVariable("id") Long id) {
		SysJobGroupDetailVO detail = sysJobGroupService.getDetail(id);

		return Result.success(detail);
	}

	@Operation(summary = "新增任务分组")
	@PreAuthorize("@auth.decide('sysJobGroup:create')")
	@PostMapping
	public Result<String> create(@Validated(CreateGroup.class) @RequestBody SysJobGroupForm form) {
		sysJobGroupService.create(form);
		return Result.success(null, ADD_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "JOB_GROUP", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_JOB_GROUP, operation = OperationLogKind.UPDATE)
	@Operation(summary = "修改任务分组")
	@PreAuthorize("@auth.decide('sysJobGroup:update')")
	@PutMapping
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody SysJobGroupUpdateForm form) {
		sysJobGroupService.update(form);
		return Result.success(null, MODIFY_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "JOB_GROUP", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_JOB_GROUP, operation = OperationLogKind.DELETE)
	@Operation(summary = "删除任务分组")
	@PreAuthorize("@auth.decide('sysJobGroup:delete')")
	@DeleteMapping("{id}")
	public Result<String> delete(@PathVariable("id") Long id) {
		sysJobGroupService.deleteById(id);
		return Result.success(null, DELETE_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "JOB_GROUP", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_JOB_GROUP, operation = OperationLogKind.UPDATE)
	@Operation(summary = "暂停分组下全部任务")
	@PreAuthorize("@auth.decide('sysJob:update')")
	@PutMapping("{groupCode}/pause-jobs")
	public Result<String> pauseAllJobs(@PathVariable("groupCode") String groupCode) {
		sysJobOperationService.pauseAllInGroup(groupCode);
		return Result.success(null, MODIFY_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "JOB_GROUP", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_JOB_GROUP, operation = OperationLogKind.UPDATE)
	@Operation(summary = "恢复分组下全部任务")
	@PreAuthorize("@auth.decide('sysJob:update')")
	@PutMapping("{groupCode}/resume-jobs")
	public Result<String> resumeAllJobs(@PathVariable("groupCode") String groupCode) {
		sysJobOperationService.resumeAllInGroup(groupCode);
		return Result.success(null, MODIFY_SUCCESS.getMessage());
	}

}
