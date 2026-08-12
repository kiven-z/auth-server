package com.auth.module.file.export.executor;

import com.auth.module.file.exception.ExportExecutionException;
import com.auth.module.file.export.datasource.PageDataSource;
import com.auth.module.file.export.definition.ExportSheetMeta;
import com.auth.module.file.export.delivery.FileDownloadNames;
import com.auth.module.file.export.shard.*;
import com.auth.module.file.export.task.FileExportTaskArtifact;
import com.auth.module.file.export.task.FileExportTaskContext;
import com.auth.module.file.export.task.FileExportTaskProgressListener;
import com.auth.module.file.export.writer.ChunkFileSessionFactoryResolver;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 异步分页导出执行器：统一分页循环、分片落盘、进度回调与产物打包
 *
 * @author Bunny
 */
public class AsyncPagedExportExecutor {

	private final ChunkFileSessionFactoryResolver sessionFactoryResolver;

	private final ExportArtifactPackager artifactPackager;

	public AsyncPagedExportExecutor(ChunkFileSessionFactoryResolver sessionFactoryResolver,
			ExportArtifactPackager artifactPackager) {
		this.sessionFactoryResolver = sessionFactoryResolver;
		this.artifactPackager = artifactPackager;
	}

	/**
	 * 执行异步导出
	 * @param context 任务上下文（含分片策略）
	 * @param dataSource 分页数据源
	 * @param progressListener 进度监听器
	 * @return 导出产物（单 xlsx 或多片 zip）
	 */
	public FileExportTaskArtifact export(FileExportTaskContext context, PageDataSource dataSource,
			FileExportTaskProgressListener progressListener) {
		ExportSheetMeta sheetMeta = dataSource.sheetMeta();
		ChunkFileSessionFactory<Map<String, Object>> sessionFactory = sessionFactoryResolver
			.resolve(context.getExportFormat(), sheetMeta);
		ExportShardPolicy shardPolicy = ExportShardPolicy.of(context.getMaxRows(), context.getShardRows());
		ShardedExportSession<Map<String, Object>> session = new ShardedExportSession<>(sessionFactory,
				shardPolicy.shardRows());

		PageWriteState state = new PageWriteState();
		List<Path> shardPaths = writeAllPages(context, dataSource, progressListener, session, state, shardPolicy);

		String filePrefix = resolveFilePrefix(sheetMeta.taskFilePrefix(), context.getBizType());
		PackagedExportArtifact packedArtifact = packArtifact(shardPaths, sessionFactory, filePrefix);
		Instant createdAt = context.getCreatedAt() != null ? context.getCreatedAt() : Instant.now();
		String fileName = FileDownloadNames.of(filePrefix, createdAt, packedArtifact.getFileExtension());
		return FileExportTaskArtifact.builder()
			.fileName(fileName)
			.contentType(packedArtifact.getContentType())
			.filePath(packedArtifact.getFilePath())
			.rowCount(state.processedRows)
			.build();
	}

	private List<Path> writeAllPages(FileExportTaskContext context, PageDataSource dataSource,
			FileExportTaskProgressListener progressListener, ShardedExportSession<Map<String, Object>> session,
			PageWriteState state, ExportShardPolicy shardPolicy) {
		try {
			writePages(context, dataSource, progressListener, session, state, shardPolicy);
			return session.finish();
		}
		catch (IOException e) {
			session.abortQuietly();
			throw ExportExecutionException.writeFailed(e.getMessage());
		}
		catch (RuntimeException e) {
			session.abortQuietly();
			throw e;
		}
	}

	private void writePages(FileExportTaskContext context, PageDataSource dataSource,
			FileExportTaskProgressListener progressListener, ShardedExportSession<Map<String, Object>> session,
			PageWriteState state, ExportShardPolicy shardPolicy) throws IOException {
		int pageNum = 1;
		int pageSize = context.getFetchPageSize();
		boolean hasMorePages = true;
		while (hasMorePages) {
			assertNotCancelled(progressListener);
			IPage<Map<String, Object>> page = dataSource.fetchPage(pageNum, pageSize);
			if (pageNum == 1) {
				state.totalRows = page.getTotal();
				shardPolicy.assertWithinMaxRows(state.totalRows);
			}
			List<Map<String, Object>> records = page.getRecords();
			if (records.isEmpty()) {
				hasMorePages = false;
			}
			else {
				session.writeRows(records);
				state.processedRows += records.size();
				progressListener.onProgress(state.processedRows, state.totalRows);
				hasMorePages = state.totalRows <= 0 || state.processedRows < state.totalRows;
				pageNum++;
			}
		}
	}

	private PackagedExportArtifact packArtifact(List<Path> shardPaths,
			ChunkFileSessionFactory<Map<String, Object>> sessionFactory, String filePrefix) {
		try {
			return artifactPackager.pack(shardPaths, sessionFactory.fileExtension(), sessionFactory.contentType(),
					filePrefix);
		}
		catch (IOException e) {
			throw ExportExecutionException.writeFailed(e.getMessage());
		}
	}

	private void assertNotCancelled(FileExportTaskProgressListener progressListener) {
		if (progressListener.isCancelRequested()) {
			throw ExportExecutionException.cancelled();
		}
	}

	private String resolveFilePrefix(String configuredPrefix, String fallbackBizType) {
		if (configuredPrefix != null && !configuredPrefix.trim().isEmpty()) {
			return configuredPrefix.trim();
		}
		return fallbackBizType.trim().toLowerCase(Locale.ROOT) + "_export";
	}

	private static final class PageWriteState {

		private long processedRows;

		private long totalRows;

	}

}
