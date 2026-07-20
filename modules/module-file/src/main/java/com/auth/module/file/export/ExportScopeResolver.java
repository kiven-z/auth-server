package com.auth.module.file.export;

import com.auth.module.file.exception.ExportException;
import com.auth.module.file.export.model.ExportPageRange;
import com.auth.module.file.export.model.enums.ExportScope;
import com.auth.module.file.export.validation.ExportPageValidator;
import lombok.experimental.UtilityClass;

import java.util.Objects;

/**
 * 解析实际导出的页码区间
 *
 * @author Bunny
 */
@UtilityClass
public class ExportScopeResolver {

	/**
	 * 根据查询中的导出范围解析页码区间并完成 pageSize 标准化
	 * @param query 导出查询
	 * @return 页码区间
	 */
	public static ExportPageRange resolve(ExportScopeQuery query) {
		ExportScope scope = Objects.requireNonNullElse(query.getExportScope(), ExportScope.PAGE_RANGE);
		return switch (scope) {
			case CURRENT_PAGE -> resolveCurrentPage(query);
			case PAGE_RANGE -> resolvePageRange(query);
			case FULL_ASYNC -> throw ExportException.fullAsyncNotSupported();
		};
	}

	/**
	 * 解析当前页码区间
	 * @param query 导出查询
	 * @return 页码区间
	 */
	private static ExportPageRange resolveCurrentPage(ExportScopeQuery query) {
		Integer pageIndex = query.getPageIndex();
		if (pageIndex == null || pageIndex < 1) {
			throw ExportException.currentPageIndexRequired();
		}
		int pageSize = ExportPageValidator.normalizePageSize(query.getPageSize());
		return new ExportPageRange(pageIndex, pageIndex, pageSize);
	}

	/**
	 * 解析页码区间
	 * @param query 导出查询
	 * @return 页码区间
	 */
	private static ExportPageRange resolvePageRange(ExportScopeQuery query) {
		Integer fromPage = query.getFromPage();
		Integer toPage = query.getToPage();
		if (fromPage == null || toPage == null) {
			throw ExportException.pageRangeRequired();
		}
		int pageSize = ExportPageValidator.normalizePageSize(query.getPageSize());
		return new ExportPageRange(fromPage, toPage, pageSize);
	}

}
