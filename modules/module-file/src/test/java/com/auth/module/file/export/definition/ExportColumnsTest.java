package com.auth.module.file.export.definition;

import com.alibaba.excel.annotation.ExcelProperty;
import com.auth.module.file.exception.ExportValidationException;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ExportColumns} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ExportColumns 列定义与快照")
class ExportColumnsTest {

	@Test
	@DisplayName("fromExcelProperties：按 index 生成 fieldKey 与 header")
	void fromExcelProperties_whenAnnotated_buildsOrderedColumns() {
		List<ExportColumn> columns = ExportColumns.fromExcelProperties(DemoRow.class);

		assertThat(columns).extracting(ExportColumn::getFieldKey).containsExactly("code", "name");
		assertThat(columns).extracting(ExportColumn::getHeader).containsExactly("编码", "名称");
	}

	@Test
	@DisplayName("snapshot：JSON 往返保持列定义")
	void snapshot_whenRoundTrip_preservesColumns() {
		List<ExportColumn> columns = ExportColumns.fromExcelProperties(DemoRow.class);

		String json = ExportColumns.toSnapshotJson(columns);
		List<ExportColumn> restored = ExportColumns.fromSnapshotJson(json);

		assertThat(restored).isEqualTo(columns);
		assertThat(json).contains("\"fieldKey\":\"code\"").contains("\"header\":\"编码\"");
	}

	@Test
	@DisplayName("fromSnapshotJson：空数组抛校验异常")
	void fromSnapshotJson_whenEmptyArray_throwsValidationException() {
		assertThatThrownBy(() -> ExportColumns.fromSnapshotJson("[]")).isInstanceOf(ExportValidationException.class)
			.hasMessageContaining("导出列定义不能为空");
	}

	@Getter
	@Setter
	private static class DemoRow {

		@ExcelProperty(value = "编码", index = 0)
		private String code;

		@ExcelProperty(value = "名称", index = 1)
		private String name;

	}

}
