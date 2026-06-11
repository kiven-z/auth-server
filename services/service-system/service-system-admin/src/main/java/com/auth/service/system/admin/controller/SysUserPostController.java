package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.user.UserPostPageQuery;
import com.auth.service.system.admin.model.vo.userpost.UserPostPageVO;
import com.auth.service.system.admin.service.SysUserPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
