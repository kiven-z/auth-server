package com.auth.service.example.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.file.api.model.dto.ExportPreCheckResult;
import com.auth.module.file.api.model.dto.FileExportTaskCreateResult;
import com.auth.module.file.api.model.request.ExportPreCheckRequest;
import com.auth.module.file.api.port.FileExportTaskOperations;
import com.auth.module.file.export.definition.ExportColumn;
import com.auth.module.file.export.definition.ExportColumns;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.example.excel.ExampleOrderSpreadsheetRow;
import com.auth.service.example.model.query.ExampleOrderQuery;
import com.auth.service.example.model.vo.ExampleOrderVO;
import com.auth.service.example.service.ExampleOrderService;
import com.auth.service.example.support.export.ExampleBizCodes;
import com.auth.service.example.support.export.ExampleFileExportTaskCreateRequests;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据权限演示单接口
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@RequestMapping("/api/example/orders")
@RestController
public class ExampleOrderController {

	private final ExampleOrderService exampleOrderService;

	private final FileExportTaskOperations fileExportTaskOperations;

	@AuthenticatedApi
	@Operation(summary = "分页查询数据权限演示单", description = "按登录画像 deptScope 行级过滤")
	@GetMapping
	public Result<PageResponse<ExampleOrderVO>> page(@Validated ExampleOrderQuery query) {
		PageResponse<ExampleOrderVO> response = exampleOrderService.getPage(query);
		return Result.success(response);
	}

	@AuthenticatedApi
	@Operation(summary = "异步导出演示单前置检查", description = "按筛选条件估算行数并返回导出方式")
	@PostMapping("/export/pre-check")
	public Result<ExportPreCheckResult> exportPreCheck(@Validated @RequestBody ExampleOrderQuery filter) {
		ExportPreCheckRequest request = ExampleFileExportTaskCreateRequests.preCheck(ExampleBizCodes.EXAMPLE_ORDER,
				filter);
		ExportPreCheckResult result = fileExportTaskOperations.preCheck(request);
		return Result.success(result);
	}

	@AuthenticatedApi
	@Operation(summary = "异步导出演示单 Excel", description = "提交异步导出任务，完成后在文件中心下载")
	@PostMapping("/export/async")
	public Result<FileExportTaskCreateResult> exportAsync(@Validated @RequestBody ExampleOrderQuery filter,
			@Parameter(description = "幂等请求ID") @RequestParam(required = false) String requestId) {
		List<ExportColumn> exportColumns = ExportColumns.fromExcelProperties(ExampleOrderSpreadsheetRow.class);
		var createRequest = ExampleFileExportTaskCreateRequests.xlsx(ExampleBizCodes.EXAMPLE_ORDER, filter, requestId,
				SecurityUserUtils.getUserId(), exportColumns);

		FileExportTaskCreateResult result = fileExportTaskOperations.createTask(createRequest);
		return Result.success(result);
	}

}
