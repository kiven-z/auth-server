package com.auth.module.file.exception;

import lombok.Getter;

/**
 * 导出校验失败（入参或契约不合法）
 *
 * @author Bunny
 */
@Getter
public final class ExportValidationException extends ExportException {

	/**
	 * 校验字段名
	 */
	private final String field;

	/**
	 * @param field 校验字段名（映射业务异常参数）
	 * @param message 错误消息
	 */
	public ExportValidationException(String field, String message) {
		super(message);
		this.field = field;
	}

	/**
	 * @param maxRows 允许的最大行数
	 * @param requestedRows 请求行数
	 * @return 校验异常
	 */
	public static ExportValidationException rowLimitExceeded(long maxRows, long requestedRows) {
		return new ExportValidationException("maxRows", "导出行数超出限制：max=" + maxRows + ", requested=" + requestedRows);
	}

	/**
	 * @param exportFormat 非法或不支持的格式
	 * @return 校验异常
	 */
	public static ExportValidationException exportFormatInvalid(String exportFormat) {
		if (exportFormat == null || exportFormat.isBlank()) {
			return new ExportValidationException("exportFormat", "导出格式必填");
		}
		return new ExportValidationException("exportFormat", "导出格式非法：" + exportFormat);
	}

	/**
	 * @return 列定义缺失时的校验异常
	 */
	public static ExportValidationException columnsRequired() {
		return new ExportValidationException("columnSnapshotJson", "导出列定义不能为空");
	}

	/**
	 * @return 列快照 JSON 非法时的校验异常
	 */
	public static ExportValidationException columnSnapshotInvalid() {
		return new ExportValidationException("columnSnapshotJson", "导出列快照 JSON 非法");
	}

}
