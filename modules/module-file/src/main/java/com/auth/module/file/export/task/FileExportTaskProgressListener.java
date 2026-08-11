package com.auth.module.file.export.task;

/**
 * 异步导出进度监听器
 *
 * @author Bunny
 */
public interface FileExportTaskProgressListener {

	/**
	 * 更新导出进度
	 * @param processedRows 已处理行数
	 * @param totalRows 总行数
	 */
	void onProgress(long processedRows, long totalRows);

	/**
	 * 是否收到取消请求
	 * @return 取消中则 true
	 */
	boolean isCancelRequested();

}
