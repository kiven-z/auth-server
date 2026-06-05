package com.auth.module.file.export;

import com.auth.module.file.export.model.enums.ExportScope;

/**
 * 导出查询需提供的范围字段
 *
 * @author Bunny
 */
public interface ExportScopeQuery {

	/**
	 * 导出范围；未传时由 {@link ExportScopeResolver} 按页码区间处理
	 * @return 导出范围
	 */
	ExportScope getExportScope();

	/**
	 * 当前页码（CURRENT_PAGE 时使用）
	 * @return 页码，1-based
	 */
	Integer getPageIndex();

	/**
	 * 起始页码（PAGE_RANGE 时使用）
	 * @return 起始页，1-based
	 */
	Integer getFromPage();

	/**
	 * 结束页码（PAGE_RANGE 时使用）
	 * @return 结束页，1-based
	 */
	Integer getToPage();

	/**
	 * 每页条数
	 * @return 分页大小
	 */
	Integer getPageSize();

}
