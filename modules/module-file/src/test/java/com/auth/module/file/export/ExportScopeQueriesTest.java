package com.auth.module.file.export;

import com.auth.common.core.model.query.PageQueryRequest;
import com.auth.module.file.export.model.ExportPageRange;
import com.auth.module.file.export.model.enums.ExportScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ExportScopeQueries} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ExportScopeQueries 筛选与导出范围合并")
class ExportScopeQueriesTest {

	@Test
	@DisplayName("merge 后 ExportScopeResolver 可按页码区间解析")
	void merge_whenPageRange_resolvesRangeFromScopeAndFilter() {
		// 筛选与范围分属两个对象
		PageQueryRequest filter = new PageQueryRequest();
		filter.setPageIndex(5);
		filter.setPageSize(20);
		ExportScopeQueryFields scope = new ExportScopeQueryFields();
		scope.setFromPage(2);
		scope.setToPage(4);

		ExportScopeQuery merged = ExportScopeQueries.merge(filter, scope);
		ExportPageRange range = ExportScopeResolver.resolve(merged);

		assertThat(range.fromPage()).isEqualTo(2);
		assertThat(range.toPage()).isEqualTo(4);
		assertThat(range.pageSize()).isEqualTo(20);
	}

	@Test
	@DisplayName("merge 后 CURRENT_PAGE 使用 filter 的 pageIndex，无需 fromPage/toPage")
	void merge_whenCurrentPage_usesFilterPageIndex() {
		// CURRENT_PAGE 只依赖 filter.pageIndex，scope 中无需提供 fromPage/toPage
		PageQueryRequest filter = new PageQueryRequest();
		filter.setPageIndex(3);
		filter.setPageSize(15);
		ExportScopeQueryFields scope = new ExportScopeQueryFields();
		scope.setExportScope(ExportScope.CURRENT_PAGE);

		ExportScopeQuery merged = ExportScopeQueries.merge(filter, scope);
		ExportPageRange range = ExportScopeResolver.resolve(merged);

		assertThat(range.fromPage()).isEqualTo(3);
		assertThat(range.toPage()).isEqualTo(3);
		assertThat(range.pageSize()).isEqualTo(15);
	}

}
