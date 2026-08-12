package com.auth.module.file.exception;

/**
 * 导出格式未填写
 *
 * @author Bunny
 */
public final class ExportFormatRequiredException extends ExportValidationException {

	/**
	 * 构造导出格式必填异常
	 */
	public ExportFormatRequiredException() {
		super("exportFormat", "导出格式必填");
	}

}
