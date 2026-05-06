package com.auth.service.system.common.controller;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.common.model.ModuleOption;
import com.auth.service.system.common.service.SystemModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统模块控制器。
 * <p>
 * 提供系统模块查询接口，用于前端远程搜索下拉框或模块选择器。 模块数据由
 * {@link com.auth.service.system.common.model.SystemModule} 枚举定义，为静态配置。
 * </p>
 *
 * @author Bunny
 */
@Tag(name = "系统模块")
@RequestMapping("/api/system/module")
@RestController
public class SystemModuleController {

	private final SystemModuleService systemModuleService;

	public SystemModuleController(SystemModuleService systemModuleService) {
		this.systemModuleService = systemModuleService;
	}

	@Operation(summary = "获取系统模块选项列表", description = "支持关键词远程搜索、分类筛选，用于下拉选择器")
	@GetMapping("/list")
	public Result<List<ModuleOption>> list(
			@Parameter(description = "搜索关键词，模糊匹配枚举 name 或中文描述") @RequestParam(required = false) String keyword,

			@Parameter(description = "模块分类筛选：SYS-系统业务、USER-用户相关、GRANT-授权、POLICY-策略、AUDIT-审计日志、LOG-操作日志") @RequestParam(
					required = false) String type,

			@Parameter(description = "返回条数上限") @RequestParam(required = false, defaultValue = "15") Integer limit) {
		List<ModuleOption> list = systemModuleService.list(keyword, type, limit);
		return Result.success(list);
	}

}
