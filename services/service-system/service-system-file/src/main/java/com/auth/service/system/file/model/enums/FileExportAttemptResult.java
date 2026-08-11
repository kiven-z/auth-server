package com.auth.service.system.file.model.enums;

/**
 * 文件异步导出执行结果
 *
 * @author Bunny
 */
public enum FileExportAttemptResult {

	/**
	 * 成功
	 */
	SUCCESS,

	/**
	 * 失败
	 */
	FAILED,

	/**
	 * 取消
	 */
	CANCELLED,

	/**
	 * 超时
	 */
	TIMEOUT

}
