package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;
import com.auth.service.system.admin.service.SysUserDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

}
