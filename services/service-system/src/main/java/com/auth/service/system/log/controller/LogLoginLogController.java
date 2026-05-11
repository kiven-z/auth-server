package com.auth.service.system.log.controller;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.*;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.log.model.entity.LogLoginLogEntity;
import com.auth.service.system.log.model.query.LogLoginLogQuery;
import com.auth.service.system.log.model.vo.LogLoginLogDetailVO;
import com.auth.service.system.log.model.vo.LogLoginLogPageVO;
import com.auth.service.system.log.service.LogLoginLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 登录日志接口
 *
 * @author Bunny
 */
@Tag(name = "登录日志", description = "log_login")
@RequestMapping("/api/system/log/login")
@RestController
public class LogLoginLogController {

	private final LogLoginLogService logLoginLogService;

	public LogLoginLogController(LogLoginLogService logLoginLogService) {
		this.logLoginLogService = logLoginLogService;
	}

	@Operation(summary = "分页查询登录日志")
	@PreAuthorize("@auth.decide('sysLogLogin:query')")
	@GetMapping("page")
	public Result<PageResponse<LogLoginLogPageVO>> page(LogLoginLogQuery query) {
		Page<LogLoginLogEntity> page = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<LogLoginLogPageVO> response = logLoginLogService.getPage(page, query);
		return Result.success(response);
	}

	@Operation(summary = "登录日志详情")
	@PreAuthorize("@auth.decide('sysLogLogin:detail')")
	@GetMapping("{id}")
	public Result<LogLoginLogDetailVO> detail(@PathVariable("id") Long id) {
		LogLoginLogDetailVO detailVO = logLoginLogService.getDetail(id);
		return Result.success(detailVO);
	}

	@Operation(summary = "批量删除登录日志")
	@PreAuthorize("@auth.decide('sysLogLogin:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		logLoginLogService.removeBatchByIds(ids);
		return Result.success(null, DELETE_SUCCESS.getMessage());
	}

}
