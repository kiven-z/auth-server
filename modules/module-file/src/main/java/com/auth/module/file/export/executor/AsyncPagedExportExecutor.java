package com.auth.module.file.export.executor;

import com.auth.module.file.exception.ExportExecutionException;
import com.auth.module.file.exception.ExportValidationException;
import com.auth.module.file.export.datasource.PageDataSource;
import com.auth.module.file.export.task.FileExportTaskArtifact;
import com.auth.module.file.export.task.FileExportTaskContext;
import com.auth.module.file.export.task.FileExportTaskProgressListener;
import com.auth.module.file.export.writer.ChunkExportWriter;
import com.auth.module.file.export.writer.ExportWriterFactory;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 异步分页导出执行器（统一分页循环、进度与产物写入）
 *
 * @author Bunny
 */
public class AsyncPagedExportExecutor {

	private static final DateTimeFormatter FILE_NAME_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
		.withZone(ZoneOffset.UTC);

	private final ExportWriterFactory exportWriterFactory;

	public AsyncPagedExportExecutor(ExportWriterFactory exportWriterFactory) {
		this.exportWriterFactory = exportWriterFactory;
	}

	/**
	 * 执行异步导出
	 * @param context 导出任务上下文
	 * @param dataSource 分页数据源
	 * @param progressListener 进度监听器
	 * @return 导出产物
	 */
	public FileExportTaskArtifact export(FileExportTaskContext context, PageDataSource dataSource,
			FileExportTaskProgressListener progressListener) {
		ExportWriterFactory.ExportWriterHandle<Map<String, Object>> writerHandle = exportWriterFactory
			.create(context.getExportFormat(), dataSource.sheetMeta());
		PageWriteState state = new PageWriteState();

		Path filePath;
		try {
			filePath = writerHandle.writer()
				.writeToTempFile(chunkWriter -> writePages(context, dataSource, progressListener, chunkWriter, state));
		}
		catch (IOException exception) {
			throw ExportExecutionException.writeFailed(exception.getMessage());
		}

		Instant createdAt = context.getCreatedAt() == null ? Instant.now() : context.getCreatedAt();
		String filePrefix = resolveFilePrefix(dataSource.sheetMeta().taskFilePrefix(), context.getBizType());
		return FileExportTaskArtifact.builder()
			.fileName(filePrefix + "_" + FILE_NAME_TIME.format(createdAt) + "."
					+ writerHandle.fileExtension().toLowerCase(Locale.ROOT))
			.contentType(writerHandle.contentType())
			.filePath(filePath)
			.rowCount(state.processedRows)
			.build();
	}

	/**
	 * 写入分页数据
	 * @param context 导出任务上下文
	 * @param dataSource 分页数据源
	 * @param progressListener 进度监听器
	 * @param chunkWriter 分页写入器
	 * @param state 分页写入状态
	 * @throws IOException 写入失败
	 */
	private void writePages(FileExportTaskContext context, PageDataSource dataSource,
			FileExportTaskProgressListener progressListener,
			ChunkExportWriter.ChunkWriter<Map<String, Object>> chunkWriter, PageWriteState state) throws IOException {
		int pageNum = 1;
		int pageSize = context.getFetchPageSize();
		boolean continuePaging = true;
		while (continuePaging) {
			assertNotCancelled(progressListener);
			IPage<Map<String, Object>> page = dataSource.fetchPage(pageNum, pageSize);
			if (pageNum == 1) {
				state.totalRows = page.getTotal();
				assertWithinRowLimit(context.getMaxRows(), state.totalRows);
			}
			continuePaging = writePageIfPresent(progressListener, chunkWriter, state, page);
			pageNum++;
		}
	}

	/**
	 * 写入分页数据
	 * @param progressListener 进度监听器
	 * @param chunkWriter 分页写入器
	 * @param state 分页写入状态
	 * @param page 分页数据
	 * @return 是否继续分页
	 * @throws IOException 写入失败
	 */
	private boolean writePageIfPresent(FileExportTaskProgressListener progressListener,
			ChunkExportWriter.ChunkWriter<Map<String, Object>> chunkWriter, PageWriteState state,
			IPage<Map<String, Object>> page) throws IOException {
		List<Map<String, Object>> pageRows = page.getRecords();
		if (pageRows.isEmpty()) {
			return false;
		}

		chunkWriter.writeChunk(pageRows);
		state.processedRows += pageRows.size();
		progressListener.onProgress(state.processedRows, state.totalRows);
		return state.totalRows <= 0 || state.processedRows < state.totalRows;
	}

	/**
	 * 断言任务是否被取消
	 * @param progressListener 进度监听器
	 * @throws ExportExecutionException 任务被取消
	 */
	private void assertNotCancelled(FileExportTaskProgressListener progressListener) {
		if (progressListener.isCancelRequested()) {
			throw ExportExecutionException.cancelled();
		}
	}

	/**
	 * 断言行数是否超过限制
	 * @param maxRows 最大行数
	 * @param totalRows 总行数
	 * @throws ExportValidationException 行数超过限制
	 */
	private void assertWithinRowLimit(long maxRows, long totalRows) {
		if (totalRows > maxRows) {
			throw ExportValidationException.rowLimitExceeded(maxRows, totalRows);
		}
	}

	/**
	 * 解析文件前缀
	 * @param configuredPrefix 配置的前缀
	 * @param fallbackBizType 默认业务类型
	 * @return 文件前缀
	 */
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
