package com.auth.service.system.file.controller.internal;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.api.model.dto.ExportPreCheckResult;
import com.auth.module.file.api.model.dto.FileExportTaskCreateResult;
import com.auth.module.file.api.model.request.ExportPreCheckRequest;
import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.service.system.file.service.FileExportTaskWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@InternalApi
@RequiredArgsConstructor
@RequestMapping("/api/system/file/inner/file-export-tasks")
@RestController
public class InternalFileExportTaskController {

	private final FileExportTaskWriteService writeService;

	@PostMapping("/pre-check")
	public Result<ExportPreCheckResult> preCheck(@Valid @RequestBody ExportPreCheckRequest request) {
		ExportPreCheckResult result = writeService.preCheck(request);
		return Result.success(result);
	}

	@PostMapping("/create")
	public Result<FileExportTaskCreateResult> create(@Valid @RequestBody FileExportTaskCreateRequest request) {
		FileExportTaskCreateResult result = writeService.createTask(request);
		return Result.success(result);
	}

}
