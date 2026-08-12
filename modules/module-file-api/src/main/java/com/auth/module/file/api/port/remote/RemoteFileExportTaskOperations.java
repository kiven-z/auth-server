package com.auth.module.file.api.port.remote;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.api.feign.SystemFileExportTaskFeignClient;
import com.auth.module.file.api.model.dto.ExportPreCheckResult;
import com.auth.module.file.api.model.dto.FileExportTaskCreateResult;
import com.auth.module.file.api.model.request.ExportPreCheckRequest;
import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;
import com.auth.module.file.api.port.FileExportTaskOperations;

/**
 * 跨服务：通过 Feign 委托 file 写服务执行导出任务操作
 *
 * @author Bunny
 */
public class RemoteFileExportTaskOperations implements FileExportTaskOperations {

	private final SystemFileExportTaskFeignClient feignClient;

	public RemoteFileExportTaskOperations(SystemFileExportTaskFeignClient feignClient) {
		this.feignClient = feignClient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExportPreCheckResult preCheck(ExportPreCheckRequest request) {
		Result<ExportPreCheckResult> result = feignClient.preCheck(request);
		if (result == null) {
			throw new IllegalStateException("Export pre-check returned null");
		}
		if (!Integer.valueOf(Result.SUCCESS_CODE).equals(result.getCode())) {
			throw new IllegalStateException(
					"Export pre-check failed: code=" + result.getCode() + ", message=" + result.getMessage());
		}
		ExportPreCheckResult data = result.getData();
		if (data == null) {
			throw new IllegalStateException("Export pre-check returned empty data");
		}
		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileExportTaskCreateResult createTask(FileExportTaskCreateRequest request) {
		Result<FileExportTaskCreateResult> result = feignClient.create(request);

		if (result == null) {
			throw new IllegalStateException("File export task create returned null");
		}
		if (!Integer.valueOf(Result.SUCCESS_CODE).equals(result.getCode())) {
			throw new IllegalStateException(
					"File export task create failed: code=" + result.getCode() + ", message=" + result.getMessage());
		}

		FileExportTaskCreateResult data = result.getData();
		if (data == null || data.getTaskId() == null) {
			throw new IllegalStateException("File export task create returned empty data");
		}

		return data;
	}

}
