package com.auth.service.system.file.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.file.model.query.FileExportTaskAdminPageQuery;
import com.auth.service.system.file.model.vo.FileExportAttemptVO;
import com.auth.service.system.file.model.vo.FileExportTaskAdminDetailVO;
import com.auth.service.system.file.model.vo.FileExportTaskAdminPageVO;
import com.auth.service.system.file.service.FileExportTaskAdminQueryService;
import com.auth.service.system.file.service.FileExportTaskWriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件异步导出任务运维
 *
 * @author Bunny
 */
@Tag(name = "文件导出任务运维", description = "管理端跨用户查询与强杀/强制重试；不含代创建")
@RequiredArgsConstructor
@RequestMapping("/api/system/file/export-tasks")
@RestController
public class FileExportTaskController {

	private final FileExportTaskAdminQueryService fileExportTaskAdminQueryService;

	private final FileExportTaskWriteService fileExportTaskWriteService;

	@Operation(summary = "分页查询导出任务", description = "跨用户；可按创建人与错误码筛选；含租约字段，不含权限快照")
	@PreAuthorize("@auth.decide('sys:file:exporttask:query')")
	@GetMapping("/page")
	public Result<PageResponse<FileExportTaskAdminPageVO>> page(@Valid FileExportTaskAdminPageQuery query) {
		return Result.success(fileExportTaskAdminQueryService.getPage(query));
	}

	@Operation(summary = "查询导出任务详情", description = "跨用户；含查询快照与租约，不含权限快照")
	@PreAuthorize("@auth.decide('sys:file:exporttask:query')")
	@GetMapping("/{id}")
	public Result<FileExportTaskAdminDetailVO> detail(@PathVariable("id") Long id) {
		return Result.success(fileExportTaskAdminQueryService.getDetail(id));
	}

	@Operation(summary = "查询导出任务执行尝试")
	@PreAuthorize("@auth.decide('sys:file:exporttask:query')")
	@GetMapping("/{id}/attempts")
	public Result<List<FileExportAttemptVO>> attempts(@PathVariable("id") Long id) {
		return Result.success(fileExportTaskAdminQueryService.listAttempts(id));
	}

	@Operation(summary = "强制取消导出任务", description = "不校验创建人；PENDING 直接取消；RUNNING 转 CANCELLING；CANCELLING/CANCELLED 幂等")
	@PreAuthorize("@auth.decide('sys:file:exporttask:cancel')")
	@PostMapping("/{id}/force-cancel")
	public Result<Void> forceCancel(@PathVariable("id") Long id) {
		fileExportTaskWriteService.forceCancelTask(id);
		return Result.success();
	}

	@Operation(summary = "强制重试导出任务", description = "不校验创建人；仅 FAILED/CANCELLED 且未超最大重试；沿用原查询与权限快照")
	@PreAuthorize("@auth.decide('sys:file:exporttask:retry')")
	@PostMapping("/{id}/force-retry")
	public Result<Void> forceRetry(@PathVariable("id") Long id) {
		fileExportTaskWriteService.forceRetryTask(id);
		return Result.success();
	}

}
