package com.auth.service.system.schedule.controller;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.*;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.schedule.model.entity.SysJobLogEntity;
import com.auth.service.system.schedule.model.query.SysJobLogQuery;
import com.auth.service.system.schedule.model.vo.SysJobLogDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobLogPageResponse;
import com.auth.service.system.schedule.service.SysJobLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 定时任务执行日志接口
 *
 * @author Bunny
 */
@Tag(name = "定时任务-日志", description = "log_job")
@RequestMapping("/api/system/job-log")
@RestController
public class SysJobLogController {

	private final SysJobLogService sysJobLogService;

	public SysJobLogController(SysJobLogService sysJobLogService) {
		this.sysJobLogService = sysJobLogService;
	}

	@Operation(summary = "分页查询任务日志")
	@PreAuthorize("@auth.decide('sysJobLog:query')")
	@GetMapping
	public Result<PageResponse<SysJobLogPageResponse>> page(SysJobLogQuery query) {
		Page<SysJobLogEntity> page = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysJobLogPageResponse> response = sysJobLogService.getPage(page, query);

		return Result.success(response);
	}

	@Operation(summary = "日志详情")
	@PreAuthorize("@auth.decide('sysJobLog:detail')")
	@GetMapping("{id}")
	public Result<SysJobLogDetailVO> detail(@PathVariable("id") Long id) {
		SysJobLogDetailVO detailVO = sysJobLogService.getDetail(id);

		return Result.success(detailVO);
	}

	@Operation(summary = "批量删除日志")
	@PreAuthorize("@auth.decide('sysJobLog:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		sysJobLogService.removeBatch(ids);

		return Result.successMessage(DELETE_SUCCESS.getMessage());
	}

	@Operation(summary = "发送清空日志验证码")
	@PreAuthorize("@auth.decide('sysJobLog:clearAll')")
	@PostMapping("send-code")
	public Result<String> sendCode() {
		sysJobLogService.sendClearLogVerificationCode();

		return Result.success();
	}

	@Operation(summary = "清空全部日志")
	@PreAuthorize("@auth.decide('sysJobLog:clearAll')")
	@DeleteMapping("clear")
	public Result<String> clearAll(@RequestParam("code") String code) {
		sysJobLogService.clearAll(code);

		return Result.successMessage(DELETE_SUCCESS.getMessage());
	}

}
