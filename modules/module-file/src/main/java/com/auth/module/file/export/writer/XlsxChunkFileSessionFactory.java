package com.auth.module.file.export.writer;

import com.alibaba.excel.write.handler.CellWriteHandler;
import com.auth.module.file.exception.ExportColumnsRequiredException;
import com.auth.module.file.export.definition.ExportColumn;
import com.auth.module.file.export.definition.ExportSheetMeta;
import com.auth.module.file.export.shard.ChunkFileSession;
import com.auth.module.file.export.shard.ChunkFileSessionFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * XLSX 单文件导出会话工厂
 *
 * @author Bunny
 */
public final class XlsxChunkFileSessionFactory implements ChunkFileSessionFactory<Map<String, Object>> {

	public static final String FILE_EXTENSION = "xlsx";

	public static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	private static final String XLSX_SUFFIX = ".xlsx";

	private final String sheetName;

	private final List<ExportColumn> columns;

	private final List<CellWriteHandler> writeHandlers;

	/**
	 * @param sheetMeta 工作表元信息（须含列定义）
	 */
	public XlsxChunkFileSessionFactory(ExportSheetMeta sheetMeta) {
		this(sheetMeta.sheetName(), sheetMeta.columns(), sheetMeta.writeHandlers());
	}

	/**
	 * @param sheetName 工作表名称
	 * @param columns 显式列定义（必填）
	 * @param writeHandlers 写入处理器（可空）
	 */
	public XlsxChunkFileSessionFactory(String sheetName, List<ExportColumn> columns,
			List<CellWriteHandler> writeHandlers) {
		if (columns == null || columns.isEmpty()) {
			throw new ExportColumnsRequiredException();
		}
		this.sheetName = sheetName;
		this.columns = MapExcelRowSupport.resolveColumns(columns);
		this.writeHandlers = writeHandlers == null ? List.of() : List.copyOf(writeHandlers);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChunkFileSession<Map<String, Object>> open() throws IOException {
		Path tempFile = TempFileSupport.createTempFile(XLSX_SUFFIX);
		try {
			return new XlsxChunkFileSession(tempFile, sheetName, columns, writeHandlers);
		}
		catch (IOException | RuntimeException exception) {
			TempFileSupport.deleteQuietly(tempFile);
			throw exception;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String fileExtension() {
		return FILE_EXTENSION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String contentType() {
		return CONTENT_TYPE;
	}

}
