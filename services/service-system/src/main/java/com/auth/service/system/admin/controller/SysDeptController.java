package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditBizModule;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.query.SysDeptQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.auth.service.system.admin.service.SysDeptService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统部门管理 Controller（查询）。
 *
 * @author Bunny
 */
@Tag(name = "系统部门管理")
@RequestMapping("/api/system/dept")
@RestController
public class SysDeptController {

	private final SysDeptService sysDeptService;

	public SysDeptController(SysDeptService sysDeptService) {
		this.sysDeptService = sysDeptService;
	}

	@OperationLog(targetType = "DEPT_LIST", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuditBizModule.SYS_DEPT, operation = OperationLogKind.QUERY)
	@Operation(summary = "部门扁平列表（全量，前端建树）")
	@PreAuthorize("@auth.decide('sysDept:query')")
	@GetMapping("/list")
	public Result<List<SysDeptListVO>> list(SysDeptQuery query) {
		return Result.success(sysDeptService.listFlat(query));
	}

	@Operation(summary = "部门分页查询（表格视图）")
	@PreAuthorize("@auth.decide('sysDept:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysDeptListVO>> page(SysDeptQuery query) {
		Page<SysDeptEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysDeptListVO> result = sysDeptService.pageFlat(pageParams, query);
		return Result.success(result);
	}

}
