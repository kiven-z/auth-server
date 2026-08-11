package com.auth.service.system.file.model.enums;

/**
 * 文件异步导出任务状态
 *
 * @author Bunny
 */
public enum FileExportTaskStatus {

	/**
	 * 待处理
	 */
	PENDING,

	/**
	 * 运行中
	 */
	RUNNING,

	/**
	 * 取消中
	 */
	CANCELLING,

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
	 * 过期
	 */
	EXPIRED

}
