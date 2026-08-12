package com.auth.module.file.exception;

/**
 * 导出列快照 JSON 非法
 *
 * @author Bunny
 */
public final class ExportColumnSnapshotInvalidException extends ExportValidationException {

	/**
	 * 构造列快照非法异常
	 */
	public ExportColumnSnapshotInvalidException() {
		super("columnSnapshotJson", "导出列快照 JSON 非法");
	}

}
