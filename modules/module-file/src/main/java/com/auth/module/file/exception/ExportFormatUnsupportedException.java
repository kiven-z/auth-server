package com.auth.module.file.exception;

import lombok.Getter;

/**
 * 导出格式非法或不支持
 *
 * @author Bunny
 */
@Getter
public final class ExportFormatUnsupportedException extends ExportValidationException {

	private final String exportFormat;

	/**
	 * @param exportFormat 非法或不支持的格式
	 */
	public ExportFormatUnsupportedException(String exportFormat) {
		super("exportFormat", "导出格式非法：" + exportFormat);
		this.exportFormat = exportFormat;
	}

}
