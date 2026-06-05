package com.auth.module.file.export;

import com.auth.module.file.exception.ExportException;
import com.auth.module.file.export.model.ExportPageRange;
import com.auth.module.file.export.model.enums.ExportScope;
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
		query.setFromPage();
		query.setToPage();
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
		query.setPageIndex();
		query.setPageSize(15);

		ExportPageRange range = ExportScopeResolver.resolve(query);

		assertThat(range.fromPage()).isEqualTo(5);
		assertThat(range.toPage()).isEqualTo(5);
		assertThat(range.pageSize()).isEqualTo(15);
	}

	@Test
	@DisplayName("FULL_ASYNC 抛出英文尚未支持异常")
	void resolve_whenFullAsync_throwsNotSupported() {
		StubExportQuery query = new StubExportQuery();
		query.setExportScope(ExportScope.FULL_ASYNC);

		assertThatThrownBy(() -> ExportScopeResolver.resolve(query)).isInstanceOf(ExportException.class)
			.hasMessage("Full async export is not supported yet");
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

		@Override
		public Integer getFromPage() {
			return fromPage;
		}

		@Override
		public Integer getToPage() {
			return toPage;
		}

		@Override
		public Integer getPageSize() {
			return pageSize;
		}

		void setPageSize(Integer pageSize) {
			this.pageSize = pageSize;
		}

		void setPageIndex() {
			this.pageIndex = 5;
		}

		void setFromPage() {
			this.fromPage = 2;
		}

		void setToPage() {
			this.toPage = 4;
		}

	}

}
