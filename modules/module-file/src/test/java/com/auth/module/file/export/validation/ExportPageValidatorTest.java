package com.auth.module.file.export.validation;

import com.auth.module.file.exception.ExportException;
import com.auth.module.file.export.model.ExportLimits;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ExportPageValidator} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ExportPageValidator 导出分页校验")
class ExportPageValidatorTest {

	@Test
	@DisplayName("pageSize 为 null 时返回默认值")
	void normalizePageSize_whenNull_returnsDefault() {
		assertThat(ExportPageValidator.normalizePageSize(null)).isEqualTo(ExportPageValidator.DEFAULT_PAGE_SIZE);
	}

	@Test
	@DisplayName("pageSize 小于下限时抛出英文非法条数异常")
	void normalizePageSize_whenBelowMin_throwsPageSizeInvalid() {
		assertThatThrownBy(() -> ExportPageValidator.normalizePageSize(0)).isInstanceOf(ExportException.class)
			.hasMessageContaining("Invalid export page size");
	}

	@Test
	@DisplayName("pageSize 大于上限时抛出英文非法条数异常")
	void normalizePageSize_whenAboveMax_throwsPageSizeInvalid() {
		assertThatThrownBy(() -> ExportPageValidator.normalizePageSize(ExportLimits.MAX_PAGE_SIZE + 1))
			.isInstanceOf(ExportException.class)
			.hasMessageContaining("Invalid export page size");
	}

	@Test
	@DisplayName("结束页小于起始页时抛出英文页码区间异常")
	void validatePageRange_whenToPageBeforeFromPage_throwsPageRangeInvalid() {
		assertThatThrownBy(() -> ExportPageValidator.validatePageRange(2, 1, 10)).isInstanceOf(ExportException.class)
			.hasMessageContaining("Invalid export page range");
	}

	@Test
	@DisplayName("跨页行数超过上限时抛出英文行数限制异常")
	void validatePageRange_whenRowSpanExceedsLimit_throwsRowLimitExceeded() {
		assertThatThrownBy(() -> ExportPageValidator.validatePageRange(1, 50, 30)).isInstanceOf(ExportException.class)
			.hasMessageContaining("Export row count exceeds limit");
	}

}
