package com.auth.service.system.file.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 文件异步导出执行尝试表
 *
 * @author Bunny
 */
@TableName("file_export_attempt")
@Schema(name = "FileExportAttemptEntity", title = "文件异步导出执行尝试")
@Getter
@Setter
public class FileExportAttemptEntity extends BaseEntity {

	@Schema(title = "任务ID")
	private Long taskId;

	@Schema(title = "第几次执行")
	private Integer attemptNo;

	@Schema(title = "执行实例标识")
	private String workerId;

	@Schema(title = "执行结果")
	private String result;

	@Schema(title = "处理行数")
	private Long processedRows;

	@Schema(title = "错误码")
	private String errorCode;

	@Schema(title = "错误信息")
	private String errorMessage;

	@Schema(title = "开始执行时间")
	private Instant startedAt;

	@Schema(title = "结束时间")
	private Instant finishedAt;

}
