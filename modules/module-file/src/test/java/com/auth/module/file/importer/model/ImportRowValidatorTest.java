package com.auth.module.file.importer.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.auth.module.file.importer.model.ImportErrorCode.INVALID_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ImportRowValidator} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ImportRowValidator 行级校验流水线")
class ImportRowValidatorTest {

	@Test
	@DisplayName("require 失败时 fail-fast 且仅产生一条错误")
	void require_whenBlank_failFast() {
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(1, errors);

		String roleCode = v.require(null, "roleCode");
		String roleName = v.require("名称", "roleName");

		assertThat(roleCode).isNull();
		assertThat(roleName).isNull();
		assertThat(v.failed()).isTrue();
		assertThat(errors).hasSize(1);
		assertThat(errors.get(0).message()).isEqualTo("Row 1: roleCode is required");
	}

	@Test
	@DisplayName("unique 优先报告文件内重复")
	void unique_whenDuplicateInFile_reportsDuplicate() {
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(2, errors);

		v.unique("admin", Set.of("admin"), Set.of("admin"), "username");

		assertThat(v.failed()).isTrue();
		assertThat(errors.get(0).message()).isEqualTo("Row 2: username 'admin' duplicated in file");
	}

	@Test
	@DisplayName("unique 库中已存在时报告 already exists")
	void unique_whenExistsInDb_reportsExists() {
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(3, errors);

		v.unique("admin", Set.of(), Set.of("admin"), "username");

		assertThat(errors.get(0).message()).isEqualTo("Row 3: username 'admin' already exists");
	}

	@Test
	@DisplayName("assertThat 条件不成立时记录格式错误")
	void assertThat_whenFalse_recordsError() {
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(4, errors);

		v.assertThat(false, INVALID_FORMAT, "roleCode", "bad");

		assertThat(errors.get(0).message()).isEqualTo("Row 4: roleCode 'bad' does not match required format");
	}

	@Test
	@DisplayName("lengthBetween 超出范围时记录长度错误")
	void lengthBetween_whenOutOfRange_recordsError() {
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(5, errors);

		v.lengthBetween("short", 8, 18, "initialPassword");

		assertThat(errors.get(0).message()).isEqualTo("Row 5: initialPassword length must be between 8 and 18");
	}

	@Test
	@DisplayName("refExists 引用不存在时记录 not found")
	void refExists_whenMissing_recordsNotFound() {
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(6, errors);

		v.refExists(null, "deptCode", "D99");

		assertThat(errors.get(0).message()).isEqualTo("Row 6: deptCode 'D99' not found");
	}

	@Test
	@DisplayName("校验全部通过时 ok 为 true")
	void pipeline_whenAllValid_ok() {
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(7, errors);

		String code = v.require(" ADMIN ", "roleCode");
		v.unique(code, Set.of(), Set.of(), "roleCode").assertThat(true, INVALID_FORMAT, "roleCode", code);

		assertThat(v.ok()).isTrue();
		assertThat(code).isEqualTo("ADMIN");
		assertThat(errors).isEmpty();
	}

	@Test
	@DisplayName("链式调用返回同一 validator 实例")
	void chainMethods_returnSameValidator() {
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(8, errors);

		ImportRowValidator chained = v.unique("x", Set.of(), Set.of(), "f")
			.assertThat(true, INVALID_FORMAT, "f", "x")
			.lengthBetween("abcdefgh", 8, 18, "f")
			.refExists("ref", "f", "x");

		assertThat(chained).isSameAs(v);
		assertThat(v.ok()).isTrue();
	}

	@Test
	@DisplayName("mapOrNull 失败时不执行 builder")
	void mapOrNull_whenFailed_skipsBuilder() {
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(9, errors);
		v.require(null, "roleCode");
		AtomicBoolean builderRan = new AtomicBoolean(false);

		String result = v.mapOrNull(() -> {
			builderRan.set(true);
			return "form";
		});

		assertThat(result).isNull();
		assertThat(builderRan).isFalse();
	}

	@Test
	@DisplayName("mapOrNull 成功时返回 builder 结果")
	void mapOrNull_whenOk_returnsBuilderResult() {
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(10, errors);

		String result = v.mapOrNull(() -> "form");

		assertThat(result).isEqualTo("form");
		assertThat(v.ok()).isTrue();
	}

	@Test
	@DisplayName("whenOk 已失败时不执行 step")
	void whenOk_whenFailed_skipsStep() {
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(11, errors);
		v.require(null, "roleCode");
		AtomicBoolean stepRan = new AtomicBoolean(false);

		v.whenOk(ignored -> stepRan.set(true));

		assertThat(stepRan).isFalse();
	}

	@Test
	@DisplayName("whenOk 成功时执行 step 并返回 this")
	void whenOk_whenOk_runsStepAndReturnsThis() {
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(12, errors);
		AtomicBoolean stepRan = new AtomicBoolean(false);

		ImportRowValidator returned = v.whenOk(ignored -> stepRan.set(true));

		assertThat(stepRan).isTrue();
		assertThat(returned).isSameAs(v);
	}

}
