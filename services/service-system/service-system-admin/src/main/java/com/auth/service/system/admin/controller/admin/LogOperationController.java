package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.LogOperationEntity;
import com.auth.service.system.admin.model.query.log.LogOperationQuery;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationDetailVO;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationPageVO;
import com.auth.service.system.admin.service.admin.LogOperationQueryService;
import com.auth.service.system.admin.service.admin.LogOperationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 操作日志接口
 *
 * @author Bunny
 */
@Tag(name = "操作日志", description = "log_operation")
@RequestMapping("/api/system/log/operation")
@RestController
public class LogOperationController {

	private final LogOperationQueryService logOperationQueryService;

	private final LogOperationService logOperationService;

	public LogOperationController(LogOperationQueryService logOperationQueryService,
			LogOperationService logOperationService) {
		this.logOperationQueryService = logOperationQueryService;
		this.logOperationService = logOperationService;
	}

	@Operation(summary = "分页查询操作日志")
	@PreAuthorize("@auth.decide('log:operation:query')")
	@GetMapping("page")
	public Result<PageResponse<LogOperationPageVO>> page(LogOperationQuery query) {
		Page<LogOperationEntity> page = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<LogOperationPageVO> response = logOperationQueryService.getPage(page, query);
		return Result.success(response);
	}

	@Operation(summary = "操作日志详情")
	@PreAuthorize("@auth.decide('log:operation:detail')")
	@GetMapping("{id}")
	public Result<LogOperationDetailVO> detail(@PathVariable("id") Long id) {
		LogOperationDetailVO detail = logOperationQueryService.getDetail(id);
		return Result.success(detail);
	}

	@Operation(summary = "批量删除操作日志")
	@PreAuthorize("@auth.decide('log:operation:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		logOperationService.removeBatchByIds(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
