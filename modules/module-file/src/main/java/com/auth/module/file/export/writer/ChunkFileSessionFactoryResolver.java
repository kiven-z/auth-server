package com.auth.module.file.export.writer;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.model.enums.FileExportFormat;
import com.auth.module.file.exception.ExportFormatRequiredException;
import com.auth.module.file.exception.ExportFormatUnsupportedException;
import com.auth.module.file.export.definition.ExportSheetMeta;
import com.auth.module.file.export.shard.ChunkFileSessionFactory;

import java.util.Locale;
import java.util.Map;

/**
 * 按导出格式解析对应的单文件会话工厂
 *
 * @author Bunny
 */
public class ChunkFileSessionFactoryResolver {

	private static FileExportFormat parseFormat(String exportFormat) {
		if (CharSequenceUtil.isBlank(exportFormat)) {
			throw new ExportFormatRequiredException();
		}
		try {
			return FileExportFormat.valueOf(exportFormat.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException e) {
			throw new ExportFormatUnsupportedException(exportFormat);
		}
	}

	/**
	 * 解析并创建对应格式的会话工厂
	 * @param exportFormat 导出格式（大小写不敏感）
	 * @param sheetMeta 工作表元信息
	 * @return 单文件会话工厂
	 */
	public ChunkFileSessionFactory<Map<String, Object>> resolve(String exportFormat, ExportSheetMeta sheetMeta) {
		FileExportFormat format = parseFormat(exportFormat);
		return switch (format) {
			case XLSX -> new XlsxChunkFileSessionFactory(sheetMeta);
			case CSV, ZIP -> throw new ExportFormatUnsupportedException(format.name());
		};
	}

}
