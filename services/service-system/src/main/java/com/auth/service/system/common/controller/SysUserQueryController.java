package com.auth.service.system.common.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.common.model.SysUserSearchItemResponse;
import com.auth.service.system.common.model.vo.SysUserProfileResponse;
import com.auth.service.system.common.service.SysUserQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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

	/**
	 * 查询用户档案（基本信息、部门、岗位）
	 * @param userId 用户ID
	 * @return 聚合数据
	 */
	@Operation(summary = "用户档案")
	@PreAuthorize("@auth.decide('sysUser:query')")
	@GetMapping("/{userId}/profile")
	public Result<SysUserProfileResponse> profile(@PathVariable("userId") Long userId) {
		return Result.success(sysUserQueryService.getProfile(userId));
	}

	/**
	 * 关键词搜索用户（远程下拉），匹配用户名、昵称、邮箱、手机、工号。
	 * @param keyword 关键字
	 * @param limit 最大返回条数，默认 20，最大 50
	 * @return 用户简要列表
	 */
	@Operation(summary = "用户关键词搜索")
	@PreAuthorize("@auth.decide('sysUser:query')")
	@GetMapping("/search")
	public Result<List<SysUserSearchItemResponse>> search(
			@Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
			@Parameter(description = "返回条数上限，默认 20，最大 50") @RequestParam(required = false,
					defaultValue = "20") Integer limit) {

		return Result.success(sysUserQueryService.searchByKeyword(keyword, limit));
	}

}
