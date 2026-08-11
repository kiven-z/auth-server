package com.auth.service.example.export;

import com.auth.module.file.api.model.dto.ExportPageDataDTO;
import com.auth.service.example.model.entity.ExampleOrderEntity;
import com.auth.service.example.model.query.ExampleOrderQuery;
import com.auth.service.example.service.ExampleOrderService;
import com.auth.service.example.support.export.ExampleBizCodes;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link ExampleOrderExportPageFetcher} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ExampleOrderExportPageFetcher 仅导出 Spreadsheet 字段")
@ExtendWith(MockitoExtension.class)
class ExampleOrderExportPageFetcherTest {

	@Mock
	private ExampleOrderService exampleOrderService;

	@InjectMocks
	private ExampleOrderExportPageFetcher fetcher;

	@Test
	@DisplayName("fetchPage：Map 仅含 title/deptId/id")
	void fetchPage_mapsOnlySpreadsheetFields() {
		ExampleOrderEntity entity = new ExampleOrderEntity();
		entity.setId(7L);
		entity.setTitle("演示单");
		entity.setDeptId(100L);
		entity.setRemark("不应出现在导出 Map");
		entity.setCreatedBy(1L);

		Page<ExampleOrderEntity> page = new Page<>(1, 10, 1);
		page.setRecords(List.of(entity));
		when(exampleOrderService.exportPage(any(ExampleOrderQuery.class), eq(1), eq(10))).thenReturn(page);

		ExportPageDataDTO data = fetcher.fetchPage(ExampleBizCodes.EXAMPLE_ORDER, "{}", 1, 10);

		assertThat(data.getTotal()).isEqualTo(1);
		Map<String, Object> row = data.getRecords().get(0);
		assertThat(row).containsEntry("title", "演示单")
			.containsEntry("deptId", 100L)
			.containsEntry("id", 7L)
			.doesNotContainKeys("remark", "createdBy", "updatedBy", "createdAt", "updatedAt", "version");
	}

}
