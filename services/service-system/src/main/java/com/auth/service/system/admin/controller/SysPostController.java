package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.query.SysPostQuery;
import com.auth.service.system.admin.model.vo.post.SysPostPageResponse;
import com.auth.service.system.admin.service.SysPostService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 系统岗位管理 Controller
 *
 * @author Bunny
 */
@Tag(name = "系统岗位管理")
@Slf4j
@RequestMapping("/api/system/post")
@RestController
public class SysPostController {

	private final SysPostService sysPostService;

	public SysPostController(SysPostService sysPostService) {
		this.sysPostService = sysPostService;
	}

	@Operation(summary = "岗位分页查询")
	@PreAuthorize("@auth.decide('sysPost:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysPostPageResponse>> page(SysPostQuery query) {
		Page<SysPostEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysPostPageResponse> result = sysPostService.getPage(pageParams, query);
		return Result.success(result);
	}

	@Operation(summary = "新增岗位")
	@PreAuthorize("@auth.decide('sysPost:create')")
	@PostMapping
	public Result<Void> create(@Valid @RequestBody SysPostForm form) {
		sysPostService.create(form);
		return Result.success();
	}

	@Operation(summary = "更新岗位")
	@PreAuthorize("@auth.decide('sysPost:update')")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable("id") Long id, @Valid @RequestBody SysPostForm form) {
		sysPostService.update(id, form);
		return Result.success();
	}

}
