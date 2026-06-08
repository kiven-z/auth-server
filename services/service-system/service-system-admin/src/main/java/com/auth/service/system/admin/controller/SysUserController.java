package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.admin.service.SysUserService;
import com.auth.service.system.common.model.audit.SystemAuditBizModule;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DELETE_SUCCESS;

/**
 * 系统用户管理 Controller
 *
 * @author Bunny
 */
@Tag(name = "系统用户管理")
@RequiredArgsConstructor
@RequestMapping("/api/system/user")
@RestController
public class SysUserController {

	private final SysUserService sysUserService;

	@OperationLog(targetType = "USER_LIST", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.QUERY)
	@Operation(summary = "用户分页查询")
	@PreAuthorize("@auth.decide('sysUser:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysUserPageVO>> page(SysUserPageQuery query) {
		Page<UserEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysUserPageVO> result = sysUserService.getPage(pageParams, query);
		return Result.success(result);
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除用户")
	@PreAuthorize("@auth.decide('sysUser:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		sysUserService.deleteByIds(ids);
		return Result.success(null, DELETE_SUCCESS.getMessage());
	}

}
