package com.auth.module.file.exception;

/**
 * 导出执行失败（写入或任务执行过程出错）
 *
 * @author Bunny
 */
public final class ExportExecutionException extends ExportException {

	/**
	 * @param message 错误消息
	 */
	public ExportExecutionException(String message) {
		super(message);
	}

	/**
	 * @return 请求取消时的执行异常
	 */
	public static ExportExecutionException cancelled() {
		return new ExportExecutionException("导出已按请求取消");
	}

	/**
	 * @param reason 失败原因
	 * @return 写入失败时的执行异常
	 */
	public static ExportExecutionException writeFailed(String reason) {
		return new ExportExecutionException("导出写入失败：" + reason);
	}

	/**
	 * @param reason 失败原因
	 * @return 远程取数失败时的执行异常
	 */
	public static ExportExecutionException remoteFetchFailed(String reason) {
		return new ExportExecutionException("远程分页取数失败：" + reason);
	}

}
