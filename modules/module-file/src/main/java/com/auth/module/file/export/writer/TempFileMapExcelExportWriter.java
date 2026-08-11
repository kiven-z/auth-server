package com.auth.module.file.export.writer;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.auth.module.file.exception.ExportValidationException;
import com.auth.module.file.export.definition.ExportColumn;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Map 行动态表头临时文件 Excel 写入器（异步分页分块）
 *
 * @author Bunny
 */
public class TempFileMapExcelExportWriter {

	private final String sheetName;

	private final List<ExportColumn> configuredColumns;

	private final List<CellWriteHandler> writeHandlers;

	/**
	 * @param sheetName 工作表名称
	 * @param configuredColumns 显式列定义（必填）
	 */
	public TempFileMapExcelExportWriter(String sheetName, List<ExportColumn> configuredColumns) {
		this.sheetName = sheetName;

		if (configuredColumns == null || configuredColumns.isEmpty()) {
			throw ExportValidationException.columnsRequired();
		}

		this.configuredColumns = List.copyOf(configuredColumns);
		this.writeHandlers = new ArrayList<>();
	}

	/**
	 * 注册单元格写入处理器
	 * @param writeHandler 处理器
	 * @return 当前实例
	 */
	public TempFileMapExcelExportWriter registerWriteHandler(CellWriteHandler writeHandler) {
		this.writeHandlers.add(writeHandler);
		return this;
	}

	/**
	 * 写入临时文件并返回路径
	 * @param writeAction 分块写入动作
	 * @return 临时文件路径
	 * @throws IOException 写入失败
	 */
	public Path writeToTempFile(ChunkExportWriter.ChunkWriteAction<Map<String, Object>> writeAction)
			throws IOException {
		return TempExcelFileSupport.writeToTempFile(outputStream -> {
			LazyMapExcelSession session = new LazyMapExcelSession(outputStream);
			try {
				writeAction.write(session::writeChunk);
				session.finish();
			}
			catch (RuntimeException exception) {
				session.closeQuietly();
				throw exception;
			}
		});
	}

	/**
	 * 一次性写入 Map 行并返回字节
	 * @param rows Map 行
	 * @return xlsx 字节
	 * @throws IOException 写入失败
	 */
	public byte[] write(List<Map<String, Object>> rows) throws IOException {
		Path tempFile = writeToTempFile(chunkWriter -> {
			if (rows != null && !rows.isEmpty()) {
				chunkWriter.writeChunk(rows);
			}
		});
		try {
			return Files.readAllBytes(tempFile);
		}
		finally {
			Files.deleteIfExists(tempFile);
		}
	}

	private final class LazyMapExcelSession {

		private final OutputStream outputStream;

		private ExcelWriter excelWriter;

		private WriteSheet writeSheet;

		private List<ExportColumn> columns;

		private LazyMapExcelSession(OutputStream outputStream) {
			this.outputStream = outputStream;
		}

		private void writeChunk(List<Map<String, Object>> rows) throws IOException {
			if (rows == null || rows.isEmpty()) {
				return;
			}
			ensureWriter();
			try {
				excelWriter.write(MapExcelRowSupport.toCellRows(rows, columns), writeSheet);
			}
			catch (RuntimeException exception) {
				throw new IOException("Excel export failed", exception);
			}
		}

		private void ensureWriter() {
			if (excelWriter != null) {
				return;
			}
			columns = MapExcelRowSupport.resolveColumns(configuredColumns);
			ExcelWriterBuilder builder = EasyExcelFactory.write(outputStream)
				.head(MapExcelRowSupport.buildHead(columns));
			TempExcelFileSupport.applyDefaultHandlers(builder, writeHandlers);
			excelWriter = builder.build();
			writeSheet = EasyExcelFactory.writerSheet(sheetName).build();
		}

		private void finish() {
			if (excelWriter != null) {
				excelWriter.finish();
				excelWriter = null;
				return;
			}
			writeEmptyWorkbook();
		}

		private void closeQuietly() {
			if (excelWriter == null) {
				return;
			}
			try {
				excelWriter.finish();
			}
			catch (RuntimeException ignored) {
				// best-effort close after write failure
			}
			finally {
				excelWriter = null;
			}
		}

		private void writeEmptyWorkbook() {
			ExcelWriterBuilder builder = EasyExcelFactory.write(outputStream);
			if (!configuredColumns.isEmpty()) {
				builder.head(MapExcelRowSupport.buildHead(configuredColumns));
			}
			TempExcelFileSupport.applyDefaultHandlers(builder, writeHandlers);
			ExcelWriter emptyWriter = builder.build();
			try {
				emptyWriter.write(List.of(), EasyExcelFactory.writerSheet(sheetName).build());
			}
			finally {
				emptyWriter.finish();
			}
		}

	}

}
