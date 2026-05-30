package com.auth.service.system.log.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.log.model.entity.LogUserPasswordHistoryEntity;
import com.auth.service.system.log.model.query.LogUserPasswordHistoryQuery;
import com.auth.service.system.log.model.vo.LogUserPasswordHistoryDetailVO;
import com.auth.service.system.log.model.vo.LogUserPasswordHistoryPageVO;
import com.auth.service.system.log.service.LogUserPasswordHistoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DELETE_SUCCESS;

/**
 * 密码历史日志接口
 *
 * @author Bunny
 */
@Tag(name = "密码历史日志", description = "log_user_password_history")
@RequestMapping("/api/system/log/password-history")
@RestController
public class LogUserPasswordHistoryController {

	private final LogUserPasswordHistoryService logUserPasswordHistoryService;

	public LogUserPasswordHistoryController(LogUserPasswordHistoryService logUserPasswordHistoryService) {
		this.logUserPasswordHistoryService = logUserPasswordHistoryService;
	}

	@Operation(summary = "分页查询密码历史日志")
	@PreAuthorize("@auth.decide('sysLogPasswordHistory:query')")
	@GetMapping("page")
	public Result<PageResponse<LogUserPasswordHistoryPageVO>> page(LogUserPasswordHistoryQuery query) {
		Page<LogUserPasswordHistoryEntity> page = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<LogUserPasswordHistoryPageVO> response = logUserPasswordHistoryService.getPage(page, query);
		return Result.success(response);
	}

	@Operation(summary = "密码历史日志详情")
	@PreAuthorize("@auth.decide('sysLogPasswordHistory:detail')")
	@GetMapping("{id}")
	public Result<LogUserPasswordHistoryDetailVO> detail(@PathVariable("id") Long id) {
		LogUserPasswordHistoryDetailVO detailVO = logUserPasswordHistoryService.getDetail(id);
		return Result.success(detailVO);
	}

	@Operation(summary = "批量删除密码历史日志")
	@PreAuthorize("@auth.decide('sysLogPasswordHistory:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		logUserPasswordHistoryService.removeBatchByIds(ids);
		return Result.success(null, DELETE_SUCCESS.getMessage());
	}

}
