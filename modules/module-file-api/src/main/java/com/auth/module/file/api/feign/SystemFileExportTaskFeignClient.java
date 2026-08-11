package com.auth.module.file.api.feign;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.api.model.dto.FileExportTaskCreateResult;
import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 service-system 异步导出任务内部接口
 *
 * @author Bunny
 */
@FeignClient(name = "service-system", contextId = "systemFileExportTask", path = "/api/system/file/inner")
public interface SystemFileExportTaskFeignClient {

	/**
	 * 创建异步导出任务
	 * @param request 创建请求
	 * @return 统一响应，data 为任务 ID
	 */
	@PostMapping("/file-export-tasks/create")
	Result<FileExportTaskCreateResult> create(@RequestBody FileExportTaskCreateRequest request);

}
