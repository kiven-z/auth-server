package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.userpost.UserPostAssignForm;
import com.auth.service.system.admin.model.form.userpost.UserPostRelationUpdateForm;
import com.auth.service.system.admin.model.query.user.UserPostPageQuery;
import com.auth.service.system.admin.model.vo.userpost.UserPostPageVO;
import com.auth.service.system.admin.service.SysUserPostService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户岗位关联管理
 *
 * @author Bunny
 */
@Tag(name = "用户岗位关联")
@RequiredArgsConstructor
@RequestMapping("/api/system/user-post")
@RestController
public class SysUserPostController {

	private final SysUserPostService sysUserPostService;

	@Operation(summary = "分页查询用户岗位关联")
	@PreAuthorize("@auth.decide('sysUserPost:query')")
	@GetMapping("/{userId}/page")
	public Result<PageResponse<UserPostPageVO>> pagePosts(@PathVariable("userId") Long userId,
			UserPostPageQuery query) {
		PageResponse<UserPostPageVO> page = sysUserPostService.getPage(userId, query);
		return Result.success(page);
	}

	@OperationLog(targetType = "USER_POST", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.CREATE)
	@Operation(summary = "新增用户岗位关联")
	@PreAuthorize("@auth.decide('sysUserPost:create')")
	@PostMapping("/{userId}")
	public Result<Void> create(@PathVariable("userId") Long userId, @Valid @RequestBody UserPostAssignForm form) {
		sysUserPostService.create(userId, form);
		return Result.success();
	}

	@OperationLog(targetType = "USER_POST", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新用户岗位关联")
	@PreAuthorize("@auth.decide('sysUserPost:update')")
	@PutMapping("/{userId}/{id}")
	public Result<Void> update(@PathVariable("userId") Long userId, @PathVariable("id") Long id,
			@Valid @RequestBody UserPostRelationUpdateForm form) {
		sysUserPostService.update(userId, id, form);
		return Result.success();
	}

	@OperationLog(targetType = "USER_POST", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除用户岗位关联")
	@PreAuthorize("@auth.decide('sysUserPost:delete')")
	@DeleteMapping("/{userId}")
	public Result<Void> removeBatch(@PathVariable("userId") Long userId, @RequestBody List<Long> ids) {
		sysUserPostService.removeBatch(userId, ids);
		return Result.success();
	}

	@OperationLog(targetType = "USER_POST", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.DELETE)
	@Operation(summary = "清空用户全部岗位关联")
	@PreAuthorize("@auth.decide('sysUserPost:delete')")
	@DeleteMapping("/{userId}/all")
	public Result<Void> removeAll(@PathVariable("userId") Long userId) {
		sysUserPostService.removeAll(userId);
		return Result.success();
	}

}
