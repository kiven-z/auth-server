package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.userdept.UserDeptAssignForm;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;
import com.auth.service.system.admin.service.SysUserDeptService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户部门关联管理
 *
 * @author Bunny
 */
@Tag(name = "用户部门关联")
@RequiredArgsConstructor
@RequestMapping("/api/system/user-dept")
@RestController
public class SysUserDeptController {

	private final SysUserDeptService sysUserDeptService;

	@Operation(summary = "分页查询用户部门关联")
	@PreAuthorize("@auth.decide('sysUserDept:query')")
	@GetMapping("/{userId}/page")
	public Result<PageResponse<UserDeptPageVO>> pageDepts(@PathVariable("userId") Long userId,
			UserDeptPageQuery query) {
		PageResponse<UserDeptPageVO> page = sysUserDeptService.getPage(userId, query);
		return Result.success(page);
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.CREATE)
	@Operation(summary = "新增用户部门关联")
	@PreAuthorize("@auth.decide('sysUserDept:create')")
	@PostMapping("/{userId}")
	public Result<Void> create(@PathVariable("userId") Long userId, @Valid @RequestBody UserDeptAssignForm form) {
		sysUserDeptService.create(userId, form);
		return Result.success();
	}

}
