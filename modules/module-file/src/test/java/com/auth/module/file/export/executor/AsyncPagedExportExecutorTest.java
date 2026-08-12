package com.auth.module.file.export.executor;

import com.auth.module.file.exception.ExportExecutionException;
import com.auth.module.file.exception.ExportValidationException;
import com.auth.module.file.export.datasource.PageDataSource;
import com.auth.module.file.export.definition.ExportColumn;
import com.auth.module.file.export.definition.ExportSheetMeta;
import com.auth.module.file.export.shard.ExportArtifactPackager;
import com.auth.module.file.export.task.FileExportTaskArtifact;
import com.auth.module.file.export.task.FileExportTaskContext;
import com.auth.module.file.export.task.FileExportTaskProgressListener;
import com.auth.module.file.export.writer.ChunkFileSessionFactoryResolver;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link AsyncPagedExportExecutor} 单元测试
 *
 * @author Bunny
 */
@DisplayName("AsyncPagedExportExecutor 异步分页导出执行")
class AsyncPagedExportExecutorTest {

	private final AsyncPagedExportExecutor executor = new AsyncPagedExportExecutor(
			new ChunkFileSessionFactoryResolver(), new ExportArtifactPackager());

	private static Map<String, Object> row(int id) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("id", id);
		row.put("name", "Name" + id);
		row.put("value", "Value" + id);
		return row;
	}

	private static IPage<Map<String, Object>> page(int current, int size, long total,
			List<Map<String, Object>> records) {
		Page<Map<String, Object>> p = new Page<>(current, size, total);
		p.setRecords(records);
		return p;
	}

	private static ExportSheetMeta sheetMeta() {
		return ExportSheetMeta.builder()
			.sheetName("测试")
			.taskFilePrefix("sys_test_export")
			.columns(List.of(ExportColumn.builder().fieldKey("id").header("id").build(),
					ExportColumn.builder().fieldKey("name").header("name").build(),
					ExportColumn.builder().fieldKey("value").header("value").build()))
			.build();
	}

	private static FileExportTaskContext contextWithShardRows(int shardRows) {
		return FileExportTaskContext.builder()
			.bizType("SYS_TEST")
			.exportFormat("XLSX")
			.fetchPageSize(2)
			.maxRows(1000L)
			.shardRows(shardRows)
			.createdAt(Instant.parse("2026-08-09T00:00:00Z"))
			.build();
	}

	private static void assertExcelContainsHeaderAndFirstRow(Path filePath) throws Exception {
		try (InputStream in = Files.newInputStream(filePath); XSSFWorkbook workbook = new XSSFWorkbook(in)) {
			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();
			Row header = sheet.getRow(0);
			assertThat(formatter.formatCellValue(header.getCell(0))).isEqualTo("id");
			assertThat(formatter.formatCellValue(header.getCell(1))).isEqualTo("name");
			assertThat(formatter.formatCellValue(header.getCell(2))).isEqualTo("value");
			assertThat(sheet.getLastRowNum()).isEqualTo(3);
		}
	}

	private static void assertZipContainsXlsxEntries(Path filePath) throws Exception {
		int count = 0;
		try (InputStream in = Files.newInputStream(filePath); ZipInputStream zis = new ZipInputStream(in)) {
			while (zis.getNextEntry() != null) {
				count++;
			}
		}
		assertThat(count).isEqualTo(2);
	}

	@Test
	@DisplayName("export：分页写入成功，产物为单 xlsx，进度按页上报")
	void export_whenPagedDataExists_returnsXlsxArtifact() throws Exception {
		TestDataSource dataSource = new TestDataSource(
				List.of(page(1, 2, 3L, List.of(row(1), row(2))), page(2, 2, 3L, List.of(row(3)))));
		RecordingProgressListener progressListener = new RecordingProgressListener(false);

		FileExportTaskArtifact artifact = executor.export(contextWithShardRows(0), dataSource, progressListener);

		assertThat(artifact.getRowCount()).isEqualTo(3L);
		assertThat(artifact.getContentType())
			.isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		assertThat(artifact.getFileName()).startsWith("sys_test_export_").endsWith(".xlsx");
		assertThat(artifact.getFilePath()).exists();
		assertThat(progressListener.progressEvents()).containsExactly("2/3", "3/3");
		assertExcelContainsHeaderAndFirstRow(artifact.getFilePath());
		Files.deleteIfExists(artifact.getFilePath());
	}

	@Test
	@DisplayName("export：总行数超过分片阈值时，产物为 zip，文件名以 .zip 结尾")
	void export_whenExceedsShardThreshold_returnsZipArtifact() throws Exception {
		// shardRows=2，3 行触发分 2 片（xlsx + 剩余）→ zip
		TestDataSource dataSource = new TestDataSource(
				List.of(page(1, 2, 3L, List.of(row(1), row(2))), page(2, 2, 3L, List.of(row(3)))));
		RecordingProgressListener progressListener = new RecordingProgressListener(false);

		FileExportTaskArtifact artifact = executor.export(contextWithShardRows(2), dataSource, progressListener);

		assertThat(artifact.getRowCount()).isEqualTo(3L);
		assertThat(artifact.getContentType()).isEqualTo("application/zip");
		assertThat(artifact.getFileName()).startsWith("sys_test_export_").endsWith(".zip");
		assertThat(artifact.getFilePath()).exists();
		assertZipContainsXlsxEntries(artifact.getFilePath());
		Files.deleteIfExists(artifact.getFilePath());
	}

	@Test
	@DisplayName("export：总行数恰好等于分片阈值时，产物仍为单 xlsx")
	void export_whenEqualsShardThreshold_returnsXlsxArtifact() throws Exception {
		// shardRows=3，3 行不超过阈值，单文件
		TestDataSource dataSource = new TestDataSource(
				List.of(page(1, 2, 3L, List.of(row(1), row(2))), page(2, 2, 3L, List.of(row(3)))));

		FileExportTaskArtifact artifact = executor.export(contextWithShardRows(3), dataSource,
				new RecordingProgressListener(false));

		assertThat(artifact.getFileName()).endsWith(".xlsx");
		Files.deleteIfExists(artifact.getFilePath());
	}

	@Test
	@DisplayName("export：首轮 totalRows 超过 maxRows 时抛校验异常")
	void export_whenTotalRowsExceededMaxRows_throwsValidationException() {
		FileExportTaskContext context = FileExportTaskContext.builder()
			.bizType("SYS_TEST")
			.exportFormat("XLSX")
			.fetchPageSize(100)
			.maxRows(500L)
			.shardRows(0)
			.createdAt(Instant.parse("2026-08-09T00:00:00Z"))
			.build();
		TestDataSource dataSource = new TestDataSource(List.of(page(1, 100, 501L, List.of(row(1)))));
		RecordingProgressListener progressListener = new RecordingProgressListener(false);

		assertThatThrownBy(() -> executor.export(context, dataSource, progressListener))
			.isInstanceOf(ExportValidationException.class)
			.hasMessageContaining("最多导出 500 行，当前约 501 行");
	}

	@Test
	@DisplayName("export：收到取消请求时抛执行异常")
	void export_whenCancelRequested_throwsExecutionException() {
		FileExportTaskContext context = contextWithShardRows(0);
		TestDataSource dataSource = new TestDataSource(List.of(page(1, 100, 1L, List.of(row(1)))));
		RecordingProgressListener progressListener = new RecordingProgressListener(true);

		assertThatThrownBy(() -> executor.export(context, dataSource, progressListener))
			.isInstanceOf(ExportExecutionException.class)
			.hasMessageContaining("导出已按请求取消");
	}

	private static final class RecordingProgressListener implements FileExportTaskProgressListener {

		private final boolean cancelRequested;

		private final List<String> progressEvents = new ArrayList<>();

		private RecordingProgressListener(boolean cancelRequested) {
			this.cancelRequested = cancelRequested;
		}

		@Override
		public void onProgress(long processedRows, long totalRows) {
			progressEvents.add(processedRows + "/" + totalRows);
		}

		@Override
		public boolean isCancelRequested() {
			return cancelRequested;
		}

		private List<String> progressEvents() {
			return progressEvents;
		}

	}

	private static final class TestDataSource implements PageDataSource {

		private final List<IPage<Map<String, Object>>> pages;

		private final AtomicInteger index = new AtomicInteger(0);

		private TestDataSource(List<IPage<Map<String, Object>>> pages) {
			this.pages = pages;
		}

		@Override
		public IPage<Map<String, Object>> fetchPage(int pageNum, int pageSize) {
			int current = index.getAndIncrement();
			if (current < pages.size()) {
				return pages.get(current);
			}
			return page(pageNum, pageSize, 0L, List.of());
		}

		@Override
		public ExportSheetMeta sheetMeta() {
			return AsyncPagedExportExecutorTest.sheetMeta();
		}

	}

}
