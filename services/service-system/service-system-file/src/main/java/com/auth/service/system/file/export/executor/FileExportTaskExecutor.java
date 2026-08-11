package com.auth.service.system.file.export.executor;

import com.auth.module.file.export.datasource.PageDataSource;
import com.auth.module.file.export.executor.AsyncPagedExportExecutor;
import com.auth.module.file.export.task.FileExportTaskArtifact;
import com.auth.module.file.export.task.FileExportTaskContext;
import com.auth.module.file.export.task.FileExportTaskProgressListener;
import com.auth.service.system.file.export.datasource.FileRemotePageDataSource;
import com.auth.service.system.file.export.remote.RemotePageFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 文件异步导出任务执行编排器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class FileExportTaskExecutor {

	private final RemotePageFetcher remotePageFetcher;

	private final AsyncPagedExportExecutor asyncPagedExportExecutor;

	/**
	 * 执行导出任务
	 * @param context 任务上下文
	 * @param progressListener 进度监听器
	 * @return 导出产物
	 */
	public FileExportTaskArtifact executeTask(FileExportTaskContext context,
			FileExportTaskProgressListener progressListener) {
		String fetchService = context.getFetchService();
		String fetchPath = context.getFetchPath();
		String bizType = context.getBizType();
		String querySnapshotJson = context.getQuerySnapshotJson();
		String columnSnapshotJson = context.getColumnSnapshotJson();
		String scopeSnapshotJson = context.getScopeSnapshotJson();

		// 创建数据源
		PageDataSource dataSource = new FileRemotePageDataSource(remotePageFetcher, fetchService, fetchPath, bizType,
				querySnapshotJson, columnSnapshotJson, scopeSnapshotJson);
		return asyncPagedExportExecutor.export(context, dataSource, progressListener);
	}

}
