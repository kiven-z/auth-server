package com.auth.service.system.log.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.log.model.entity.JobLogEntity;
import com.auth.service.system.log.model.query.LogJobQuery;
import com.auth.service.system.log.model.vo.LogJobDetailVO;
import com.auth.service.system.log.model.vo.LogJobPageResponse;
import com.auth.service.system.log.service.LogJobService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.DELETE_SUCCESS;

/**
 * 定时任务执行日志接口
 *
 * @author Bunny
 */
@Tag(name = "定时任务-日志", description = "log_job")
@RequestMapping("/api/system/job-log")
@RestController
public class LogJobController {

	private final LogJobService logJobService;

	public LogJobController(LogJobService logJobService) {
		this.logJobService = logJobService;
	}

	@Operation(summary = "分页查询任务日志")
	@PreAuthorize("@auth.decide('sysJobLog:query')")
	@GetMapping
	public Result<PageResponse<LogJobPageResponse>> page(LogJobQuery query) {
		Page<JobLogEntity> page = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<LogJobPageResponse> response = logJobService.getPage(page, query);

		return Result.success(response);
	}

	@Operation(summary = "日志详情")
	@PreAuthorize("@auth.decide('sysJobLog:detail')")
	@GetMapping("{id}")
	public Result<LogJobDetailVO> detail(@PathVariable("id") Long id) {
		LogJobDetailVO detailVO = logJobService.getDetail(id);

		return Result.success(detailVO);
	}

	@Operation(summary = "批量删除日志")
	@PreAuthorize("@auth.decide('sysJobLog:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		logJobService.removeByIds(ids);

		return Result.success(null, DELETE_SUCCESS.getMessage());
	}

	@Operation(summary = "发送清空日志验证码")
	@PreAuthorize("@auth.decide('sysJobLog:clearAll')")
	@PostMapping("send-code")
	public Result<String> sendCode() {
		logJobService.sendClearLogVerificationCode();

		return Result.success();
	}

	@Operation(summary = "清空全部日志")
	@PreAuthorize("@auth.decide('sysJobLog:clearAll')")
	@DeleteMapping("clear")
	public Result<String> clearAll(@RequestParam("code") String code) {
		logJobService.clearAll(code);

		return Result.success(null, DELETE_SUCCESS.getMessage());
	}

}
