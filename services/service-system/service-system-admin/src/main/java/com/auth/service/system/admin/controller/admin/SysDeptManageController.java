package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.dept.DeptClosureHealthVO;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.service.admin.DeptClosureHealthQueryService;
import com.auth.service.system.admin.service.admin.GrantTableService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 部门扩展管理
 *
 * @author Bunny
 */
@Tag(name = "部门扩展管理")
@RequestMapping("/api/system/dept")
@RestController
public class SysDeptManageController {

	private final GrantTableService grantTableService;

	private final DeptClosureHealthQueryService deptClosureHealthQueryService;

	public SysDeptManageController(GrantTableService grantTableService,
			DeptClosureHealthQueryService deptClosureHealthQueryService) {
		this.grantTableService = grantTableService;
		this.deptClosureHealthQueryService = deptClosureHealthQueryService;
	}

	@OperationLog(targetType = "DEPT_CLOSURE_HEALTH", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.DEPT_CLOSURE, operation = OperationLogKind.QUERY)
	@Operation(summary = "部门闭包表健康检查（只读运维）")
	@PreAuthorize("@auth.decide('sysDept:diagnose')")
	@GetMapping("/closure/health")
	public Result<DeptClosureHealthVO> closureHealth() {
		DeptClosureHealthVO healthVO = deptClosureHealthQueryService.inspect();
		return Result.success(healthVO);
	}

	@Operation(summary = "查询部门已分配角色")
	@PreAuthorize("@auth.decide('sysDept:query')")
	@GetMapping("/{deptId}/roles")
	public Result<List<GrantTableAssignedRoleRow>> listRoles(@PathVariable("deptId") Long deptId) {
		List<GrantTableAssignedRoleRow> rows = grantTableService.listAssignedRoles(GrantTableSubjectType.DEPT, deptId);
		return Result.success(rows);
	}

	@OperationLog(targetType = "DEPT_ROLE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.GRANT_TABLE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "全量覆盖部门角色授权")
	@PreAuthorize("@auth.decide('sysDept:update')")
	@PutMapping("/{deptId}/roles")
	public Result<Void> replaceRoles(@PathVariable("deptId") Long deptId,
			@Valid @RequestBody GrantTableAssignRoleForm form) {
		grantTableService.replaceOrgSubjectRoles(GrantTableSubjectType.DEPT, deptId, form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
