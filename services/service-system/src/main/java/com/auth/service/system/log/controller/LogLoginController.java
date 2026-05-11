package com.auth.service.system.log.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.log.model.entity.LogLoginEntity;
import com.auth.service.system.log.model.query.LogLoginLogQuery;
import com.auth.service.system.log.model.vo.LogLoginLogDetailVO;
import com.auth.service.system.log.model.vo.LogLoginLogPageVO;
import com.auth.service.system.log.service.LogLoginService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.DELETE_SUCCESS;

/**
 * 登录日志接口
 *
 * @author Bunny
 */
@Tag(name = "登录日志", description = "log_login")
@RequestMapping("/api/system/log/login")
@RestController
public class LogLoginController {

	private final LogLoginService logLoginService;

	public LogLoginController(LogLoginService logLoginService) {
		this.logLoginService = logLoginService;
	}

	@Operation(summary = "分页查询登录日志")
	@PreAuthorize("@auth.decide('sysLogLogin:query')")
	@GetMapping("page")
	public Result<PageResponse<LogLoginLogPageVO>> page(LogLoginLogQuery query) {
		Page<LogLoginEntity> page = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<LogLoginLogPageVO> response = logLoginService.getPage(page, query);
		return Result.success(response);
	}

	@Operation(summary = "登录日志详情")
	@PreAuthorize("@auth.decide('sysLogLogin:detail')")
	@GetMapping("{id}")
	public Result<LogLoginLogDetailVO> detail(@PathVariable("id") Long id) {
		LogLoginLogDetailVO detailVO = logLoginService.getDetail(id);
		return Result.success(detailVO);
	}

	@Operation(summary = "批量删除登录日志")
	@PreAuthorize("@auth.decide('sysLogLogin:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		logLoginService.removeBatchByIds(ids);
		return Result.success(null, DELETE_SUCCESS.getMessage());
	}

}
