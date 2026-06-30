package com.auth.module.file.importer.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ImportErrors} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ImportErrors 导入错误工厂")
class ImportErrorsTest {

	@Test
	@DisplayName("required 生成标准必填错误文案")
	void required_formatsMessage() {
		ImportRowError error = ImportErrors.required(3, "roleCode");

		assertThat(error.rowIndex()).isEqualTo(3);
		assertThat(error.message()).isEqualTo("Row 3: roleCode is required");
		assertThat(error.code()).isEqualTo(ImportErrorCode.REQUIRED);
		assertThat(error.args()).containsExactly("roleCode");
	}

	@Test
	@DisplayName("duplicateInFile 生成标准文件内重复文案")
	void duplicateInFile_formatsMessage() {
		ImportRowError error = ImportErrors.duplicateInFile(2, "username", "admin");

		assertThat(error.message()).isEqualTo("Row 2: username 'admin' duplicated in file");
		assertThat(error.code()).isEqualTo(ImportErrorCode.DUPLICATE_IN_FILE);
	}

	@Test
	@DisplayName("duplicatePostInFile 生成岗位复合键重复文案")
	void duplicatePostInFile_formatsCompositeMessage() {
		ImportRowError error = ImportErrors.duplicatePostInFile(1, "P01", "D01");

		assertThat(error.message()).isEqualTo("Row 1: postCode 'P01' duplicated in file for deptCode 'D01'");
	}

	@Test
	@DisplayName("alreadyExistsPost 生成岗位复合键已存在文案")
	void alreadyExistsPost_formatsCompositeMessage() {
		ImportRowError error = ImportErrors.alreadyExistsPost(4, "P02", "D02");

		assertThat(error.message()).isEqualTo("Row 4: postCode 'P02' already exists for deptCode 'D02'");
	}

	@Test
	@DisplayName("rowLimitExceeded 生成行数超限文案")
	void rowLimitExceeded_formatsMessage() {
		ImportRowError error = ImportErrors.rowLimitExceeded(500);

		assertThat(error.rowIndex()).isZero();
		assertThat(error.message()).isEqualTo("Import row count exceeds limit: max=500");
		assertThat(error.code()).isEqualTo(ImportErrorCode.ROW_LIMIT_EXCEEDED);
	}

	@Test
	@DisplayName("statusRequired 生成域特定状态必填文案")
	void statusRequired_formatsDomainMessage() {
		ImportRowError error = ImportErrors.statusRequired(5);

		assertThat(error.message()).isEqualTo("Row 5: status is required");
		assertThat(error.code()).isEqualTo(ImportErrorCode.INVALID_VALUE);
	}

	@Test
	@DisplayName("referenceUnavailable 生成引用不可用文案")
	void referenceUnavailable_formatsMessage() {
		ImportRowError error = ImportErrors.referenceUnavailable(6, "deptCode", "RD");

		assertThat(error.message()).isEqualTo("Row 6: deptCode 'RD' does not exist or is unavailable");
		assertThat(error.code()).isEqualTo(ImportErrorCode.INVALID_VALUE);
		assertThat(error.args()).containsExactly("deptCode", "RD");
	}

}
