package com.auth.module.file.export.writer;

import com.alibaba.excel.write.handler.CellWriteHandler;
import com.auth.module.file.export.definition.ExportSheetMeta;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Map 行 XLSX 分块导出写入器（异步分页导出）
 *
 * @author Bunny
 */
public class XlsxChunkExportWriter implements ChunkExportWriter<Map<String, Object>> {

	private final TempFileMapExcelExportWriter tempFileMapExcelExportWriter;

	/**
	 * @param sheetMeta 工作表元信息（须含列定义）
	 */
	public XlsxChunkExportWriter(ExportSheetMeta sheetMeta) {
		this.tempFileMapExcelExportWriter = new TempFileMapExcelExportWriter(sheetMeta.sheetName(),
				sheetMeta.columns());
		for (CellWriteHandler writeHandler : sheetMeta.writeHandlers()) {
			this.tempFileMapExcelExportWriter.registerWriteHandler(writeHandler);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] write(List<Map<String, Object>> rows) throws IOException {
		return tempFileMapExcelExportWriter.write(rows);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path writeToTempFile(ChunkWriteAction<Map<String, Object>> writeAction) throws IOException {
		return tempFileMapExcelExportWriter.writeToTempFile(writeAction);
	}

}
