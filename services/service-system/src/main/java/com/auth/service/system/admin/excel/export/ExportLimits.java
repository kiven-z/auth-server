package com.auth.service.system.admin.excel.export;

import lombok.experimental.UtilityClass;

/**
 * 管理端 Excel 导出通用上限（非配置项，各业务导出服务引用）
 *
 * @author Bunny
 */
@UtilityClass
public class ExportLimits {

	/**
	 * 单次导出允许的最大行数（跨页累计）
	 */
	public static final int MAX_EXPORT_ROWS = 1000;

	/**
	 * 导出请求中单页最大条数
	 */
	public static final int MAX_PAGE_SIZE = 500;

	/**
	 * 导出请求中单页最小条数
	 */
	public static final int MIN_PAGE_SIZE = 1;

}
