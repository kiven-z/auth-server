package com.auth.module.file.export.task;

import com.auth.common.core.utils.JsonSupport;
import com.auth.module.file.api.model.enums.FileExportFormat;
import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;
import com.auth.module.file.export.definition.ExportColumn;
import com.auth.module.file.export.definition.ExportColumns;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 组装异步导出创建请求
 *
 * @author Bunny
 */
@UtilityClass
public class FileExportTaskCreateRequests {

	/**
	 * 由创建命令组装创建请求
	 * @param command 创建命令
	 * @return 创建请求
	 */
	public static FileExportTaskCreateRequest of(FileExportTaskCreateCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("command");
		}
		if (command.getExportFormat() == null) {
			throw new IllegalArgumentException("exportFormat");
		}

		FileExportTaskCreateRequest request = new FileExportTaskCreateRequest();
		request.setBizType(command.getBizType());
		request.setQuerySnapshotJson(command.getQuery() == null ? "{}" : JsonSupport.toJson(command.getQuery()));
		request.setColumnSnapshotJson(ExportColumns.toSnapshotJson(command.getColumns()));
		request.setExportFormat(command.getExportFormat().name());
		request.setFetchService(command.getFetchService());
		request.setFetchPath(command.getFetchPath());
		request.setRequestId(command.getRequestId());
		request.setCreatedBy(command.getCreatedBy());
		return request;
	}

	/**
	 * 组装 XLSX 异步导出创建请求
	 * @param bizType 业务类型
	 * @param query 列表筛选
	 * @param requestId 幂等请求 ID（可空）
	 * @param createdBy 创建用户 ID
	 * @param fetchService 取数服务名
	 * @param fetchPath 取数路径
	 * @param columns 导出列定义
	 * @return 创建请求
	 */
	public static FileExportTaskCreateRequest xlsx(String bizType, Object query, String requestId, Long createdBy,
			String fetchService, String fetchPath, List<ExportColumn> columns) {
		return of(FileExportTaskCreateCommand.builder()
			.bizType(bizType)
			.query(query)
			.requestId(requestId)
			.createdBy(createdBy)
			.exportFormat(FileExportFormat.XLSX)
			.fetchService(fetchService)
			.fetchPath(fetchPath)
			.columns(columns)
			.build());
	}

}
