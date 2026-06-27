package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserDetailVO;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.admin.model.vo.user.SysUserProfileVO;
import com.auth.service.system.admin.model.vo.user.SysUserSearchItemVO;
import com.auth.service.system.admin.service.admin.SysUserQueryService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统用户只读查询接口（档案、关键词搜索）
 *
 * @author Bunny
 */
@Tag(name = "系统用户-查询", description = "用户档案与关键词检索")
@RequiredArgsConstructor
@RequestMapping("/api/system/user")
@RestController
public class SysUserQueryController {

	private final SysUserQueryService sysUserQueryService;

	@OperationLog(targetType = "USER_LIST", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.QUERY)
	@Operation(summary = "用户分页查询")
	@PreAuthorize("@auth.decide('sysUser:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysUserPageVO>> page(SysUserPageQuery query) {
		Page<UserEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysUserPageVO> result = sysUserQueryService.getPage(pageParams, query);
		return Result.success(result);
	}

	@AuthenticatedApi
	@Operation(summary = "用户档案")
	@GetMapping("/{userId}/profile")
	public Result<SysUserProfileVO> profile(@PathVariable("userId") Long userId) {
		SysUserProfileVO profile = sysUserQueryService.getProfile(userId);
		return Result.success(profile);
	}

	@Operation(summary = "用户详情")
	@PreAuthorize("@auth.decide('sysUser:query')")
	@GetMapping("/{userId}/detail")
	public Result<SysUserDetailVO> detail(@PathVariable("userId") Long userId) {
		SysUserDetailVO detail = sysUserQueryService.getDetail(userId);
		return Result.success(detail);
	}

	@AuthenticatedApi
	@Operation(summary = "用户关键词搜索")
	@GetMapping("/search")
	public Result<List<SysUserSearchItemVO>> search(
			@Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
			@Parameter(description = "返回条数上限，默认 20，最大 50") @RequestParam(required = false,
					defaultValue = "20") Integer limit) {

		List<SysUserSearchItemVO> data = sysUserQueryService.searchByKeyword(keyword, limit);
		return Result.success(data);
	}

}
