package com.auth.module.file.exception;

/**
 * 导出列定义缺失
 *
 * @author Bunny
 */
public final class ExportColumnsRequiredException extends ExportValidationException {

	/**
	 * 构造列定义缺失异常
	 */
	public ExportColumnsRequiredException() {
		super("columnSnapshotJson", "导出列定义不能为空");
	}

}
