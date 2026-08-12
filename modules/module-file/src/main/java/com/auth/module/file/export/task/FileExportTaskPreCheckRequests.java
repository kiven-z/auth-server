package com.auth.module.file.export.task;

import com.auth.common.core.utils.JsonSupport;
import com.auth.module.file.api.model.request.ExportPreCheckRequest;
import lombok.experimental.UtilityClass;

/**
 * 组装异步导出前置检查请求
 *
 * @author Bunny
 */
@UtilityClass
public class FileExportTaskPreCheckRequests {

	/**
	 * 由业务筛选条件组装前置检查请求
	 * @param bizType 业务类型
	 * @param query 列表筛选（序列化为 querySnapshotJson）
	 * @param fetchService 取数服务名
	 * @param fetchPath 取数路径
	 * @return 前置检查请求
	 */
	public static ExportPreCheckRequest of(String bizType, Object query, String fetchService, String fetchPath) {
		ExportPreCheckRequest request = new ExportPreCheckRequest();
		request.setBizType(bizType);
		request.setFetchService(fetchService);
		request.setFetchPath(fetchPath);
		request.setQuerySnapshotJson(query == null ? "{}" : JsonSupport.toJson(query));
		return request;
	}

}
