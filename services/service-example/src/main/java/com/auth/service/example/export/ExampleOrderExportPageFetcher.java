package com.auth.service.example.export;

import com.auth.common.core.utils.JsonSupport;
import com.auth.module.file.api.model.dto.ExportPageDataDTO;
import com.auth.module.file.export.definition.ExportRowMaps;
import com.auth.service.example.excel.ExampleOrderSpreadsheetRow;
import com.auth.service.example.model.entity.ExampleOrderEntity;
import com.auth.service.example.model.query.ExampleOrderQuery;
import com.auth.service.example.service.ExampleOrderService;
import com.auth.service.example.support.export.ExampleBizCodes;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 演示单导出分页取数
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class ExampleOrderExportPageFetcher {

	private final ExampleOrderService exampleOrderService;

	/**
	 * 按业务类型拉取分页数据
	 * @param bizType 业务类型
	 * @param querySnapshotJson 查询条件快照
	 * @param pageNum 页码
	 * @param pageSize 每页条数
	 * @return 分页数据
	 */
	public ExportPageDataDTO fetchPage(String bizType, String querySnapshotJson, int pageNum, int pageSize) {
		if (!ExampleBizCodes.EXAMPLE_ORDER.equalsIgnoreCase(bizType)) {
			throw new IllegalArgumentException("Unsupported bizType: " + bizType);
		}
		ExampleOrderQuery query = parseQuery(querySnapshotJson);
		IPage<ExampleOrderEntity> page = exampleOrderService.exportPage(query, pageNum, pageSize);
		List<Map<String, Object>> records = page.getRecords().stream().map(entity -> {
			ExampleOrderSpreadsheetRow row = new ExampleOrderSpreadsheetRow();
			row.setTitle(entity.getTitle());
			row.setDeptId(entity.getDeptId());
			row.setId(entity.getId());
			return row;
		}).map(ExportRowMaps::fromBean).toList();
		return ExportPageDataDTO.builder().records(records).total(page.getTotal()).build();
	}

	private ExampleOrderQuery parseQuery(String querySnapshotJson) {
		if (querySnapshotJson == null || querySnapshotJson.isBlank()) {
			return new ExampleOrderQuery();
		}
		try {
			ExampleOrderQuery query = JsonSupport.fromJson(querySnapshotJson, ExampleOrderQuery.class);
			return query == null ? new ExampleOrderQuery() : query;
		}
		catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Invalid querySnapshotJson", exception);
		}
	}

}
