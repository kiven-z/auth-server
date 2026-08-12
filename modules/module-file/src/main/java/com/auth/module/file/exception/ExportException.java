package com.auth.module.file.exception;

/**
 * 导出执行异常基类
 *
 * @author Bunny
 */
public abstract class ExportException extends RuntimeException {

	/**
	 * @param message 错误消息
	 */
	protected ExportException(String message) {
		super(message);
	}

}
