package com.auth.service.system.file.controller.me;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.file.model.query.FileExportTaskPageQuery;
import com.auth.service.system.file.model.vo.FileExportTaskDetailVO;
import com.auth.service.system.file.model.vo.FileExportTaskDownloadLinkVO;
import com.auth.service.system.file.model.vo.FileExportTaskPageVO;
import com.auth.service.system.file.service.FileExportTaskQueryService;
import com.auth.service.system.file.service.FileExportTaskWriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 个人导出任务
 *
 * @author Bunny
 */
@AuthenticatedApi
@Tag(name = "个人导出任务", description = "当前用户异步导出任务管理")
@RequiredArgsConstructor
@RequestMapping("/api/system/me/file-export-tasks")
@RestController
public class PersonalFileExportTaskController {

	private final FileExportTaskQueryService fileExportTaskQueryService;

	private final FileExportTaskWriteService fileExportTaskWriteService;

	@Operation(summary = "分页查询导出任务", description = "仅当前用户任务；不含权限快照与租约字段")
	@GetMapping("/page")
	public Result<PageResponse<FileExportTaskPageVO>> page(@Valid FileExportTaskPageQuery query) {
		Long userId = SecurityUserUtils.getUserId();
		PageResponse<FileExportTaskPageVO> page = fileExportTaskQueryService.getPage(query, userId);
		return Result.success(page);
	}

	@Operation(summary = "查询导出任务详情", description = "仅当前用户任务；含查询快照，不含权限快照与租约字段")
	@GetMapping("/{id}")
	public Result<FileExportTaskDetailVO> detail(@PathVariable("id") Long id) {
		Long userId = SecurityUserUtils.getUserId();
		FileExportTaskDetailVO detail = fileExportTaskQueryService.getDetail(id, userId);
		return Result.success(detail);
	}

	@Operation(summary = "取消导出任务", description = "PENDING 直接取消；RUNNING 转 CANCELLING；CANCELLING/CANCELLED 幂等")
	@PostMapping("/{id}/cancel")
	public Result<Void> cancel(@PathVariable("id") Long id) {
		Long userId = SecurityUserUtils.getUserId();
		fileExportTaskWriteService.cancelTask(id, userId);
		return Result.success();
	}

	@Operation(summary = "重试导出任务", description = "仅 FAILED/CANCELLED 且未超最大重试；沿用原查询与权限快照")
	@PostMapping("/{id}/retry")
	public Result<Void> retry(@PathVariable("id") Long id) {
		Long userId = SecurityUserUtils.getUserId();
		fileExportTaskWriteService.retryTask(id, userId);
		return Result.success();
	}

	@Operation(summary = "获取导出任务下载链接", description = "仅 SUCCESS 且产物未过期；链接现签")
	@GetMapping("/{id}/download-link")
	public Result<FileExportTaskDownloadLinkVO> downloadLink(@PathVariable("id") Long id) {
		Long userId = SecurityUserUtils.getUserId();
		FileExportTaskDownloadLinkVO downloadLink = fileExportTaskWriteService.getDownloadLink(id, userId);
		return Result.success(downloadLink);
	}

}
