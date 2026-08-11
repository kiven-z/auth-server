package com.auth.service.system.file.model.vo;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * 文件异步导出任务管理端详情（含租约；不含权限快照）
 *
 * @author Bunny
 */
@Schema(name = "FileExportTaskAdminDetailVO", title = "文件异步导出任务管理端详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class FileExportTaskAdminDetailVO extends BaseResponse {

	@Schema(title = "业务类型")
	private String bizType;

	@Schema(title = "导出格式")
	private String exportFormat;

	@Schema(title = "查询条件快照JSON（提交时冻结的列表筛选）")
	private String querySnapshotJson;

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
