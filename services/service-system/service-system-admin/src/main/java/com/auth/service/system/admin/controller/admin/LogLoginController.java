package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.system.admin.model.query.log.LogLoginLogQuery;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogDetailVO;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogPageVO;
import com.auth.service.system.admin.service.admin.LogLoginQueryService;
import com.auth.service.system.admin.service.admin.LogLoginService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 登录日志接口
 *
 * @author Bunny
 */
@Tag(name = "登录日志", description = "log_login")
@RequestMapping("/api/system/log/login")
@RestController
public class LogLoginController {

	private final LogLoginQueryService logLoginQueryService;

	private final LogLoginService logLoginService;

	public LogLoginController(LogLoginQueryService logLoginQueryService, LogLoginService logLoginService) {
		this.logLoginQueryService = logLoginQueryService;
		this.logLoginService = logLoginService;
	}

	@Operation(summary = "分页查询登录日志")
	@PreAuthorize("@auth.decide('sysLogLogin:query')")
	@GetMapping("page")
	public Result<PageResponse<LogLoginLogPageVO>> page(LogLoginLogQuery query) {
		Page<LoginLogEntity> page = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<LogLoginLogPageVO> response = logLoginQueryService.getPage(page, query);
		return Result.success(response);
	}

	@Operation(summary = "登录日志详情")
	@PreAuthorize("@auth.decide('sysLogLogin:detail')")
	@GetMapping("{id}")
	public Result<LogLoginLogDetailVO> detail(@PathVariable("id") Long id) {
		LogLoginLogDetailVO detailVO = logLoginQueryService.getDetail(id);
		return Result.success(detailVO);
	}

	@Operation(summary = "批量删除登录日志")
	@PreAuthorize("@auth.decide('sysLogLogin:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		logLoginService.removeBatchByIds(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
