package com.auth.module.file.export.model.enums;

/**
 * 导出范围：决定拉取数据的页码策略
 *
 * @author Bunny
 */
public enum ExportScope {

	/**
	 * 仅导出当前页（使用 pageIndex）
	 */
	CURRENT_PAGE,

	/**
	 * 导出指定页码区间（fromPage ~ toPage）
	 */
	PAGE_RANGE,

	/**
	 * 全量异步导出（预留，Phase 2 接入任务与对象存储）
	 */
	FULL_ASYNC

}
