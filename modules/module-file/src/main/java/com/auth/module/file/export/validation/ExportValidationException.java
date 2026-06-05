package com.auth.module.file.export.validation;

import lombok.Getter;

import java.util.Arrays;

/**
 * 导出参数校验异常 FIXMe 只是抛出异常
 *
 * @author Bunny
 */
@Getter
public class ExportValidationException extends RuntimeException {

	private final ExportValidationCode code;

	private final Object[] args;

	/**
	 * @param code 校验错误码
	 * @param args 占位参数
	 */
	public ExportValidationException(ExportValidationCode code, Object... args) {
		super(code.name());
		this.code = code;
		this.args = args != null ? args : new Object[0];
	}

	@Override
	public String getMessage() {
		return code.name() + (args.length > 0 ? " " + Arrays.toString(args) : "");
	}

}
