package com.auth.module.file.importer.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RowOutcome} 单元测试
 *
 * @author Bunny
 */
@DisplayName("RowOutcome 行处理结果")
class RowOutcomeTest {

	@Test
	@DisplayName("ok 携带值且无错误")
	void ok_carriesValue() {
		RowOutcome<String> outcome = RowOutcome.ok("value");

		assertThat(outcome.ok()).isTrue();
		assertThat(outcome.value()).isEqualTo("value");
		assertThat(outcome.errors()).isEmpty();
	}

	@Test
	@DisplayName("err 无值且携带错误")
	void err_carriesErrors() {
		ImportRowError error = ImportErrors.required(1, "username");
		RowOutcome<String> outcome = RowOutcome.err(error);

		assertThat(outcome.ok()).isFalse();
		assertThat(outcome.value()).isNull();
		assertThat(outcome.errors()).containsExactly(error);
	}

	@Test
	@DisplayName("map 成功时转换值")
	void map_whenOk_transformsValue() {
		RowOutcome<Integer> outcome = RowOutcome.ok(2).map(n -> n * 2);

		assertThat(outcome.ok()).isTrue();
		assertThat(outcome.value()).isEqualTo(4);
	}

	@Test
	@DisplayName("map 失败时保留错误")
	void map_whenErr_preservesErrors() {
		ImportRowError error = ImportErrors.required(3, "email");
		RowOutcome<String> outcome = RowOutcome.<String>err(error).map(String::toUpperCase);

		assertThat(outcome.ok()).isFalse();
		assertThat(outcome.errors()).containsExactly(error);
	}

}
