package com.auth.service.system.admin.controller.internal;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.api.model.dto.ExportPageDataDTO;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.service.system.admin.export.ExportPageDispatchService;
import com.auth.service.system.admin.model.request.InternalExportPageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部导出分页取数接口（供异步导出 Worker 回调）
 *
 * @author Bunny
 */
@InternalApi
@RequiredArgsConstructor
@RequestMapping("/api/system/inner/export")
@RestController
public class InternalExportPageController {

	private final ExportPageDispatchService exportPageDispatchService;

	/**
	 * 统一内部分页取数（恢复权限快照后按业务类型路由）
	 * @param request 取数请求
	 * @return 分页数据
	 */
	@PostMapping("/page")
	public Result<ExportPageDataDTO> page(@Valid @RequestBody InternalExportPageRequest request) {
		ExportPageDataDTO data = exportPageDispatchService.fetchPage(request);
		return Result.success(data);
	}

}
