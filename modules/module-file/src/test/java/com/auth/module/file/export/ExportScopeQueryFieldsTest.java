package com.auth.module.file.export;

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
	@DisplayName("fromPage 与 toPage 均合法时无校验错误")
	void validate_whenPagesPresent_passes() {
		// 构造合法页码区间
		ExportScopeQueryFields fields = new ExportScopeQueryFields();
		fields.setFromPage(1);
		fields.setToPage(2);

		Set<ConstraintViolation<ExportScopeQueryFields>> violations = validator.validate(fields);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("fromPage 为空时返回非空校验错误")
	void validate_whenFromPageMissing_fails() {
		// 仅设置结束页，触发 fromPage 非空约束
		ExportScopeQueryFields fields = new ExportScopeQueryFields();
		fields.setToPage(1);

		Set<ConstraintViolation<ExportScopeQueryFields>> violations = validator.validate(fields);

		assertThat(violations).extracting(ConstraintViolation::getMessage).contains("起始页码不能为空");
	}

}
