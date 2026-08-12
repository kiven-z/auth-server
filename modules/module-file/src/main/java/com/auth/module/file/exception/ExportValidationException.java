package com.auth.module.file.exception;

import lombok.Getter;

/**
 * 导出校验失败（入参或契约不合法）
 *
 * @author Bunny
 */
@Getter
public abstract sealed class ExportValidationException extends RuntimeException
		permits ExportRowLimitExceededException, ExportFormatRequiredException, ExportFormatUnsupportedException,
		ExportColumnsRequiredException, ExportColumnSnapshotInvalidException {

	/**
	 * 校验字段名
	 */
	private final String field;

	/**
	 * @param field 校验字段名（映射业务异常参数）
	 * @param message 错误消息
	 */
	protected ExportValidationException(String field, String message) {
		super(message);
		this.field = field;
	}

}
