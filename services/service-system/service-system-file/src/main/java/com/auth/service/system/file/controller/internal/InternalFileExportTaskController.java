package com.auth.service.system.file.controller.internal;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.api.model.dto.ExportPreCheckResult;
import com.auth.module.file.api.model.dto.FileExportTaskCreateResult;
import com.auth.module.file.api.model.request.ExportPreCheckRequest;
import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;
import com.auth.module.file.api.port.FileExportTaskOperations;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部导出任务
 *
 * @author Bunny
 */
@InternalApi
@Tag(name = "内部-导出任务", description = "服务间预检与创建异步导出任务")
@RequiredArgsConstructor
@RequestMapping("/api/system/file/inner/file-export-tasks")
@RestController
public class InternalFileExportTaskController {

	private final FileExportTaskOperations fileExportTaskOperations;

	@Operation(summary = "预检导出任务", description = "估算导出行数，不落任务")
	@PostMapping("/pre-check")
	public Result<ExportPreCheckResult> preCheck(@Valid @RequestBody ExportPreCheckRequest request) {
		ExportPreCheckResult result = fileExportTaskOperations.preCheck(request);
		return Result.success(result);
	}

	@Operation(summary = "新增导出任务")
	@PostMapping("/create")
	public Result<FileExportTaskCreateResult> create(@Valid @RequestBody FileExportTaskCreateRequest request) {
		FileExportTaskCreateResult result = fileExportTaskOperations.createTask(request);
		return Result.success(result);
	}

}
