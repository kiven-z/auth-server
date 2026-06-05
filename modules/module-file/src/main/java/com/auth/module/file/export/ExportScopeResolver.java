package com.auth.module.file.export;

import com.auth.module.file.export.model.ExportPageRange;
import com.auth.module.file.export.model.enums.ExportScope;
import com.auth.module.file.export.validation.ExportPageValidator;
import com.auth.module.file.export.validation.ExportValidationCode;
import com.auth.module.file.export.validation.ExportValidationException;

import lombok.experimental.UtilityClass;

/**
 * 按 {@link ExportScope} 解析实际导出的页码区间
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
		ExportScope scope = query.getExportScope() != null ? query.getExportScope() : ExportScope.PAGE_RANGE;
		return switch (scope) {
			case CURRENT_PAGE -> resolveCurrentPage(query);
			case PAGE_RANGE -> resolvePageRange(query);
			case FULL_ASYNC -> throw new ExportValidationException(ExportValidationCode.FULL_ASYNC_NOT_SUPPORTED);
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
			throw new ExportValidationException(ExportValidationCode.CURRENT_PAGE_INDEX_REQUIRED);
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
			throw new ExportValidationException(ExportValidationCode.PAGE_RANGE_REQUIRED);
		}
		int pageSize = ExportPageValidator.normalizePageSize(query.getPageSize());
		return new ExportPageRange(fromPage, toPage, pageSize);
	}

}
