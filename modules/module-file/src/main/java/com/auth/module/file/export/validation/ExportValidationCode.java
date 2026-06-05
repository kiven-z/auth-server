package com.auth.module.file.export.validation;

/**
 * 导出参数校验错误码 FIXME 取消，使用英文方式
 *
 * @author Bunny
 */
public enum ExportValidationCode {

	/**
	 * 页码区间无效
	 */
	PAGE_RANGE_INVALID,

	/**
	 * 跨页行数超限
	 */
	ROW_LIMIT_EXCEEDED,

	/**
	 * 每页条数非法
	 */
	PAGE_SIZE_INVALID,

	/**
	 * 页码区间字段缺失
	 */
	PAGE_RANGE_REQUIRED,

	/**
	 * 当前页页码缺失或非法
	 */
	CURRENT_PAGE_INDEX_REQUIRED,

	/**
	 * 全量异步导出尚未支持
	 */
	FULL_ASYNC_NOT_SUPPORTED

}
