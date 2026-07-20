package com.auth.module.file.export;

import com.auth.module.file.export.model.enums.ExportScope;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ExportScopeQueryFields} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ExportScopeQueryFields 导出范围字段校验")
class ExportScopeQueryFieldsTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	@DisplayName("PAGE_RANGE 模式：fromPage 与 toPage 均合法时无校验错误")
	void validate_whenPagesPresent_passes() {
		// PAGE_RANGE 时正常填写 fromPage/toPage
		ExportScopeQueryFields fields = new ExportScopeQueryFields();
		fields.setFromPage(1);
		fields.setToPage(2);

		Set<ConstraintViolation<ExportScopeQueryFields>> violations = validator.validate(fields);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("CURRENT_PAGE 模式：不填 fromPage/toPage 也无校验错误")
	void validate_whenCurrentPageScopeAndNoPagesProvided_passes() {
		// CURRENT_PAGE 时 fromPage/toPage 不是必填，JSR-303 层面不报错
		ExportScopeQueryFields fields = new ExportScopeQueryFields();
		fields.setExportScope(ExportScope.CURRENT_PAGE);

		Set<ConstraintViolation<ExportScopeQueryFields>> violations = validator.validate(fields);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("fromPage 为 0 时触发 @Min 校验错误")
	void validate_whenFromPageZero_fails() {
		// 提供了 fromPage 但值非法（小于 1）
		ExportScopeQueryFields fields = new ExportScopeQueryFields();
		fields.setFromPage(0);
		fields.setToPage(1);

		Set<ConstraintViolation<ExportScopeQueryFields>> violations = validator.validate(fields);

		assertThat(violations).extracting(ConstraintViolation::getMessage).contains("起始页码不能小于1");
	}

	@Test
	@DisplayName("toPage 为负数时触发 @Min 校验错误")
	void validate_whenToPageNegative_fails() {
		// 提供了 toPage 但值非法
		ExportScopeQueryFields fields = new ExportScopeQueryFields();
		fields.setFromPage(1);
		fields.setToPage(-1);

		Set<ConstraintViolation<ExportScopeQueryFields>> violations = validator.validate(fields);

		assertThat(violations).extracting(ConstraintViolation::getMessage).contains("结束页码不能小于1");
	}

}
