package com.auth.module.file.export.task;

import com.auth.module.file.api.model.enums.FileExportFormat;
import com.auth.module.file.export.definition.ExportColumn;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * 异步导出创建命令（含取数路由与列定义）
 *
 * @author Bunny
 */
@Value
@Builder
public class FileExportTaskCreateCommand {

	/**
	 * 业务类型
	 */
	String bizType;

	/**
	 * 列表筛选（序列化为 querySnapshotJson）
	 */
	Object query;

	/**
	 * 幂等请求 ID（可空）
	 */
	String requestId;

	/**
	 * 创建用户 ID
	 */
	Long createdBy;

	/**
	 * 导出格式
	 */
	FileExportFormat exportFormat;

	/**
	 * 分页取数目标服务名
	 */
	String fetchService;

	/**
	 * 分页取数路径
	 */
	String fetchPath;

	/**
	 * 导出列定义
	 */
	List<ExportColumn> columns;

}
