package com.auth.module.file.export.executor;

import com.auth.module.file.exception.ExportExecutionException;
import com.auth.module.file.exception.ExportValidationException;
import com.auth.module.file.export.datasource.PageDataSource;
import com.auth.module.file.export.definition.ExportColumn;
import com.auth.module.file.export.definition.ExportSheetMeta;
import com.auth.module.file.export.task.FileExportTaskArtifact;
import com.auth.module.file.export.task.FileExportTaskContext;
import com.auth.module.file.export.task.FileExportTaskProgressListener;
import com.auth.module.file.export.writer.ExportWriterFactory;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link AsyncPagedExportExecutor} 单元测试
 *
 * @author Bunny
 */
@DisplayName("AsyncPagedExportExecutor 异步分页导出执行")
class AsyncPagedExportExecutorTest {

	private final AsyncPagedExportExecutor asyncPagedExportExecutor = new AsyncPagedExportExecutor(
			new ExportWriterFactory());

	private static Map<String, Object> row(int id) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("id", id);
		row.put("name", "Name" + id);
		row.put("value", "Value" + id);
		return row;
	}

	private static IPage<Map<String, Object>> createPage(int current, int size, List<Map<String, Object>> records,
			long total) {
		Page<Map<String, Object>> page = new Page<>(current, size, total);
		page.setRecords(records);
		return page;
	}

	private static void assertExcelContainsDataRows(Path filePath) throws Exception {
		try (InputStream inputStream = Files.newInputStream(filePath);
				XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();
			Row header = sheet.getRow(0);
			assertThat(formatter.formatCellValue(header.getCell(0))).isEqualTo("id");
			assertThat(formatter.formatCellValue(header.getCell(1))).isEqualTo("name");
			assertThat(formatter.formatCellValue(header.getCell(2))).isEqualTo("value");
			assertThat(sheet.getLastRowNum()).isEqualTo(3);
			Row firstData = sheet.getRow(1);
			assertThat(formatter.formatCellValue(firstData.getCell(0))).isEqualTo("1");
			assertThat(formatter.formatCellValue(firstData.getCell(1))).isEqualTo("Name1");
			assertThat(formatter.formatCellValue(firstData.getCell(2))).isEqualTo("Value1");
		}
	}

	@Test
	@DisplayName("export：分页写入成功返回导出产物并上报进度")
	void export_whenPagedDataExists_returnsArtifact() throws Exception {
		FileExportTaskContext context = FileExportTaskContext.builder()
			.bizType("SYS_TEST")
			.exportFormat("XLSX")
			.fetchPageSize(2)
			.maxRows(100L)
			.createdAt(Instant.parse("2026-08-09T00:00:00Z"))
			.build();
		TestDataSource dataSource = new TestDataSource(
				List.of(createPage(1, 2, List.of(row(1), row(2)), 3L), createPage(2, 2, List.of(row(3)), 3L)));
		RecordingProgressListener progressListener = new RecordingProgressListener(false);

		FileExportTaskArtifact artifact = asyncPagedExportExecutor.export(context, dataSource, progressListener);

		assertThat(artifact.getRowCount()).isEqualTo(3L);
		assertThat(artifact.getContentType())
			.isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		assertThat(artifact.getFileName()).startsWith("sys_test_export_").endsWith(".xlsx");
		assertThat(Files.exists(artifact.getFilePath())).isTrue();
		assertThat(progressListener.progressEvents()).containsExactly("2/3", "3/3");
		assertExcelContainsDataRows(artifact.getFilePath());
		Files.deleteIfExists(artifact.getFilePath());
	}

	@Test
	@DisplayName("export：首轮 totalRows 超限时抛异常")
	void export_whenTotalRowsExceeded_throwsException() {
		FileExportTaskContext context = FileExportTaskContext.builder()
			.bizType("SYS_TEST")
			.exportFormat("XLSX")
			.fetchPageSize(100)
			.maxRows(500L)
			.createdAt(Instant.parse("2026-08-09T00:00:00Z"))
			.build();
		TestDataSource dataSource = new TestDataSource(List.of(createPage(1, 100, List.of(row(1)), 501L)));
		FileExportTaskProgressListener progressListener = new RecordingProgressListener(false);

		assertThatThrownBy(() -> asyncPagedExportExecutor.export(context, dataSource, progressListener))
			.isInstanceOf(ExportValidationException.class)
			.hasMessageContaining("导出行数超出限制");
	}

	@Test
	@DisplayName("export：收到取消请求时抛异常")
	void export_whenCancelRequested_throwsException() {
		FileExportTaskContext context = FileExportTaskContext.builder()
			.bizType("SYS_TEST")
			.exportFormat("XLSX")
			.fetchPageSize(100)
			.maxRows(500L)
			.createdAt(Instant.parse("2026-08-09T00:00:00Z"))
			.build();
		TestDataSource dataSource = new TestDataSource(List.of(createPage(1, 100, List.of(row(1)), 1L)));
		FileExportTaskProgressListener progressListener = new RecordingProgressListener(true);

		assertThatThrownBy(() -> asyncPagedExportExecutor.export(context, dataSource, progressListener))
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
			return createPage(pageNum, pageSize, List.of(), 0L);
		}

		@Override
		public ExportSheetMeta sheetMeta() {
			return ExportSheetMeta.builder()
				.sheetName("测试")
				.downloadFilePrefix("测试")
				.taskFilePrefix("sys_test_export")
				.columns(List.of(ExportColumn.builder().fieldKey("id").header("id").build(),
						ExportColumn.builder().fieldKey("name").header("name").build(),
						ExportColumn.builder().fieldKey("value").header("value").build()))
				.build();
		}

	}

}
