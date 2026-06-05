package com.auth.module.file.export;

import com.auth.module.file.export.model.ExportPageRange;
import com.auth.module.file.export.model.enums.ExportScope;
import com.auth.module.file.export.validation.ExportValidationCode;
import com.auth.module.file.export.validation.ExportValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ExportScopeResolver} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ExportScopeResolver 导出范围解析")
class ExportScopeResolverTest {

	@Test
	@DisplayName("未传 exportScope 时按页码区间解析")
	void resolve_whenScopeNull_usesPageRange() {
		StubExportQuery query = new StubExportQuery();
		query.setFromPage(2);
		query.setToPage(4);
		query.setPageSize(20);

		ExportPageRange range = ExportScopeResolver.resolve(query);

		assertThat(range.fromPage()).isEqualTo(2);
		assertThat(range.toPage()).isEqualTo(4);
		assertThat(range.pageSize()).isEqualTo(20);
	}

	@Test
	@DisplayName("CURRENT_PAGE 使用 pageIndex 作为单页区间")
	void resolve_whenCurrentPage_usesPageIndex() {
		StubExportQuery query = new StubExportQuery();
		query.setExportScope(ExportScope.CURRENT_PAGE);
		query.setPageIndex(5);
		query.setPageSize(15);

		ExportPageRange range = ExportScopeResolver.resolve(query);

		assertThat(range.fromPage()).isEqualTo(5);
		assertThat(range.toPage()).isEqualTo(5);
		assertThat(range.pageSize()).isEqualTo(15);
	}

	@Test
	@DisplayName("FULL_ASYNC 抛出尚未支持异常")
	void resolve_whenFullAsync_throwsNotSupported() {
		StubExportQuery query = new StubExportQuery();
		query.setExportScope(ExportScope.FULL_ASYNC);

		assertThatThrownBy(() -> ExportScopeResolver.resolve(query)).isInstanceOf(ExportValidationException.class)
			.extracting(ex -> ((ExportValidationException) ex).getCode())
			.isEqualTo(ExportValidationCode.FULL_ASYNC_NOT_SUPPORTED);
	}

	/**
	 * 测试用导出查询桩
	 */
	private static final class StubExportQuery implements ExportScopeQuery {

		private ExportScope exportScope;

		private Integer pageIndex;

		private Integer fromPage;

		private Integer toPage;

		private Integer pageSize;

		@Override
		public ExportScope getExportScope() {
			return exportScope;
		}

		void setExportScope(ExportScope exportScope) {
			this.exportScope = exportScope;
		}

		@Override
		public Integer getPageIndex() {
			return pageIndex;
		}

		void setPageIndex(Integer pageIndex) {
			this.pageIndex = pageIndex;
		}

		@Override
		public Integer getFromPage() {
			return fromPage;
		}

		void setFromPage(Integer fromPage) {
			this.fromPage = fromPage;
		}

		@Override
		public Integer getToPage() {
			return toPage;
		}

		void setToPage(Integer toPage) {
			this.toPage = toPage;
		}

		@Override
		public Integer getPageSize() {
			return pageSize;
		}

		void setPageSize(Integer pageSize) {
			this.pageSize = pageSize;
		}

	}

}
