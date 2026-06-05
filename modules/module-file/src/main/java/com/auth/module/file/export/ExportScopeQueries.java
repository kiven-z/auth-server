package com.auth.module.file.export;

import com.auth.common.core.model.query.PageQueryRequest;
import com.auth.module.file.export.model.enums.ExportScope;
import lombok.experimental.UtilityClass;

/**
 * 将列表筛选与导出范围
 *
 * @author Bunny
 */
@UtilityClass
public class ExportScopeQueries {

	/**
	 * 合并分页筛选与导出范围字段
	 * @param filter 列表筛选（含 pageIndex / pageSize）
	 * @param scope 导出范围字段
	 * @return 导出范围查询视图
	 */
	public static ExportScopeQuery merge(PageQueryRequest filter, ExportScopeQueryFields scope) {
		return new MergedExportScopeQuery(filter, scope);
	}

	/**
	 * 筛选 + 范围的只读适配器
	 */
	private record MergedExportScopeQuery(PageQueryRequest filter,
			ExportScopeQueryFields scope) implements ExportScopeQuery {

		@Override
		public ExportScope getExportScope() {
			return scope.getExportScope();
		}

		@Override
		public Integer getPageIndex() {
			return filter.getPageIndex();
		}

		@Override
		public Integer getFromPage() {
			return scope.getFromPage();
		}

		@Override
		public Integer getToPage() {
			return scope.getToPage();
		}

		@Override
		public Integer getPageSize() {
			return filter.getPageSize();
		}

	}

}
