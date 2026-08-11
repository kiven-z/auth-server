package com.auth.service.system.admin.support.export;

import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;
import com.auth.module.file.export.definition.ExportColumn;
import com.auth.module.file.export.task.FileExportTaskCreateRequests;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class AdminFileExportTaskCreateRequests {

	/**
	 * 默认取数目标服务（service-system 聚合部署）
	 */
	public static final String SERVICE_SYSTEM = "service-system";

	/**
	 * 统一内部分页取数路径（bizType 由任务携带）
	 */
	@SuppressWarnings("java:S1075")
	public static final String INTERNAL_EXPORT_PAGE_PATH = "/api/system/inner/export/page";

	/**
	 * 由业务筛选条件组装创建请求
	 * @param bizType 业务类型
	 * @param query 列表筛选（序列化为 querySnapshotJson）
	 * @param requestId 幂等请求 ID（可空）
	 * @param createdBy 创建用户 ID
	 * @param columns 导出列定义
	 * @return 创建请求
	 */
	public static FileExportTaskCreateRequest xlsx(String bizType, Object query, String requestId, Long createdBy,
			List<ExportColumn> columns) {
		return FileExportTaskCreateRequests.xlsx(bizType, query, requestId, createdBy, SERVICE_SYSTEM,
				INTERNAL_EXPORT_PAGE_PATH, columns);
	}

}
