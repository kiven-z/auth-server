package com.auth.module.file.export.writer;

import com.auth.module.file.exception.ExportValidationException;
import com.auth.module.file.export.definition.ExportColumn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link MapExcelRowSupport} 单元测试
 *
 * @author Bunny
 */
@DisplayName("MapExcelRowSupport Map 导出行转换")
class MapExcelRowSupportTest {

	@Test
	@DisplayName("resolveColumns：返回显式列定义")
	void resolveColumns_whenConfigured_returnsConfigured() {
		List<ExportColumn> configured = List.of(ExportColumn.builder().fieldKey("id").header("主键").build());

		List<ExportColumn> columns = MapExcelRowSupport.resolveColumns(configured);

		assertThat(columns).hasSize(1);
		assertThat(columns.get(0).getFieldKey()).isEqualTo("id");
		assertThat(columns.get(0).getHeader()).isEqualTo("主键");
	}

	@Test
	@DisplayName("resolveColumns：无显式列时抛校验异常")
	void resolveColumns_whenEmpty_throwsValidationException() {
		List<ExportColumn> empty = List.of();
		assertThatThrownBy(() -> MapExcelRowSupport.resolveColumns(empty)).isInstanceOf(ExportValidationException.class)
			.hasMessageContaining("导出列定义不能为空");
	}

	@Test
	@DisplayName("toCellRows：Long 转字符串，Instant 转 ISO 文本")
	void toCellRows_whenLongAndInstant_normalizesValues() {
		Instant now = Instant.parse("2026-08-10T12:00:00Z");
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("id", 2_061_699_478_481_860_001L);
		row.put("createdAt", now);
		List<ExportColumn> columns = List.of(ExportColumn.builder().fieldKey("id").header("id").build(),
				ExportColumn.builder().fieldKey("createdAt").header("createdAt").build());

		List<List<Object>> cellRows = MapExcelRowSupport.toCellRows(List.of(row), columns);

		assertThat(cellRows).hasSize(1);
		assertThat(cellRows.get(0)).containsExactly("2061699478481860001", "2026-08-10T12:00:00Z");
	}

}
