package com.auth.service.system.file.export.task;

import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.enums.FileExportTaskStatus;
import lombok.experimental.UtilityClass;

import java.util.Locale;

/**
 * 异步导出任务实体组装
 *
 * @author Bunny
 */
@UtilityClass
public class FileExportTaskAssembler {

	private static final int DEFAULT_MAX_RETRY = 3;

	/**
	 * 由创建请求与归一化上下文组装待持久化任务实体
	 * @param request 创建请求
	 * @param context 归一化快照与路由
	 * @return 任务实体
	 */
	public static FileExportTaskEntity fromCreateRequest(FileExportTaskCreateRequest request,
			FileExportTaskNormalizedCreateContext context) {
		if (request == null) {
			throw new IllegalArgumentException("request");
		}
		if (context == null) {
			throw new IllegalArgumentException("context");
		}
		FileExportTaskEntity entity = new FileExportTaskEntity();
		entity.setBizType(context.getBizType());
		entity.setExportFormat(request.getExportFormat().trim().toUpperCase(Locale.ROOT));
		entity.setQuerySnapshotJson(context.getQuerySnapshotJson());
		entity.setColumnSnapshotJson(context.getColumnSnapshotJson());
		entity.setScopeSnapshotJson(context.getScopeSnapshotJson());
		entity.setFetchService(context.getFetchService());
		entity.setFetchPath(context.getFetchPath());
		entity.setStatus(FileExportTaskStatus.PENDING.name());
		entity.setProgressPercent(0);
		entity.setProcessedRows(0L);
		entity.setRetryCount(0);
		entity.setMaxRetry(DEFAULT_MAX_RETRY);
		entity.setRequestId(context.getRequestId());
		entity.setCreatedBy(request.getCreatedBy());
		return entity;
	}

}
