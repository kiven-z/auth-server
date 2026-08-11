package com.auth.service.example.support.export;

import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;
import com.auth.module.file.export.definition.ExportColumn;
import com.auth.module.file.export.task.FileExportTaskCreateRequests;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 演示服务异步导出创建请求组装
 *
 * @author Bunny
 */
@UtilityClass
public class ExampleFileExportTaskCreateRequests {

	/**
	 * 取数目标服务（Nacos 注册名）
	 */
	public static final String SERVICE_EXAMPLE = "service-example";

	/**
	 * 内部分页取数路径
	 */
	@SuppressWarnings("java:S1075")
	public static final String INNER_EXPORT_PAGE_PATH = "/api/example/inner/export/page";

	/**
	 * 由业务筛选条件组装 XLSX 异步导出创建请求
	 * @param bizType 业务类型
	 * @param query 列表筛选（序列化为 querySnapshotJson）
	 * @param requestId 幂等请求 ID（可空）
	 * @param createdBy 创建用户 ID
	 * @param columns 导出列定义
	 * @return 创建请求
	 */
	public static FileExportTaskCreateRequest xlsx(String bizType, Object query, String requestId, Long createdBy,
			List<ExportColumn> columns) {
		return FileExportTaskCreateRequests.xlsx(bizType, query, requestId, createdBy, SERVICE_EXAMPLE,
				INNER_EXPORT_PAGE_PATH, columns);
	}

}
