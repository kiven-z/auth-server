package com.auth.module.file.export.writer;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.model.enums.FileExportFormat;
import com.auth.module.file.exception.ExportValidationException;
import com.auth.module.file.export.definition.ExportSheetMeta;

import java.util.Locale;
import java.util.Map;

/**
 * 导出写入器工厂（按导出格式分配 Map 行动态表头写入器）
 *
 * @author Bunny
 */
public class ExportWriterFactory {

	private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	/**
	 * 创建异步导出写入器
	 * @param exportFormat 导出格式
	 * @param sheetMeta 工作表元信息（须含列定义）
	 * @return 写入器句柄
	 */
	public ExportWriterHandle<Map<String, Object>> create(String exportFormat, ExportSheetMeta sheetMeta) {
		FileExportFormat format = parseFormat(exportFormat);
		return switch (format) {
			case XLSX -> new ExportWriterHandle<>(new XlsxChunkExportWriter(sheetMeta), "xlsx", XLSX_CONTENT_TYPE);
			case CSV, ZIP -> throw ExportValidationException.exportFormatInvalid(format.name());
		};
	}

	private FileExportFormat parseFormat(String exportFormat) {
		if (CharSequenceUtil.isBlank(exportFormat)) {
			throw ExportValidationException.exportFormatInvalid(exportFormat);
		}
		try {
			return FileExportFormat.valueOf(exportFormat.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException exception) {
			throw ExportValidationException.exportFormatInvalid(exportFormat);
		}
	}

	/**
	 * 导出写入器句柄
	 *
	 * @param writer 分块写入器
	 * @param fileExtension 文件扩展名
	 * @param contentType MIME
	 * @param <R> 导出行类型
	 */
	public record ExportWriterHandle<R>(ChunkExportWriter<R> writer, String fileExtension, String contentType) {
	}

}
