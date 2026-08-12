package com.auth.module.file.exception;

import lombok.Getter;

/**
 * 导出行数超过上限
 *
 * @author Bunny
 */
@Getter
public final class ExportRowLimitExceededException extends ExportValidationException {

	private final long maxRows;

	private final long requestedRows;

	/**
	 * @param maxRows 允许的最大行数
	 * @param requestedRows 请求行数
	 */
	public ExportRowLimitExceededException(long maxRows, long requestedRows) {
		super("maxRows", "最多导出 " + maxRows + " 行，当前约 " + requestedRows + " 行，请缩小筛选范围后重试");
		this.maxRows = maxRows;
		this.requestedRows = requestedRows;
	}

}
