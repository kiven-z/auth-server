package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.admin.model.query.LogAuthorizationAuditQuery;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditDetailVO;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditPageVO;
import com.auth.service.system.admin.service.LogAuthorizationAuditService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 权限决策审计日志接口
 *
 * @author Bunny
 */
@Tag(name = "权限决策审计日志", description = "log_authorization_audit")
@RequestMapping("/api/system/log/authorization-audit")
@RestController
public class LogAuthorizationAuditController {

	private final LogAuthorizationAuditService logAuthorizationAuditService;

	public LogAuthorizationAuditController(LogAuthorizationAuditService logAuthorizationAuditService) {
		this.logAuthorizationAuditService = logAuthorizationAuditService;
	}

	@Operation(summary = "分页查询权限决策审计")
	@PreAuthorize("@auth.decide('sysLogAuthorizationAudit:query')")
	@GetMapping("page")
	public Result<PageResponse<LogAuthorizationAuditPageVO>> page(LogAuthorizationAuditQuery query) {
		Page<LogAuthorizationAuditEntity> page = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<LogAuthorizationAuditPageVO> response = logAuthorizationAuditService.getPage(page, query);
		return Result.success(response);
	}

	@Operation(summary = "权限决策审计详情")
	@PreAuthorize("@auth.decide('sysLogAuthorizationAudit:detail')")
	@GetMapping("{id}")
	public Result<LogAuthorizationAuditDetailVO> detail(@PathVariable("id") Long id) {
		LogAuthorizationAuditDetailVO detail = logAuthorizationAuditService.getDetail(id);
		return Result.success(detail);
	}

	@Operation(summary = "批量删除权限决策审计（仅接收 ID 列表）")
	@PreAuthorize("@auth.decide('sysLogAuthorizationAudit:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		logAuthorizationAuditService.removeBatchByIds(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
