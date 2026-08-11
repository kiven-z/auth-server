package com.auth.service.system.file.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 文件异步导出任务主表
 *
 * @author Bunny
 */
@TableName("file_export_task")
@Schema(name = "FileExportTaskEntity", title = "文件异步导出任务")
@Getter
@Setter
public class FileExportTaskEntity extends BaseEntity {

	@Schema(title = "业务类型")
	private String bizType;

	@Schema(title = "导出格式")
	private String exportFormat;

	@Schema(title = "查询条件快照JSON")
	private String querySnapshotJson;

	@Schema(title = "导出列快照JSON")
	private String columnSnapshotJson;

	@Schema(title = "数据权限快照JSON")
	private String scopeSnapshotJson;

	@Schema(title = "分页取数目标服务名")
	private String fetchService;

	@Schema(title = "分页取数路径")
	private String fetchPath;

	@Schema(title = "任务状态")
	private String status;

	@Schema(title = "进度百分比")
	private Integer progressPercent;

	@Schema(title = "已处理行数")
	private Long processedRows;

	@Schema(title = "总行数")
	private Long totalRows;

	@Schema(title = "产物文件记录ID")
	private Long fileRecordId;

	@Schema(title = "错误码")
	private String errorCode;

	@Schema(title = "错误信息")
	private String errorMessage;

	@Schema(title = "重试次数")
	private Integer retryCount;

	@Schema(title = "最大重试次数")
	private Integer maxRetry;

	@Schema(title = "下次重试时间")
	private Instant nextRetryAt;

	@Schema(title = "租约持有者")
	private String lockedBy;

	@Schema(title = "租约到期时间")
	private Instant leaseUntil;

	@Schema(title = "幂等请求ID")
	private String requestId;

	@Schema(title = "任务开始时间")
	private Instant startedAt;

	@Schema(title = "任务结束时间")
	private Instant finishedAt;

	@Schema(title = "产物过期时间")
	private Instant expireAt;

}
