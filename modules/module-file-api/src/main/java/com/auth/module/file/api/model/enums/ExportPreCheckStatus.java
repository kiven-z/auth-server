package com.auth.module.file.api.model.enums;

/**
 * 导出前置检查状态
 *
 * @author Bunny
 */
public enum ExportPreCheckStatus {

	/**
	 * 总行数未超过分片阈值，产物为单个 xlsx
	 */
	NORMAL,

	/**
	 * 总行数超过分片阈值但未超过总上限，产物为多分片 zip
	 */
	SHARDED,

	/**
	 * 总行数超过最大导出上限，不可导出
	 */
	EXCEEDED

}
