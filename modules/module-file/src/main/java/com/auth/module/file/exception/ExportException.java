package com.auth.module.file.exception;

/**
 * 导出异常基类（校验失败与执行失败的公共父类）
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
