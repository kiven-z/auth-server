package com.auth.service.example.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.api.model.dto.FileExportTaskCreateResult;
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
	@Operation(summary = "查询数据权限演示单列表", description = "按登录画像 deptScope 行级过滤")
	@GetMapping
	public Result<List<ExampleOrderVO>> listScoped() {
		List<ExampleOrderVO> data = exampleOrderService.listScoped();
		return Result.success(data);
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
