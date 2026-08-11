package com.auth.service.system.file.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 文件异步导出执行尝试行
 *
 * @author Bunny
 */
@Schema(name = "FileExportAttemptVO", title = "文件异步导出执行尝试行")
@Getter
@Setter
@ToString
public class FileExportAttemptVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

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
