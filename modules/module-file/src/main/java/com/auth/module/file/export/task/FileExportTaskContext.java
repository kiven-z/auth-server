package com.auth.module.file.export.task;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * 异步导出任务上下文
 *
 * @author Bunny
 */
@Value
@Builder
public class FileExportTaskContext {

	/**
	 * 任务ID
	 */
	Long taskId;

	/**
	 * 业务类型
	 */
	String bizType;

	/**
	 * 导出格式
	 */
	String exportFormat;

	/**
	 * 查询条件快照JSON
	 */
	String querySnapshotJson;

	/**
	 * 导出列快照JSON
	 */
	String columnSnapshotJson;

	/**
	 * 数据权限快照JSON
	 */
	String scopeSnapshotJson;

	/**
	 * 分页取数目标服务名
	 */
	String fetchService;

	/**
	 * 分页取数路径
	 */
	String fetchPath;

	/**
	 * 创建人
	 */
	Long createdBy;

	/**
	 * 任务创建时间
	 */
	Instant createdAt;

	/**
	 * 分页拉取大小
	 */
	int fetchPageSize;

	/**
	 * 最大导出行数
	 */
	long maxRows;

	/**
	 * 分片阈值（≤ 0 表示不分片，单文件输出）
	 */
	int shardRows;

}
