package com.auth.module.file.export.definition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ExportRowMaps} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ExportRowMaps SpreadsheetRow 转 Map")
class ExportRowMapsTest {

	@Test
	@DisplayName("fromBean：字段名作为 key，忽略 null")
	void fromBean_whenRowHasValues_mapsFieldKeys() {
		DemoRow row = new DemoRow();
		row.setTitle("订单A");
		row.setId(1001L);

		Map<String, Object> map = ExportRowMaps.fromBean(row);

		assertThat(map).containsEntry("title", "订单A").containsEntry("id", 1001L).doesNotContainKey("deptId");
	}

	@Test
	@DisplayName("fromBean：空入参返回空 Map")
	void fromBean_whenNull_returnsEmptyMap() {
		assertThat(ExportRowMaps.fromBean(null)).isEmpty();
	}

	@lombok.Getter
	@lombok.Setter
	private static class DemoRow {

		private String title;

		private Long deptId;

		private Long id;

	}

}
